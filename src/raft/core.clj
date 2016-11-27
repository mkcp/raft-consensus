(ns raft.core
  (:require [raft.node :as n]
            [clojure.core.async :refer [chan go go-loop timeout >! >!! <! <!! alts! close!]]
            [clojure.spec :as s]
            [taoensso.timbre :as t]
            [taoensso.timbre.appenders.core :as appenders]
            [taoensso.timbre.appenders.3rd-party.rotor :as rotor]))

(t/merge-config! {:appenders
                  {:rotor (rotor/rotor-appender {:max-size (* 1024 1024)
                                                 :backlog 32
                                                 :path "raft.log"})}})

(def ^:dynmaic ctrl-chan (chan))

;; NOTE
;; Inboxes and outboxes are pretty janky global state, but they work for a simple local simulation.
;; I would eventually like to abstract this out.
;; If I were being optimisic this is a simple impl of ports on loopback.
;; Ideally, these wouldn't be predefined, but 
(def network
  (atom {:1 {:in (chan)
             :out (chan)}
         :2 {:in (chan)
             :out (chan)}
         :3 {:in (chan)
             :out (chan)}
         :4 {:in (chan)
             :out (chan)}
         :5 {:in (chan)
             :out (chan)}}))

(defn broadcast-append
  "Debug function to test adding to all inboxes."
  []
  (go (doseq [[id inbox] @inboxes]
        (>! inbox [:request-append {}]))))

(defn send-messages!
  [{:keys [messages]}]
  (let [inboxes @inboxes]
    (doseq [message messages]
      (let [[procedure body] message]
        (>! ((:to body) inboxes) message)))))

(defn start-node
  "Reads off of each node's inbox until a timeout is reached or the node receives a signal on the
  ctrl-chan."
  [node timeout-duration]
  (let [[id initial-node] node
        {:keys [in out]} (id @network)]

    ;; Reader loop
    (go-loop [node initial-node]
      (let [[message port] (alts! [(timeout timeout-duration) in ctrl-chan])
            stop? (= port ctrl-chan)]
        (when-not stop?
          (let [new-node (n/handle message node)]
            (t/info {:message "tick completed"
                     :new-node new-node})
            (recur node)))))

    ;; Writer loop
    (go-loop []
      ;; TODO Take from the node's out channel and apply to necessary in channel
      (let [message (<! out)
            [_ {:keys [to]}] message
            destination (get-in @network [to :in])]
        (>! destination message)))))

(defn stop-sim! [] (>!! ctrl-chan :stop))

(defn start!
  "Create a network and start a loop for each server."
  []
  (let [ids (keys @network)
        event {:nodes ids
               :count (count ids)
               :state :started}]
    (doseq [node network]
      (start-node node 3000))
    (t/info event)))

;; Dev fns
#_(start!)
#_(stop-sim!)

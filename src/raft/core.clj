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

(def ctrl-chan (chan))
(def network (n/create-1))

(defn start-node
  "Reads off of each node's inbox until a timeout is reached or the node receives a signal on the
  ctrl-chan."
  [id election-timeout append-frequency]
  (let [{:keys [node in out]} (id network)]

    ;; Reader loop
    (go-loop []
      (t/info {:message "Loop started"})
      (let [[message port] (alts! [(timeout election-timeout) in ctrl-chan])
            stop? (= port ctrl-chan)]
        (when-not stop?
          (let [new-node (n/handle message @node)]
            (n/commit-update node new-node)
            (recur)))))

    ;; Writer loop
    #_(go-loop []
        (let [state (:state @node)
              [message port] (alts! [(if n/leader? append-frequency)
                                     out
                                     ctrl-chan])
              stop? (= port ctrl-chan)]
          (when-not stop?
            (let [[_ {:keys [to]}] message
                  destination (get-in network [to :in])]
              (>! destination message)))))
    ))

(defn stop! [] (>!! ctrl-chan :stop))

(defn start!
  "Create a network and start a loop for each server."
  []
  (let [ids (keys network)
        event {:nodes ids
               :count (count ids)
               :state :started}]
    (doseq [id ids]
      (start-node id 1000 250))
    (t/info event)))

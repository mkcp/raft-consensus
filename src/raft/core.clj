(ns raft.core
  (:require [raft.node :as n]
            [clojure.core.async :refer [chan go go-loop timeout >! >!! <! <!! alts! close!]]
            [clojure.spec :as s]
            [taoensso.timbre :as t]
            [taoensso.timbre.appenders.core :as appenders]
            [taoensso.timbre.appenders.3rd-party.rotor :as rotor]))

(t/merge-config! {:appenders
                  {:rotor (rotor/rotor-appender {:max-size (* 1024 1024 1024)
                                                 :backlog 32
                                                 :path "raft.log"})}})

;; FIXME Move these pieces of state off of the namespace.
(def reader-ctrl (chan 100))
(def writer-ctrl (chan 100))
(def network (n/create-1))

;; Sends a message to its destination node's in channel
(defn send! [message network]
  (let [[_ {:keys [to]}] message
        destination (get-in network [to :in])]
    (>! destination message)))

;; FIXME Decompose state management from data transformations
(defn start-node
  "Reads off of each node's inbox until a timeout is reached or the node receives a signal on the
  ctrl-chan."
  [id election-timeout append-frequency]
  (let [{:keys [node in out]} (id network)]

    ;; Read loop
    (go-loop []
      (let [[message port] (alts! [(timeout election-timeout) in reader-ctrl])
            stop? (= port reader-ctrl)]
        (when-not stop?
          (let [new-node (n/read message @node)]
            (swap! node merge new-node))
          (recur))))

    ;; Write loop
    (go-loop []
      (let [append (timeout append-frequency)
            [message port] (alts! [append out writer-ctrl])
            current-node @node
            stop? (= port writer-ctrl)]
        (when-not stop?
          (let [{:keys [state]} current-node
                [procedure {:keys [to]}] message
                new-node (n/write message current-node)]
            (swap! node merge new-node))
          (recur))))))

(defn stop!
  "FIXME: This is silly, each node needs its own set of ctrl chans."
  []
  (doseq [node network]
    (>!! reader-ctrl :stop)
    (>!! writer-ctrl :stop)))

(defn start!
  "Create a network and start a loop for each server."
  []
  (let [ids (keys network)
        event {:nodes ids
               :count (count ids)
               :state :starting}]
    (t/info event)
    (doseq [id ids]
      (start-node id 2000 500))))

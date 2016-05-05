(ns raft.core
  (:require [raft.server :as s]
            [clojure.core.async
             :as a
             :refer [chan go]]))

(def goals '[node-states leader-election log-replication])

;;;; Node states

;;;; Log replication
(defn append
  [{:keys [log] :as node} entry]
  (let [entry [:append entry]]
    (assoc node :log (conj log entry))))

(defn append-entries [entry node])
(defn confirm-append [leader])
(defn rollback [leader])

;;;; Leader election
(defn vote
  [{:keys [vote-count] :as node}]
  (let [new-count (inc vote-count)]
    (assoc node :vote-count new-count)))

(defn send-vote-request [network])
(defn voted-this-term? [node])
(defn get-timeout [] (random-sample 0.5 #{150 300}))
(defn heartbeat [])
(defn elect-leader [network])
(defn majority-overlap? [config1 config2])
(defn split-vote? [])

;;;; Simulation

(defn network-1 []
  {:1 (s/create [])})

(defn network-2 []
  {:1 (s/create [:2])
   :2 (s/create [:1])})

(defn network-5 []
  {:1 (s/create [:2 :3 :4 :5])
   :2 (s/create [:1 :3 :4 :5])
   :3 (s/create [:1 :2 :4 :5])
   :4 (s/create [:1 :2 :3 :5])
   :5 (s/create [:1 :2 :3 :4])})

(defn main [x]
  (network-1))

(ns raft.core)

(def goals '[node-states log-replication leader-election membership-changes])

;;;; Node states
(defn new-node []
  {:id (rand-int)
   :log []})

(defn follower
  ([] (merge (new-node) {:state :follower}))
  ([node] (assoc node :state :follower)))

(defn candidate
  ([] (merge (new-node) {:state :candidate}))
  ([node] (assoc node :state :candidate)))

(defn leader
  ([] (merge (new-node) {:state :leader}))
  ([node] (assoc node :state :leader)))

;;;; Log replication
(defn append-entries [leader followers])
(defn confirm-append [leader])
(defn rollback [leader])

;;;; Leader election
(defn vote
  [{:keys [vote-count] :as node}]
  (let [new-count (inc vote-count)]
    (assoc node :vote-count new-count)))

(defn higher-term? [network1 network2])
(defn send-vote-request [network])
(defn voted-this-term? [node])
(defn get-timeout [] (random-sample 0.5 #{150 300}))
(defn heartbeat [])
(defn elect-leader [network])
(defn majority-overlap? [config1 config2])
(defn split-vote? [])

(defn main
  "FIXME: Should probably initialize a network and begin log replication from leader"
  [x]
  (println "Main doesn't do anything yet."))

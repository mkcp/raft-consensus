(ns raft.core)

(def todo '[log-replication
            leader-election
            safety
            network-discovery?])

(defn new-node []
  {:term 0
   :vote-count 0})

(defn follower
  ([] (merge (new-node) {:state :follower}))
  ([node] (assoc node :state :follower)))

(defn candidate
  ([] (merge (new-node) {:state :candidate}))
  ([node] (assoc node :state :candidate)))

(defn leader
  ([] (merge (new-node) {:state :leader}))
  ([node] (assoc node :state :leader)))


;;;; Election
(defn vote
  [{:keys [vote-count] :as node}]
  (let [new-count (inc vote-count)]
    (assoc node :vote-count new-count)))

(defn get-timeout [] (random-sample 0.5 #{150 300}))
(defn heartbeat [])
(defn elect-leader [nodes])
(defn majority-overlap? [config1 config2])

(defn main
  "FIXME: Should probably initialize a network and begin log replication from leader"
  [x]
  (println "Main doesn't do anything yet."))

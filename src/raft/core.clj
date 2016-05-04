(ns raft.core)

(def goals '[node-states leader-election log-replication])

;;;; Node states
(defn new-node []
  (atom {:id (str (java.util.UUID/randomUUID))
         :state :follower
         :commit-index 0
         :current-term 0
         :last-applied 0
         :voted-for nil
         :election-alarm 0
         :peers []
         :log []}))

(defn follower
  ([] (swap! (new-node) assoc :state :follower))
  ([node] (swap! node assoc :state :follower)))

(defn candidate
  ([] (swap! (new-node) merge {:state :candidate}))
  ([node] (swap! node assoc :state :candidate)))

(defn leader
  ([] (merge (new-node) {:state :leader}))
  ([{:keys [commit-index] :as node}]
   (swap! node
          assoc
          :state :leader
          :next-index (inc commit-index)
          :match-index commit-index ; FIXME Probably bugged, check peers?
          )))

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

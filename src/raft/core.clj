(ns raft.core)

(def goals '[node-states leader-election log-replication])

;;;; Node states
(defn new-node [peers]
  {:state :follower
   :commit-index 0
   :current-term 0
   :last-applied 0
   :voted-for nil
   :election-alarm 0
   :peers (or peers [])
   :log []})

(defn follower [peers]
  (assoc (new-node peers) :state :follower))

(defn candidate
  ([node] (swap! node assoc :state :candidate)))

(defn leader
  ([{:keys [commit-index] :as node}]
   (swap! node
          assoc
          :state :leader
          :next-index (inc commit-index)
          :match-index commit-index ; FIXME Probably bugged, check peers?
          )))

(defn network []
  {:1 (atom (follower [:2 :3 :4 :5]))
   :2 (atom (follower [:1 :3 :4 :5]))
   :3 (atom (follower [:1 :2 :4 :5]))
   :4 (atom (follower [:1 :2 :3 :5]))
   :5 (atom (follower [:1 :2 :3 :4]))})

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

(defn main [x]
  (network))

(ns raft.log)

(defn append
  [{:keys [log] :as node} entry]
  (let [entry [:append entry]]
    (assoc node :log (conj log entry))))

(defn append-entries [entry node])
(defn confirm-append [leader])
(defn rollback [leader])


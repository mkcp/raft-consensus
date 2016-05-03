(ns raft.core)

(def todo '[networking logging])

(def world
  {:nodes 5
   :leader :1})

(defn create-node []
  {:state :follower})

(defn make-candidate [node]
  (assoc node :state :candidate))

(defn make-leader [node]
  (assoc node :state :leader))

(defn main [x]
  (println "Main doesn't do anything yet."))

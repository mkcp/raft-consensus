(ns raft.network
  (:require [raft.node :as node]
            [clojure.spec :as s]
            [taoensso.timbre :as t]))

(defn now [] (new java.util.Date))

(defn create
  "Takes an int representing the number of nodes to create and returns a mapping of :id to node.
  It's important that peer IDs are passed in, this allows non-homogenous networks to be created."
  [nodes]
  (t/debug {:event :create-network :timestamp (str (now))})
  (case nodes
    1 {:1 (node/create {:id :1 :peers []})}

    2 {:1 (node/create {:id :1 :peers [:2]})
       :2 (node/create {:id :2 :peers [:1]})}

    3 {:1 (node/create {:id :1 :peers [:2 :3]})
       :2 (node/create {:id :2 :peers [:1 :3]})
       :3 (node/create {:id :3 :peers [:1 :2]})}

    5 {:1 (node/create {:id :1 :peers [:2 :3 :4 :5]})
       :2 (node/create {:id :2 :peers [:1 :3 :4 :5]})
       :3 (node/create {:id :3 :peers [:1 :2 :4 :5]})
       :4 (node/create {:id :4 :peers [:1 :2 :3 :5]})
       :5 (node/create {:id :5 :peers [:1 :2 :3 :4]})}

    ;; Default is 1
    {:1 (node/create {:id :1 :peers []})}))

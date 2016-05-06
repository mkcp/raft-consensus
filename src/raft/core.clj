(ns raft.core
  (:require [raft.server :as s]
            [clojure.core.async :as a]))

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

(defn get-peers
  [{:keys [id inbox commit-index] :as state}]
  (let [peer {:peer id
              :inbox inbox
              :next-index (inc commit-index)
              :match-index 0
              :vote-granted false
              :rpc-due 300
              :heartbeat-due 24}]))

(defn set-peers [network]
  (let [peers (map get-peers )
        nodes (values network)
        nodes (map #(assoc % :peers peers) nodes)]
    nodes))

(defn spawn-servers [network]
  (let [servers (resolve-peers network)]
    ;; Spawn a go loop per server which reads from its
    ;; inbox and matches the message to an rpc
    ))

(defn main [x]
  (let [network (network-1)]
    (println network)))

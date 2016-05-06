(ns raft.core
  (:require [raft.server :as s]
            [clojure.core.async
             :as a
             :refer [chan go]]))

(def inboxes
  {:1 (chan)
   :2 (chan)
   :3 (chan)
   :4 (chan)
   :5 (chan)})

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

(defn spawn-servers [network]
  (loop [n (count network)
         server network]
    (if (> n 0)
      (do
        (println server)
        (recur (dec n) network)
        ))

    "Servers started."))

(defn main [x]
  (let [network (network-1)]
    ))

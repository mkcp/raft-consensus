(ns raft.core
  (:require [raft.node :as node]
            [clojure.core.async :refer [chan go go-loop timeout >! >!! <! <!! alts! close!]]
            [clojure.spec :as s]
            [taoensso.timbre :as t]
            [taoensso.timbre.appenders.core :as appenders]
            [taoensso.timbre.appenders.3rd-party.rotor :as rotor]))

;; Configure logging
(t/merge-config! {:appenders
                  {:rotor (rotor/rotor-appender {:max-size (* 1024 1024 1024)
                                                 :backlog 32
                                                 :path "raft.log"})}})

;; NOTE The peer ids get passed in at runtime because it would be useful to eventually simulate full and bridged network partitions.
(defn network
  "Takes an int representing the number of nodes to create and returns a mapping of :id to node. "
  [nodes]
  (case nodes
      1 {:1 (node/create {:id :1 :peers[]})}

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
      {:1 (node/create {:id :1 :peers[]})}))

(defn stop [network]
  (doseq [n network] (node/stop n)))

;; FIXME: Take network size as an arg
(defn start
  "Create a network and starts a loop for each server."
  [network-size]
  (let [ids (keys (network network-size))
        event {:nodes ids
               :count (count ids)
               :state :starting}]
    (t/info event)
    (doseq [id ids]
      (start-node (id network) network 2000 500))))

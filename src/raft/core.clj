(ns raft.core
  (:require [raft.server :as s]
            [clojure.core.async :refer [chan go go-loop timeout >! >!! <! <!! alts!]]
            [taoensso.timbre :as t]
            [taoensso.timbre.appenders.core :as appenders]))

(t/merge-config! {:async? true
                  :appenders {:spit (appenders/spit-appender {:fname "raft.log"})}})

(def inboxes
  (atom {:1 (chan)
         :2 (chan)
         :3 (chan)
         :4 (chan)
         :5 (chan)}))

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

(defn broadcast-append []
  (go (doseq [[id ch] @inboxes]
        (>! ch (s/respond-append :0 id 0 true 0)))))

(defn get-timeout [] (random-sample 0.5 #{150 300}))

(defn handle-tick [request server]
  (case (:state server)
    :follower (s/handle-follower request server)
    :candidate (s/handle-candidate request server)
    :leader (s/handle-leader request server)))

(defn tick
  "Reads a message off the inbox or times out, then peforms the
  resulting state transition before returning the new state of the server."
  [[id server]]
  (let [inbox (id @inboxes)
        request (<! (alts! [inbox (timeout 300)]))
        new-server (handle-tick request server)]

    ;; TODO Send messages
    (comment (for [message messages]
               (>! receipient message)))

    new-server))

(defn start
  "Each loop advances server state by one tick, and performs any logging."
  [config]
  (go-loop [server config]
    (let [new-server (tick server)]
      (t/info new-server)
      (recur new-server))))

(defn main
  "Create a network configuration and start each server."
  []
  (let [network (network-5)
        c (count network)]
    (doseq [server network]
      (start server))
    (t/info c " Server(s) started.")))

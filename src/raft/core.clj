(ns raft.core
  (:require [raft.server :as s]
            [clojure.core.async :refer [chan go go-loop timeout >! >!! <! <!!]]
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

(defn start
  "Takes a server configuration and runs a go loop."
  [config]
  (go-loop [server config]
    (let [[id state] server
          inbox (id @inboxes)]
      (let [msg (<! inbox)
            st (:state state)
            new-state (case st
                        :follower (s/handle-rpc msg)
                        :candidate (s/handle-rpc msg)
                        :leader (s/handle-rpc msg))
            server [id new-state]]
        (t/info server)
        (recur server)))))

(defn main
  "Create a network configuration and start each server."
  []
  (let [network (network-5)
        c (count network)]
    (doseq [server network]
      (start server))
    (t/info c " Server(s) started.")))

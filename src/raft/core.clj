(ns raft.core
  (:require [raft.server :as s]
            [clojure.core.async
             :as a
             :refer [chan go go-loop
                     timeout >! >!! <! <!!]]
            [taoensso.timbre :as t]
            [taoensso.timbre.appenders.core :as appenders]))

(t/merge-config! {:async? true
                  :appenders {:spit (appenders/spit-appender {:fname "raft.log"})}})

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

(defn get-inbox [server]
  (let [id (first server)]
    (id inboxes)))

(defn handle-follower [])
(defn handle-candidate [])
(defn handle-leader [])

(defn handle [server]
  (let [[id state] server
        mode (:state state)]
    (t/info mode)
    (let [[id state] server
          peers (:peers state)
          message (s/request-append (:id state)
                                    (:id (first (:peers state)))
                                    (:current-term state))]
      server)

    #_(case mode
        :follower (handle-follower server)
        :candidate (handle-follower server)
        :leader (handle-follower server))))

(defn start
  "Takes a server configuration and runs a go loop."
  [config]
  (go-loop [server config]
    (let [[id state] server
          ;; FIXME Can decouple from world state?
          inbox (id inboxes)
          to (case id
               :1 :2
               :2 :1)
          msg (<! inbox)]
      (t/info msg)
      (recur server))))

(defn main
  "Create a network configuration and start each server."
  []
  (let [network (network-2)
        c (count network)]

    (go (doseq [[id ch] inboxes]
          (>! ch (s/request-append :test-server id 0))))

    (doseq [server network] (start server))
    (t/infof "%s Server(s) started." c)))

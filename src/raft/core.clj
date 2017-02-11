(ns raft.core
  (:require [raft.node :as node]
            [raft.network :as network]
            [taoensso.timbre :as t]
            [taoensso.timbre.appenders.core :as appenders]
            [taoensso.timbre.appenders.3rd-party.rotor :as rotor]))

;; Configure logging
(t/merge-config! {:appenders
                  {:rotor (rotor/rotor-appender {:max-size (* 1024 1024 1024)
                                                 :backlog 32
                                                 :path "raft.log"})}})

(defn stop [network]
  (doseq [[_ n] network] (node/stop n)))

;; FIXME: Take network size as an arg
(defn start
  "Create a network and starts a loop for each server."
  [network-size]
  (let [network (network/create network-size)
        ids (keys network)]
    (t/info {:nodes ids
             :count (count ids)
             :state :starting})
    (doseq [id ids]
      (node/start (id network) 2000 500))
    network))

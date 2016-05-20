(ns raft.core
  (:require [clojure.core.async :refer [chan go go-loop timeout >! >!! <! <!! alts!]]
            [taoensso.timbre :as t]
            [taoensso.timbre.appenders.core :as appenders]))

(t/merge-config! {:async? true
                  :appenders {:spit (appenders/spit-appender {:fname "raft.log"})}})

(defn peer
  "Takes a peer id and intializes the state for a new network."
  [id]
  {:peer id
   :next-index 1
   :match-index 0
   :vote-granted false
   :rpc-due 300
   :heartbeat-due 300})

(defn create [peers]
  {:state :follower
   :current-term 0
   :voted-for nil
   :commit-index 0
   :last-applied 0
   :election-alarm 0
   :peers (mapv peer peers)
   :log []
   :messages []})

(defn follower
  [server]
  (assoc server
         :state
         :follower))

(defn candidate
  [server]
  (assoc server
         :state :candidate
         :messages [] ; TODO Fill with messages to peers
         ))

(defn leader
  [{:keys [commit-index] :as server}]
  (assoc server
         :state :leader
         :next-index (inc commit-index)
         :match-index commit-index ; FIXME Probably bugged, check peers?
         ))

(defn request-append
  [from to term]
  [:append-entries
   {:from from
    :to to
    :sent :todo-time-sent
    :deliver :todo-time-deliver
    :term term
    :prev-index 0
    :prev-term 0
    :entries []
    :commit-index 0}])

(defn respond-append
  [from to term success? match-index]
  [:append-entries
   {:from from
    :to to
    :sent :todo-time-sent
    :deliver :todo-time-deliver
    :term term
    :success? success?
    :match-index match-index}])

;;;; Leader election
(defn higher-term? [local remote] (< local remote))

;; TODO build messsage from server state
(defn request-vote
  [from to term granted?]
  {:from from
   :to to
   :sent :todo-time-sent
   :deliver :todo-time-deliver
   :term term
   :last-log-index :todo-last-log-index
   :last-log-term :todo-last-log-term})

(defn respond-vote
  [{:keys [from to term granted?] :as message}
   server]
  {:from from
   :to to
   :sent :todo-time-sent
   :deliver :todo-time-deliver
   :term term
   :granted? granted?})

(defn voted-this-term? [server])

(defn vote
  [{:keys [vote-count] :as server}]
  (let [new-count (inc vote-count)]
    (assoc server :vote-count new-count)))

(defn handle-follower
  [request server]
  (let [procedure (first request)
        message (rest request)]
    (case procedure
      :request-vote server #_(respond-vote message server)
      :append-entries server #_(respond-append message server)
      nil (candidate server))))

(defn handle-candidate
  [[procedure message] server]
  (case procedure
    :request-vote (respond-vote message server)
    :append-entries (respond-append message server)))

(defn handle-leader
  [[procedure message] server]
  (case procedure
    :request-vote (respond-vote message server)
    :append-entries (respond-append message server)))

(def inboxes
  (atom {:1 (chan)
         :2 (chan)
         :3 (chan)
         :4 (chan)
         :5 (chan)}))

(defn network-1 []
  {:1 (create [])})

(defn network-2 []
  {:1 (create [:2])
   :2 (create [:1])})

(defn network-5 []
  {:1 (create [:2 :3 :4 :5])
   :2 (create [:1 :3 :4 :5])
   :3 (create [:1 :2 :4 :5])
   :4 (create [:1 :2 :3 :5])
   :5 (create [:1 :2 :3 :4])})

(defn broadcast-append []
  (go (doseq [[id ch] @inboxes]
        (>! ch (respond-append :0 id 0 true 0)))))

(defn get-timeout [] (random-sample 0.5 #{150 300}))

(defn handle [request server]
  (case (:state server)
    :follower (handle-follower request server)
    :candidate (handle-candidate request server)
    :leader (handle-leader request server)))

(defn start [config]
  (go-loop [[id server] config]
    (let [inbox (id @inboxes)
          request (<! (timeout 3000))
          response [id server]
         ; new-server (handle request server)
          ]
      (t/info response)
      (recur response))))

(defn main
  "Create a network configuration and start each server."
  []
  (let [network (network-2)
        c (count network)]
    (doseq [node network]
      (start node))
    (t/info c " Server(s) started.")))

(defn send-messages [messages]
  (for [{:keys [to] :as message} messages]
    (>! (to @inboxes) message)))



(defn product [n1 n2]
  (if (> n2 1)
    (+ n1 (product n1 (dec n2)))
    n1))

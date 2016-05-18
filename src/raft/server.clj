(ns raft.server
  (:require [clojure.core.async
             :as a
             :refer [chan go timeout >! >!!  <! <!!]]
            [taoensso.timbre :as t]))

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

;; TODO Assoc new server state
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
      :request-vote (respond-vote message server)
      :append-entries (respond-append message server)
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

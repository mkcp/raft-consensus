(ns raft.server
  (:require [clojure.core.async
             :as a
             :refer [chan go timeout >! >!!  <! <!!]]))

(defn peer [id]
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
   :peers (if peers
            (mapv peer peers)
            [])
   :log []})

(defn follower [node]
  (assoc node :state :follower))

(defn candidate [node]
  (assoc node :state :candidate))

(defn leader
  [{:keys [commit-index] :as node}]
  ((swap! node
          assoc
          :state :leader
          :next-index (inc commit-index)
          :match-index commit-index ; FIXME Probably bugged, check peers?
          )))

(defn request-vote [from to term granted?]
  {:from from
   :to to
   :sent :todo-time-sent
   :deliver :todo-time-deliver
   :term term
   :last-log-index :todo-last-log-index
   :last-log-term :todo-last-log-term})

(defn respond-vote [from to term granted?]
  {:from from
   :to to
   :sent :todo-time-sent
   :deliver :todo-time-deliver
   :term term
   :granted? granted?})

(defn request-append [from to term]
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

(defn request-vote
  [term id last-log-index last-log-term]
  [:request-vote {:term term
                  :candidate-id id
                  :last-log-index last-log-index
                  :last-log-term last-log-term}])

(defn respond-vote
  [term vote-granted?]
  [:request-vote {:term term
                  :vote-granted vote-granted?}])

(defn vote
  [{:keys [vote-count] :as node}]
  (let [new-count (inc vote-count)]
    (assoc node :vote-count new-count)))

(defn voted-this-term? [node])
(defn get-timeout [] (random-sample 0.5 #{150 300}))
(defn heartbeat [])

;; RPC
(defn handle-rpc [message]
  (let [request (first message)
        args (second message)]
    (case request
      :append-entries (respond-append args)
      :request-vote (respond-vote args))))


;; High level properties that may not be applicable on the node level.
(defn elect-leader [network])
(defn majority-overlap? [config1 config2])
(defn split-vote? [])

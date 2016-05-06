(ns raft.server
  (:require [clojure.core.async
             :as a
             :refer [chan go timeout >! >!!  <! <!!]]))

(defn create []
  {:state :follower
   :current-term 0
   :voted-for nil
   :commit-index 0
   :last-applied 0
   :election-alarm 0
   :inbox (chan)
   :peers []
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

(defn request-append
  [{:keys [from to sent deliver
           term prev-index prev-term
           entries commit-index]
    :as args}]
  [:append-entries args])

(defn respond-append
  [{:keys [term success?] :as args}]
  [:append-entries {:term term
                    :success? success?}])

;; Voting
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

(defn handle-rpc [message]
  (let [request (first message)
        args (second message)]
    (case request
      :append-entries (respond-append args)
      :request-vote (respond-vote args))))

;;;; Leader election
(defn vote
  [{:keys [vote-count] :as node}]
  (let [new-count (inc vote-count)]
    (assoc node :vote-count new-count)))

(defn send-vote-request [network])
(defn voted-this-term? [node])
(defn get-timeout [] (random-sample 0.5 #{150 300}))
(defn heartbeat [])
(defn elect-leader [network])
(defn majority-overlap? [config1 config2])
(defn split-vote? [])

(ns raft.server
  (:require [clojure.core.async
             :as a
             :refer [chan go timeout >! >!!  <! <!!]]))

(defn create
  [peers]
  {:state :follower
   :commit-index 0
   :current-term 0
   :last-applied 0
   :voted-for nil
   :election-alarm 0
   :peers (or peers [])
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
  [{:keys [from to
           sent deliver
           term prev-index
           prev-term entries
           commit-index]
    :as state}]
  [:append-entries state])

(defn respond-append
  [{:keys [term success?]
    :as state}]
  [:append-entries state])

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

(defn init-rpc [{:keys [in out] :as peer}]
  (let [in (chan)
        out (chan)
        to (timeout 150)]
    {:in in
     :out out}))

(defn handle-rpc [message]
  (let [request (first message)
        args (second message)]
    (case request
      :append-entries (respond-append args)
      :request-vote (respond-vote args)))

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

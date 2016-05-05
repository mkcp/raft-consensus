(ns raft.server
  (:require [clojure.core.async
             :as a
             :refer [chan go >! >!!  <! <!!]]))

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
           commit-index]}]
  [:append-entries {:from from
                    :to to
                    :sent nil
                    :deliver nil
                    :term nil
                    :prev-index nil
                    :prev-term nil
                    :entries nil
                    :commit-index nil}])

(defn respond-append
  [term success?]
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

(defn init-rpc [{:keys [in out] :as peer}]
  (let [in (chan)
        out (chan)]
    {:in in
     :out out}))

(defn handle-rpc [message]
  (let [request (first message)
        args (second message)]
    (case request
      :append-entries (respond-append args)
      :request-vote (respond-vote args)))

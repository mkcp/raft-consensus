(ns raft.request-vote
  (:require [clojure.spec :as s]))

(defn higher-term? [local remote] (< local remote))

;; TODO Def spec
(defn request-vote
  [message
   {:keys [id current-term]}]
  [:request-vote {:from id
                  :to (:id message)
                  :term current-term
                  :last-log-index :todo-last-log-index
                  :last-log-term :todo-last-log-term}])

;; TODO Def spec
(defn respond-vote
  "FIXME Build a mechanism to respond to only one server."
  [message
   {:keys [id term voted-for] :as server}]
  (let [granted? (= voted-for (:id message))]
    [:request-vote {:from id
                    :to (:id message)
                    :term term
                    :granted? granted?}]))

(defn voted-this-term?
  "Checks a server if it has voted."
  [{:keys [voted-for]}]
  (not (nil? voted-for)))

(defn assoc-vote
  [{:keys [vote-count] :as server}]
  (assoc server :vote-count (inc vote-count)))

(defn handle [])

(s/def ::request
  {::from keyword?
   ::to keyword?
   ::term integer?
   ::last-log-index integer?
   ::last-log-term integer?})

(s/def ::response
  {::from keyword?
   ::to keyword?
   ::term integer?
   ::granted? boolean?})

(ns raft.append-entries
  (:require [clojure.spec :as s]))

(defn request-append
  [message
   {:keys [id current-term]}]
  (let [body {:from id
              :to (:id message)
              :term current-term
              :prev-index 0
              :prev-term 0
              :entries []
              :commit-index 0}]
    [:append-entries (s/conform ::request body)]))

(defn respond-append
  "FIXME: We need to figure this one out"
  [message server]
  (let [body {:from (:id server)
              :to (:id message)
              :term (:current-term server)
              :success? true
              :match-index 0 ;; ???
              }]
    [:append-entries (s/conform ::response body)]))

(defn handle-append-entries
  [[procedure body]
   {:keys [messages] :as server}]
  (let [add-message #(assoc server :messages (conj messages (% body server)))]))

(s/def ::request
  {:from keyword?
   :to keyword?
   :term integer?
   :prev-index integer?
   :prev-term integer?
   :entries vector?
   :commit-index integer?})

(s/def ::response
  {:from keyword?
   :to keyword?
   :term integer?
   :success? boolean?
   :last-log-term integer?})

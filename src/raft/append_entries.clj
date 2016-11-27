(ns raft.append-entries
  (:require [clojure.spec :as s]
            [clojure.core.async :refer [>!]]
            [taoensso.timbre :as t]))

(s/def ::request
  {:from keyword?
   :to keyword?
   :term integer?
   :prev-index integer?
   :prev-term integer?
   :entries vector?
   :commit-index integer?})

(defn request-append
  [peer {:keys [id current-term]}]
  (let [body {:from id
              :to peer
              :term current-term
              :prev-index 0
              :prev-term 0
              :entries []
              :commit-index 0}]
    [:append-entries (s/conform ::request body)]))

(defn create-requests
  [{:keys [peers] :as node}]
  (map #(request-append % node) peers))

(defn create-response
  "FIXME: We need to figure this one out"
  [message node]
  (let [body {:from (:id node)
              :to (:id message)
              :term (:current-term node)
              :success? true
              :match-index 0 ;; ???
              }]
    [:append-entries (s/conform ::response body)]))

(defn respond-append
  [message node]
  (t/info {:response (create-response)}))

(s/def ::response
  {:from keyword?
   :to keyword?
   :term integer?
   :success? boolean?
   :last-log-term integer?})

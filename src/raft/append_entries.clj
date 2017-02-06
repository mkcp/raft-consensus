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
  "TODO: Validate with a spec so you don't send garbage data."
  [{:keys [peer]}
   {:keys [id current-term]}]
  {:rpc :append-entries
   :from id
   :to peer
   :term current-term
   :prev-index 0
   :prev-term 0
   :entries []
   :commit-index 0})

(defn respond-append
  [message node]
  {:rpc :append-entries
   :from (:id node)
   :to (:id message)
   :term (:current-term node)
   :success? true
   :match-index 0 ;; ???
   })

(s/def ::response
  {:from keyword?
   :to keyword?
   :term integer?
   :success? boolean?
   :last-log-term integer?})


;; FIXME This namespace probably shouldn't be concerned with how many times this function gets called. Try to move the map up to the boundary
(defn create-requests
  [{:keys [peers] :as node}]
  (map #(request-append % node) peers))

(defn create-responses
  [{:keys [peers] :as node}]
  (map #(respond-append % node) peers))

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
  [{:keys [peer]} id current-term]
  (t/debug {:event :request-append
            :peer peer
            :id id})
  {:rpc :append-entries
   :to peer
   :from id
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

(defn create-responses
  [{:keys [peers] :as node}]
  (map #(respond-append % node) peers))

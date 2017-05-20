(ns raft.append-entries
  (:require [clojure.core.async :refer [>!]]))

(def request-spec
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
  (pr-str {:event :request-append
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

(def response-spec
  {:from keyword?
   :to keyword?
   :term integer?
   :success? boolean?
   :last-log-term integer?})

(defn create-responses
  [{:keys [peers] :as node}]
  (map #(respond-append % node) peers))

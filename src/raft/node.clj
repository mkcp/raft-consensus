(ns raft.node
  (:require [raft.request-vote :as r]
            [raft.append-entries :as a]
            [clojure.core.async :refer [chan go go-loop timeout >! >!! <! <!! alts! close!]]
            [taoensso.timbre :as t]))

(defn peer
  "Takes a peer id and create a peer map."
  [id]
  {:peer id
   :next-index 1
   :match-index 0
   :vote-granted false})

(defn create
  "Takes the ID of the node and a collection of peers, returns an atom containing the initialized node map."
  [{:keys [id peers]}]
  {:node (atom {:id id
                :state :follower
                :current-term 0
                :voted-for nil
                :commit-index 0
                :last-applied 0 ;; FIXME, What does this do?
                :peers (mapv peer peers)
                :log []
                :messages []})
   :in (chan)
   :out (chan)})

;; NOTE The peers get passed in at runtime because it would be useful to eventually simulate full and bridged network partitions.
(defn create-1 []
  {:1 (create {:id :1 :peers[]})})

(defn create-2 []
  {:1 (create {:id :1 :peers [:2]})
   :2 (create {:id :2 :peers [:1]})})

(defn create-3 []
  {:1 (create {:id :1 :peers [:2 :3]})
   :2 (create {:id :2 :peers [:1 :3]})
   :3 (create {:id :3 :peers [:1 :2]})})

(defn create-5 []
  {:1 (create {:id :1 :peers [:2 :3 :4 :5]})
   :2 (create {:id :2 :peers [:1 :3 :4 :5]})
   :3 (create {:id :3 :peers [:1 :2 :4 :5]})
   :4 (create {:id :4 :peers [:1 :2 :3 :5]})
   :5 (create {:id :5 :peers [:1 :2 :3 :4]})})

(defn merge
  "Takes an atom containing the node map and merges new-node's changes into it."
  [{:keys [node]} new-node]
  (swap! node merge new-node))

(defn follower [node]
  (assoc node :state :follower))

(defn candidate [node]
  (assoc node :state :candidate))

(defn leader
  [{:keys [commit-index] :as node}]
  (assoc node
         :state :leader
         :next-index (inc commit-index)
         :match-index commit-index ; FIXME What is this? -- old: Probably bugged, check peers?
         ))

(defn leader?
  [state]
  (= state :leader))

(defn add-message
  [f message {:keys [messages] :as node}]
  (assoc node :messages (conj messages (f message node))))

(defn handle
  [[procedure message]
   {:keys [state id] :as node}]
  (case state
    :follower (case procedure
                :request-vote (r/handle message node)
                :append-entries (a/handle-append-entries message node)
                nil (do
                      (t/info {:id (:id node)
                               :message (str "No leader detected. Node " id " becoming candidate.")})
                      (candidate node)))

    :candidate (case procedure
                 :request-vote (r/respond-vote message node)
                 :append-entries (a/respond-append message node))

    :leader (case procedure
              :request-vote (r/respond-vote message node)
              :append-entries (a/request-append message node))) )

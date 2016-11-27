(ns raft.node
  (:require [raft.request-vote :as r]
            [raft.append-entries :as a]
            [taoensso.timbre :as t]))

(defn peer
  "Takes a peer id and create a peer map."
  [id]
  {:peer id
   :next-index 1
   :match-index 0
   :vote-granted false
   :rpc-due 300
   :heartbeat-due 300})

(defn create
  "Creates a server map"
  [id peers]
  {:id id
   :state :follower
   :current-term 0
   :voted-for nil
   :commit-index 0
   :last-applied 0 ;; FIXME, What does this do?
   :peers (mapv peer peers)
   :log []
   :messages []})

(defn create-1 []
  {:1 (create :1 [])})

(defn create-2 []
  {:1 (create :1 [:2])
   :2 (create :2 [:1])})

(defn create-3 []
  {:1 (create :1 [:2 :3])
   :2 (create :2 [:1 :3])
   :3 (create :3 [:1 :2])})

(defn create-5 []
  {:1 (create :1 [:2 :3 :4 :5])
   :2 (create :2 [:1 :3 :4 :5])
   :3 (create :3 [:1 :2 :4 :5])
   :4 (create :4 [:1 :2 :3 :5])
   :5 (create :5 [:1 :2 :3 :4])})

(defn follower [server]
  (assoc server :state :follower))

(defn candidate
  [server]
  (assoc server :state :candidate))

(defn leader
  [{:keys [commit-index] :as server}]
  (assoc server
         :state :leader
         :next-index (inc commit-index)
         :match-index commit-index ; FIXME What is this? -- old: Probably bugged, check peers?
         ))

(defn add-message
  [f message {:keys [messages] :as server}]
  (assoc server :messages (conj messages (f message server))))

(defn handle
  [[procedure message]
   {:keys [state id] :as server}]
  (case state
    :follower (case procedure
                :request-vote (r/handle message server)
                :append-entries (a/handle-append-entries message server)
                nil (do
                      (t/info {:id (:id server)
                               :message (str "No leader detected. Node " id " becoming candidate.")})
                      (candidate server)))
    :candidate (case procedure
                 :request-vote (r/respond-vote message server)
                 :append-entries (a/respond-append message server))
    ;; FIXME This should dispatch append entries asap.
    :leader (case procedure
              :request-vote (r/respond-vote message server)
              :append-entries (a/request-append message server))) )

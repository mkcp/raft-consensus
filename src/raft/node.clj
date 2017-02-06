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
   :in (chan 1000)
   :in-ctrl (chan 100)
   :out (chan 1000)
   :out-ctrl (chan 100)})

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

(defn read-in
  [[procedure message]
   {:keys [state id] :as node}]
  (case state
    :follower (case procedure
                :request-vote (do
                                (t/info {:id id
                                         :message (str "No leader found. Node " id " promoted to candidate.")})
                                node)
                :append-entries (do
                                  (t/info {:id id
                                           :message (str "No leader found. Node " id " promoted to candidate.")})
                                  node)
                nil (do
                      (t/info {:id id
                               :message (str "No leader found. Node " id " promoted to candidate.")})
                      (candidate node)))

    :candidate (case procedure
                 :request-vote (let [event {:id id
                                            :message (str "Election not implemented. Node " id " promoted to leader.")}]
                                 (t/info event)
                                 node)

                 :append-entries (do
                                   (t/info {:id id
                                            :contents message
                                            :message "Candidate received append-entries"})
                                   node)
                 nil (do
                       (leader node)))

    :leader (let [event {:id id
                         :message (str "Leader not implemented. Node " id " demoting to follower.")}]
              (t/info event)
              (follower node))))

(defn write-out
  [[procedure body]
   {:keys [node out] :as server}]
  (let [{:keys [state] :as current-node} @node]
    (t/info "write-out called")
    (case procedure
      :append-entries (do (t/info {:sent :append-entries})
                          current-node)

      :request-vote (do (t/info {:sent :request-vote})
                        current-node)

      ;; FIXME Add :append-entries to node's outbox
      nil (when (leader? state)
            (let [messages (a/create-requests current-node)
                  _ (t/info {:event :append-entries-created :messages messages})
                  messages [{:append-entries [:append-entry {}]}]]
              (doseq [message messages]
                (t/info {:event :append-entries-added :message message})
                (>! out message))
              current-node)))))

(defn send!
  "Takes a message and a network of nodes and pushes the message into the inbox
  of the node referenced in the message's :to field."
  [message network]
  (let [[procedure {:keys [to]}] message
        destination (get-in network [to :in])]
    (>! destination message)))

(defn stop?
  "Takes the channel returned from alts! and the loop's ctrl-chan and checks if the ctrl-chan returned."
  [p ctrl-chan]
  (= p ctrl-chan))

(defn stop
  "Takes a node and pushes a stop signal to its reader and writer ctrl channels."
  [{:keys [in-ctrl out-ctrl]}]
  (>!! in-ctrl :stop)
  (>!! out-ctrl :stop))

(defn random-timeout
  "Takes an int representing the upper ceiling ms and creates a timeout channel between then and 0ms."
  [ms]
  (timeout ms))

;; FIXME: Move network to its own namespace
(defn start
  "Takes the node and creates read and write loops."
  [server election-timeout-ms append-ms]
  (let [{:keys [node in out in-ctrl out-ctrl]} server]

    ;; Read loop
    (go-loop []
      (let [election-timeout (random-timeout election-timeout-ms)
            [message port] (alts! [election-timeout in in-ctrl])]
        (when-not (stop? port in-ctrl)
          (let [new-node (read-in message @node)]
            (swap! node merge new-node))
          (recur))))

    ;; Write loop
    (go-loop []
      (let [append-timeout (random-timeout append-ms)
            [message port] (alts! [append-timeout out out-ctrl])]
        (when-not (stop? port out-ctrl)
          (let [new-node (write-out message server)]
            (swap! node merge new-node))
          (recur))))))

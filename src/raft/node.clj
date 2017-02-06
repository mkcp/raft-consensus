(ns raft.node
  (:require [raft.request-vote :as r]
            [raft.append-entries :as a]
            [clojure.core.async :refer [chan go go-loop timeout >! >!! <! <!! alts! close!]]
            [taoensso.timbre :as t]))

(def node-inbox-buffer 1000)
(def node-outbox-buffer 1000)

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
                :log []})
   :in (chan node-inbox-buffer)
   :out (chan node-outbox-buffer)
   :in-ctrl (chan 100)
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

(defn read-in
  [[procedure message]
   {:keys [state id peers] :as node}]
  (case state
    :follower (case procedure
                :request-vote node ;; FIXME Send back vote reply if you haven't voted yet
                :append-entries node ;; FIXME Commit entries to log
                nil (candidate node) ;; FIXME Send out request-votes to peers
                )

    :candidate (case procedure
                 :request-vote node ;; FIXME This is a noop, candidates vote for themselves in an election
                 :append-entries node ;; FIXME If candidate receives an append-entries with a higher current-term you fold into follower
                 nil (leader node))

    :leader (if-not (empty? peers) ;; FIXME This could be a bad decision -- should a node be leader if it has no peers? Asking for a friend
              node
              (follower node))))

(defn create-request-appends
  "Takes a collection of peers and the current node and creates an :append-entries for each peer."
  [peers {:keys [id current-term]}]
  (mapv #(a/request-append % id current-term) peers))

;; FIXME This function is broken deeply. Think more about the behavior and message flow of each node's inbox and outbox.
;;       you may only be able to fill in this functionality when read-in is more fully implemented. That's ok.
(defn write-out
  [[procedure body] {:keys [node out]}]
  (let [node @node
        {:keys [state peers id current-term]} node]
    (case state
      :leader (case procedure
                :append-entries node ;; FIXME If leader receives an append-entries with a higher current-term you fold into follower
                :request-vote node
                nil (let [messages (create-request-appends peers node)]
                      (doseq [message messages]
                        (t/debug {:event :append-entries-added :message message})
                        (>!! out message))
                      node))

      :candidate (case procedure
                   :request-vote node ;; FIXME Node should create request-votes to all peers when transitioning to candidate
                   :append-entries node
                   :nil node
                   )

      ;; FIXME Check this assumption: A node in follower state sends out no messages.
      :follower (case procedure
                  :request-vote node
                  :append-entries node
                  nil node
                  )
      node)))

(defn send!
  "Takes a message and a network of nodes and pushes the message into the inbox
  of the node referenced in the message's :to field."
  [message network]
  (let [[procedure {:keys [to]}] message
        destination (get-in network [to :in])]
    (>!! destination message)))

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

    ;; Write loop (FIXME Should this ever have to mutate its internal state?)
    (go-loop []
      (let [append-timeout (random-timeout append-ms)
            [message port] (alts! [append-timeout out out-ctrl])]
        (when-not (stop? port out-ctrl)
          (let [new-node (write-out message server)]
            (swap! node merge new-node))
          (recur))))))

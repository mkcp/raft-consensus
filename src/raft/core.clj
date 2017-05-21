(ns raft.core
  (:gen-class)
  (:require [raft [append-entries :as a]
                  [request-vote :as rv]]
            [mount.core :refer [defstate]]
            [clojure.core.async :refer [chan go go-loop timeout >! >!! <! <!! alts! close!]]))

(def node-inbox-buffer 1000)
(def node-outbox-buffer 1000)

(defn uuid [] (str (java.util.UUID/randomUUID)))

;; Start with a register, kv store comes later
(defstate state-machine
  :start (atom))

(defstate log
  :start (atom []))

(defstate node
  :start (atom {:id (uuid)
                :state :inactive
                :current-term 0
                :voted-for false
                :commit-index 0
                :last-applied 0
                :match-index 0
                :peers []}))

(defn random-timeout
  "Takes an int representing the upper ceiling ms and creates a timeout channel between then and 0ms."
  [ms]
  (timeout ms))

#_(defn peer
    "Takes a peer id and create a peer map."
    [id]
    {:peer id
     :vote-granted false})

(defn inactive [node]
  (assoc node :state :inactive))

(defn follower [node]
  (assoc node :state :follower))

(defn candidate [node]
  (assoc node :state :candidate))

(defn leader
  [node]
  (let [commit-index (:commit-index node)]
    (assoc node
           :state :leader
           :next-index (inc commit-index))))

(defn leader?
  [state]
  (= state :leader))

(defn handle-follower [procedure]
  (case procedure
    :request-vote node ;; FIXME Send back vote reply if you haven't voted yet
    :append-entries node ;; FIXME Commit entries to log
    nil (swap! node candidate) ;; FIXME Send out request-votes to peers
    ))
(defn handle-candidate [procedure]
  (case procedure
    :request-vote node ;; FIXME This is a noop, candidates vote for themselves in an election
    :append-entries node ;; FIXME If candidate receives an append-entries with a higher current-term you fold into follower
    nil (swap! node leader)))

(defn handle-leader
  "When the node has no peers, sets inactive."
  [procedure]
  (if-not (empty? (:peers @node))
    (case procedure
      :request-vote @node
      :append-entries @node
      nil (swap! node follower)
      )
    (swap! node inactive)))

(defn create-request-appends
  "Takes a collection of peers and the current node and creates an :append-entries for each peer."
  [peers {:keys [id current-term]}]
  (mapv #(a/request-append % id current-term) peers))

(defn send!
  "Takes a message and writes it to STDOUT"
  [message network])

(defn stop?
  "Takes the channel returned from alts! and the loop's ctrl-chan and checks if the ctrl-chan returned."
  [p ctrl-chan]
  (= p ctrl-chan))

(def node-spec
  {:id integer?
   :state #{:inactive :follower :candidate :leader}
   :current-term integer?
   :voted-for boolean?
   :commit-index integer?
   :last-applied integer? ;; FIXME What does this do? Check the simulation
   :peers map?})

(defn start [opts]
  (let [timeout-ms (:election-timeout-ms opts)
        append-ms  (:append-ms opts)
        in (chan node-inbox-buffer)
        in-ctrl (chan)]

    ;; Election loop
    (go-loop []
      (let [election-timeout (random-timeout timeout-ms)
            [message port] (alts! [election-timeout in in-ctrl])]
        (when-not (stop? port in-ctrl)
          (let [{:keys [state]} @node
                [procedure body] message]
            (case state
              :follower (handle-follower procedure)
              :candidate (handle-candidate procedure)
              :leader (handle-leader procedure)))
          (recur))))

    ;; TODO Entries
    ))

;; TODO Client to accept events from other nodes and apply them to log and StateMachine
(defn -main
  [& args]
  (pr-str "Starting node")
  (while true
    (start {:election-timeout-ms 2000
            :append-ms 500})
    )
  )

;; Got some old code down here
#_(defn node-start
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

#_(defn network-stop [network]
    (doseq [[_ n] network] (node/stop n)))

#_(defn network-start
    "Create a network and starts a loop for each server."
    [network-size]
    (let [network (network/create network-size)
          ids (keys network)]
      (t/info {:nodes ids
               :count (count ids)
               :state :starting})
      (doseq [id ids]
        (node/start (id network) 2000 500))
      network))

;; This function is from when each node had a reader and a writer loop.
#_(defn write-out
    [[procedure body] {:keys [node out]}]
    (let [node @node
          {:keys [state peers id current-term]} node]
      (case state
        :leader (case procedure
                  :append-entries node ;; FIXME If leader receives an append-entries with a higher current-term you fold into follower
                  :request-vote node
                  nil (let [messages (create-request-appends peers node)]
                        (doseq [message messages]
                          (pr-str {:event :append-entries-added :message message})
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


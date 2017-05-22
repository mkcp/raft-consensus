(ns raft.core
  (:gen-class)
  (:require [raft [append-entries :as a]
                  [request-vote :as rv]]
            [mount.core :refer [defstate]]
            [clojure.core.async :refer [chan go go-loop timeout >! >!! <! <!! alts! close!]]
            [cheshire.core :refer [parse-string]]))

(defn uuid [] (str (java.util.UUID/randomUUID)))

(defn pr-err [str]
  (binding [*out* *err*]
    (pr-str str)))

(defstate config
  :start {:timeout-ms 2000
          :append-ms 500})

(defstate state-machine
  :start (atom {}))

(defstate log
  :start (atom []))

(defstate node
  :start (atom {:id nil
                :state :inactive
                :current-term 0
                :voted-for false
                :commit-index 0
                :last-applied 0
                :match-index 0
                :peers []}))

(defstate client
  :start (atom {:node nil
                :next-msg-id 0
                :handlers {}
                :callbacks {}}))

(defstate in-chan
  :start (chan)
  :stop (close! in-chan))

(defn random-timeout
  "Takes an int representing the upper ceiling ms and creates a timeout channel between then and 0ms."
  [ms]
  (timeout ms))

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
      nil (swap! node follower))
    (swap! node inactive)))

(defn create-request-appends
  "Takes a collection of peers and the current node and creates an :append-entries for each peer."
  [peers {:keys [id current-term]}]
  (mapv #(a/request-append % id current-term) peers))

(defn send!
  "Takes a message and writes it to STDOUT"
  [message]
  (pr-err ("Sent " message)))

(defn append [entry]
  (swap! log conj entry))

(defn get-entry [index]
  (nth @log index))

(defn log-last [])
(defn log-last-term [])

(defn majority [n])
(defn median [n])

(def stdin-reader
  (java.io.BufferedReader. *in*))

(defn process-message!
  "Starts a thread to handle incoming messages"
  []
  (go-loop []
    (doseq [line (line-seq stdin-reader)]
      (let [message (parse-string (str line))]
        (pr-err (str "Received " message))
        (>!! in-chan message)))
    (recur))

  (go-loop []
    (let [message (<!! in-chan)]
      (pr-err "Handle message not implemented yet")
      (recur)))
  true)

(defn replicate-log! [force?]
  (pr-err "Replicate log not implemented yet")
  true)

(defn election! []
  (pr-err "Election not implemented yet")
  true)

(defn leader-advance-commit-index! []
  (pr-err "leader-advance-commit-index not implemented yet")
  true)

(defn advance-state-machine! []
  (pr-err "advance-state-machine not implemented yet")
  true)

;; FIXME how to get this to terminate when maelstrom shuts down?
(defn -main
  [& args]
  (pr-err "Starting node")

  (mount.core/start)

  ;; Shamelessly ripped from the demo 乁༼☯‿☯✿༽ㄏ
  (while true
    (or
     (process-message!)
     (replicate-log! false)
     (election!)
     (leader-advance-commit-index!)
     (advance-state-machine!)
     (Thread/sleep 1))))

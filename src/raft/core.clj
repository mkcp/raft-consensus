(ns raft.core
  (:gen-class)
  (:refer-clojure :exclude [boolean? bound-count])
  (:require [raft [append-entries :as a]
                  [request-vote :as rv]]
            [mount.core :refer [defstate]]
            [clojure.core.async :refer [chan go go-loop timeout >! >!! <! <!! alts! close! thread]]
            [cheshire.core :refer [parse-string]]))

(defn uuid [] (str (java.util.UUID/randomUUID)))

(defn pr-err [str]
  (binding [*out* *err*]
    (pr-str str)))

(def config
  {:timeout-ms 2000
   :append-ms 500})

(defn random-timeout
  "Takes an int representing the upper ceiling ms and creates a timeout channel between then and 0ms."
  [ms]
  (timeout ms))

(defn create-request-appends
  "Takes a collection of peers and the current node and creates an :append-entries for each peer."
  [peers {:keys [id current-term]}]
  (mapv #(a/request-append % id current-term) peers))

(defn send!
  "Takes a message and writes it to STDOUT"
  [message]
  (pr-err ("Sent " message)))

;;;;;;;; Log
(def log
  (atom []))

(defn append [entry]
  (swap! log conj entry))

(defn get-entry [index]
  (nth @log index))

(defn log-last [])

(defn log-last-term [])

(defn replicate-log! [force?]
  (pr-err "Replicate log not implemented yet")
  true)

(defn leader-advance-commit-index! []
  (pr-err "leader-advance-commit-index not implemented yet")
  true)

;;;;;;;; State machine
(def state-machine
  (atom {}))

(defn advance-state-machine! []
  (pr-err "advance-state-machine not implemented yet")
  true)

;; Node
(def node
  (atom {:id nil
         :state :inactive
         :current-term 0
         :voted-for false
         :commit-index 0
         :last-applied 0
         :match-index 0
         :peers []}))

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

(defn majority [n])
(defn median [n])

(defn election! []
  (pr-err "Election not implemented yet")
  true)

;;;;;;; Client
;; FIXME
(def client
  (atom {:node nil
         :next-msg-id 0
         :handlers {}
         :callbacks {}}))

(def stdin-reader
  (java.io.BufferedReader. *in*))

(def in-chan (chan))

(defn send-msg!
  "Sends a raw message"
  [msg]
  (pr-err (str "Sent " msg))
  #_(todo write json to stdout with newline))

(defn send! [dest body]
  (let [id (:id @node)]
    (send-msg! {:dest dest :src id :body body})))

(defn reply! [req body]
  (let [reply-to (get-in req [:body :msg-id])]
    (send! (:src req) body)))

(defn handle-init [msg]
  ;; Error out if initing a second itme
  (let [node @node
        body (:body msg)
        id (:id @node)]
    (pr-err "Handling raft init")
    (reply! msg {:type "raft_init_ok"})))

;; FIXME
(defn handle-follower [procedure]
  (case procedure
    :request-vote node
    :append-entries node
    nil (swap! node candidate)
    ))

;; FIXME
(defn handle-candidate [procedure]
  (case procedure
    :request-vote node
    :append-entries node
    nil (swap! node leader)))

;; FIXME
(defn handle-leader
  "When the node has no peers, sets inactive."
  [procedure]
  (if-not (empty? (:peers @node))
    (case procedure
      :request-vote @node
      :append-entries @node
      nil (swap! node follower))
    (swap! node inactive)))

(defn process-message!
  "Starts a thread to handle incoming messages"
  []
  (doseq [line (line-seq stdin-reader)]
    (let [message (parse-string (str line))]
      (pr-err (str "Received " message))))
  true)

;; FIXME how to get this to terminate when maelstrom shuts down?

(defn -main
  [& args]
  (pr-err "Starting node")

  (mount.core/start)

  ;; Shamelessly ripped from the demo 乁༼☯‿☯✿༽ㄏ
  (while true
    (or
     (doseq [line (line-seq stdin-reader)]
       (let [message (parse-string (str line))]
         (pr-err (str "Received " message))))
     #_(replicate-log! false)
     #_(election!)
     #_(leader-advance-commit-index!)
     #_(advance-state-machine!)
     (Thread/sleep 1))))

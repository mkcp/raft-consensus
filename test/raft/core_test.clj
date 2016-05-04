(ns raft.core-test
  (:require [clojure.test :refer :all]
            [raft.core :as raft]))

(deftest node-states
  (testing "Can create follower node"
    (let [node (raft/follower)]
      (is (= (:state node) :follower))))

  (testing "Follower can become candidate"
    (let [node (raft/candidate (raft/follower))]
      (is (= (:state node) :candidate))))

  (testing "Candidate can become leader"
    (let [node (raft/leader (raft/candidate))]
      (is (= (:state node) :leader))))

  (testing "Candidate can become follower"
    (let [node (raft/follower (raft/candidate))]
      (is (= (:state node) :follower)))))

(deftest networking
  (testing "heartbeat"
    (testing "FIXME: Can send heartbeat")
    (testing "FIXME: Can receive heartbeat")))

(deftest log-replication
  (testing "with leader"
    (testing "FIXME: log is append only, no deletions or updates")
    (testing "FIXME: Entries with the same index and term are identical in all entries up to the index")
    (testing "FIXME: A committed entry in a term is present in the logs of leaders for all higher terms")
    (testing "FIXME: The state of an entry at a specific index is guaranteed and across the network")))

(deftest leader-election
  (testing "followers"
    (testing "FIXME: New nodes begin as Follower")
    (testing "FIXME: After timeout, Follower starts election and becomes Candidate"))

  (testing "candidates"
    (testing "FIXME: Candidate remains Candidate if timeout during new election")
    (testing "FIXME: Candidate becomes Leader if majority of votes received")
    (testing "FIXME: Candidate becomes Follower if current leader or new term discovered"))

  (testing "leaders"
    (testing "FIXME: Leader becomes Follower if server with higher term discovered")
    (testing "FIXME: Only one Leader can be elected in a given term")))

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

(deftest log-replication
  (testing "FIXME: System can replicate logs."))

(deftest leader-election
  (testing "FIXME: Nodes have timeout"
    (let [followers [(raft/follower)
                     (raft/follower)
                     (raft/follower)]
          leader (raft/leader)]))

  (testing "FIXME: Can send heartbeat.")

  (testing "FIXME: Can elect leader with 5 nodes"))

(deftest safety)

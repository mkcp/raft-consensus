(ns raft.core-test
  (:require [clojure.test :refer :all]
            [raft.core :as raft]))

(deftest leader-election
  (testing "followers"
    (testing "New nodes begin as Follower")

    (testing "FIXME: After timeout, Follower becomes Candidate and begins election"))

  (testing "candidates"
    (testing "FIXME: Candidate remains Candidate if timeout during new election")
    (testing "FIXME: Candidate becomes Leader if majority of votes received")
    (testing "FIXME: Candidate becomes Follower if current leader or new term discovered"))

  (testing "leaders"
    (testing "FIXME: Leader becomes Follower if server with higher term discovered")
    (testing "FIXME: Only one Leader can be elected in a given term")))

(deftest log-replication
  (testing "leader"
    (testing "can append to log"))

  (testing "follower"
    (testing "can copy log")))

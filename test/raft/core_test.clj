(ns raft.core-test
  (:require [clojure.test :refer :all]
            [raft.core :as raft]))

(deftest leader-election
  (testing "followers"
    (testing "New nodes begin as Follower"
      (let [node (raft/new-node)]
        (is (= (:state node)
               :follower))))

    (testing "FIXME: After timeout, Follower becomes Candidate and begins election"
      (let [node (raft/new-node)]
        (is (= :candidate
               :candidate)))))

  (testing "candidates"
    (testing "FIXME: Candidate remains Candidate if timeout during new election")
    (testing "FIXME: Candidate becomes Leader if majority of votes received")
    (testing "FIXME: Candidate becomes Follower if current leader or new term discovered"))

  (testing "leaders"
    (testing "FIXME: Leader becomes Follower if server with higher term discovered")
    (testing "FIXME: Only one Leader can be elected in a given term")))

(deftest log-replication
  (testing "leader"
    (testing "can append to log"
      (let [node (raft/new-node)]
        (is (= (-> node (raft/append 2) :log first)
               [:append 2])))))

  (testing "follower"
    (testing "can copy log"
      (let [leader (-> (raft/leader) (raft/append 2) :log first)
            follower (raft/follower)]
        (is (= (:log leader)
               (-> follower (raft/append-entries leader) :log)))))))

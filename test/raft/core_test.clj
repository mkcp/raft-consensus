(ns raft.core-test
  (:require [clojure.test :refer :all]
            [raft.core :as raft]))

(deftest consensus
  (testing "Node can be follower."
    (let [node (raft/create-node)]
      (is (= (:state node)
             :follower))))

  (testing "Node can be candidate"
    (let [node (raft/create-node)
          node (raft/make-candidate node)]
      (is (= (:state node)
             :candidate))))

  (testing "Node can be leader"
    (let [node (raft/create-node)
          node (raft/make-leader node)]
      (is (= (:state node)
             :leader))))

  (testing "FIXME: Can send heartbeat.")

  (testing "FIXME: Can replicate log.")

  (testing "FIXME: Can elect leader with 5 nodes"))

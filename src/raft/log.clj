(ns raft.log)

;; There's a lot of research that I still need to do on the log.
;; What index we're on: commit-index?
;; A value called match-index is showing up in AppendEntries responses
;; The requests contain the previous-index and the commit-index, which I believe are the same if they haven't been updated recently.

(defn add [log entry]
  (conj log entry))

(defn commit [log entry]
  (comment (state-change {:state log
                          :compare :uncommitted
                          :swap :committed})))


;; If this is append only then removing an entry probably isn't the way to go.
;; Log compaction, that is, truncating under a certain index, that makes sense.
;;;;;;;;;;;; TODO
(defn match?
  [entry entry2]
  (= (:id entry)
     (:id entry2)))

(defn remove [log entry]
  (filter #(match entry %) log))

(defn remove-test []
  (testing "filters matching entry from log"
    (let [log-pre [{:id :meow
                    :entry [:append-entries {}]}
                   {:id :bark
                    :entry [:append-entries {}]}]

          log-post [{:id :bark
                     :entry [:append-entries {}]}]]

      (is (= (remove log-pre :meow)
             log-post)))))

(defn cas [log entry])
;;;;;;;;;;;;; end TODO

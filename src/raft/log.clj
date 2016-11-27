(ns raft.log)

;; There's a lot of research that I still need to do on the log.
;; What index we're on: commit-index?
;; A value called match-index is showing up in AppendEntries responses
;; The requests contain the previous-index and the commit-index, which I believe are the same if they haven't been updated recently.

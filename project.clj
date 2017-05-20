(defproject raft "0.1.0-SNAPSHOT"
  :description "Toy raft implementation for use with maelstrom"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main raft.core
  :dependencies [[org.clojure/clojure "1.9.0-alpha14"]
                 [org.clojure/core.async "0.2.374"]
                 [cheshire "5.7.0"]
                 [mount "0.1.11"]])

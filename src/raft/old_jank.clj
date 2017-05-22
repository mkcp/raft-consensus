#_(ns raft.old-jank
    "Just some old code that I've moved out of project compilation for now")

#_(defn node-start
    "Takes the node and creates read and write loops."
    [server election-timeout-ms append-ms]
    (let [{:keys [node in out in-ctrl out-ctrl]} server]

      ;; Read loop
      (go-loop []
        (let [election-timeout (random-timeout election-timeout-ms)
              [message port] (alts! [election-timeout in in-ctrl])]
          (when-not (stop? port in-ctrl)
            (let [new-node (read-in message @node)]
              (swap! node merge new-node))
            (recur))))

      ;; Write loop (FIXME Should this ever have to mutate its internal state?)
      (go-loop []
        (let [append-timeout (random-timeout append-ms)
              [message port] (alts! [append-timeout out out-ctrl])]
          (when-not (stop? port out-ctrl)
            (let [new-node (write-out message server)]
              (swap! node merge new-node))
            (recur))))))

#_(defn network-stop [network]
    (doseq [[_ n] network] (node/stop n)))

#_(defn network-start
    "Create a network and starts a loop for each server."
    [network-size]
    (let [network (network/create network-size)
          ids (keys network)]
      (t/info {:nodes ids
               :count (count ids)
               :state :starting})
      (doseq [id ids]
        (node/start (id network) 2000 500))
      network))

;; This function is from when each node had a reader and a writer loop.
#_(defn write-out
    [[procedure body] {:keys [node out]}]
    (let [node @node
          {:keys [state peers id current-term]} node]
      (case state
        :leader (case procedure
                  :append-entries node ;; FIXME If leader receives an append-entries with a higher current-term you fold into follower
                  :request-vote node
                  nil (let [messages (create-request-appends peers node)]
                        (doseq [message messages]
                          (pr-str {:event :append-entries-added :message message})
                          (>!! out message))
                        node))

        :candidate (case procedure
                     :request-vote node ;; FIXME Node should create request-votes to all peers when transitioning to candidate
                     :append-entries node
                     :nil node
                     )

        ;; FIXME Check this assumption: A node in follower state sends out no messages.
        :follower (case procedure
                    :request-vote node
                    :append-entries node
                    nil node
                    )
        node)))

# raft

Playing with an implemention of raft for brain training. This algorithm simulation is incomplete, the section Implementation Status below tracks my current progress.

## Goals

The goals of consensus are outlined here:
https://www.wikiwand.com/en/Consensus_(computer_science)

## The Raft Consensus Algorithm

[Raft](http://raftconsensus.github.io/) is a consensus algorithm that is
designed to be easy to understand. It's equivalent to Paxos in
fault-tolerance and performance. The difference is that it's decomposed
into relatively independent subproblems, and it cleanly addresses all
major pieces needed for practical systems.

### Raft's Semantics

To establish consensus we will ensure a consistent state machine across a cluster of servers. 

We will elect leader for the cluster, by a timeout-to-candidate transition. If a node has not received an update from a leader in the timeout period, it changes state from follower to candidate. With this transition, the node broadcasts a RequestVote message to the entire cluster, incrementing its own election term.

Nodes respond to a RequestVote by granting a vote if they have not yet voted in the latest term. RequestVote messages received after a vote has been granted are responded to with a false.

Once a node has received a majority of votes from the cluster, it transitions from candidate to leader. The leader then periodically broadcasts AppendEntries messages to all nodes in the cluster.


## Implementation status
- [ ] Leader Election

- [ ] Log Replication

- [ ] Safety

- [ ] Log Compaction

## Usage

Currently developing with cider, so running the code involved loading the project up and running a fn in core.

Run `main` and the cluster will startup with the configuration bound in under `network`.

## License

Copyright © 2016 Mikaela Patella

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

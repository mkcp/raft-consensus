# raft

Working on a single process simulation of raft for the purposes of building a generally usable full raft impementation in Clojure. This algorithm is not complete or ready to be used, the section Implementation Status below tracks current progress.

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

- [ ] Committed entries

- [ ] Log Compaction

## Usage

Currently developing with cider, so running the code involved loading the project up and running a fn in core.

Run `main` and the nodes will start with the state bound in `network`.


## Project Goals

I started this project out because I wanted to learn how raft works. Along the way, I learned that one of the reasons leader election is so difficult to implement is because of how difficult it is to simulate. I believe that the messaging layer of raft can be abstracted out such that one could run in a single process, many proccesses, or over a network, so that a consensus-based store can exist in those forms. I'm not really sure of the why yet, I'm just enjoying the challenge of designing it.

## Community goals

This isn't really useful yet so I doubt any kind of communal support organization would exist around it yet. However, if this project were to become useful to others, I would add some technical goals and acceptable design tradeoffs to help organize contributors.

## License

Copyright © 2016 Mikaela Patella

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

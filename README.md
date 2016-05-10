# raft

Playing with an implemention of raft for brain training.

I afford no guarantees about the quality of this software.

## Raft Consensus Algorithm

[Raft](http://raftconsensus.github.io/) is a consensus algorithm that is
designed to be easy to understand. It's equivalent to Paxos in
fault-tolerance and performance. The difference is that it's decomposed
into relatively independent subproblems, and it cleanly addresses all
major pieces needed for practical systems.

## Implementation status
- [ ] Leader Election

- [ ] Log Replication

- [ ] Safety

- [ ] Log Compaction

## Usage

Currently developing with cider, so running the code involved loading the project up and running a fn in core.

If you run start, the nodes in hte network will start up and read the message I've configured in their inboxes. I still need to figure out how to do the timeouts needed for the raft spec, as doing a timeout will block the thread. need to read more about core.async, as alts seems promising.

## License

Copyright © 2016 Mikaela Patella

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

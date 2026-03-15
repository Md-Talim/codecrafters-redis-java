[![progress-banner](https://backend.codecrafters.io/progress/redis/a14b4bba-5598-4db4-90bf-0e60a6bf2499)](https://app.codecrafters.io/users/Md-Talim?r=2qF)

[![CI](https://github.com/Md-Talim/codecrafters-redis-java/actions/workflows/ci.yml/badge.svg?branch=master)](https://github.com/Md-Talim/codecrafters-redis-java/actions/workflows/ci.yml)

# Redis — Built from Scratch in Java

A from-scratch implementation of Redis in Java — RESP protocol, 6 data structures, master-slave replication, pub/sub, transactions, streams, geospatial queries, and RDB persistence. No libraries. No frameworks. Just `java.net` and `java.util.concurrent`.

**83,458 SET/s · 85,645 GET/s** with 50 concurrent clients (500K ops, single-node, JDK 23 virtual threads).

> Ranked **#3 globally** in the Java track on [CodeCrafters](https://app.codecrafters.io/users/Md-Talim).

## Architecture

```
┌────────────────────────────────────────────────────────────────────┐
│                        Redis Server (Main)                         │
│                                                                    │
│  ┌──────────────┐    ┌───────────────┐    ┌─────────────────────┐  │
│  │   Acceptor   │───▶│ Virtual Thread│───▶│   Client Handler    │  │
│  │ (ServerSocket│    │   per conn    │    │  ┌───────────────┐  │  │
│  │   .accept()) │    └───────────────┘    │  │ TrackedInput  │  │  │
│  └──────────────┘          │              │  │ TrackedOutput │  │  │
│                            │              │  │ (BufferedI/O) │  │  │
│                            ▼              │  └───────────────┘  │  │
│                   ┌───────────────┐       └────────┬────────────┘  │
│                   │  Deserializer │                │               │
│                   │  (RESP Parse) │                │               │
│                   └──────┬────────┘                │               │
│                          ▼                         ▼               │
│               ┌────────────────────┐   ┌───────────────────┐       │
│               │  CommandRegistry   │   │   Redis Engine    │       │
│               │  (36 commands)     │──▶│  evaluate(cmd)    │       │
│               └────────────────────┘   └────────┬──────────┘       │
│                                                 │                  │
│          ┌──────────┬───────────┬───────────┬───┴────┐             │
│          ▼          ▼           ▼           ▼        ▼             │
│   ┌──────────┐ ┌────────┐ ┌─────────┐ ┌────────┐ ┌──────┐          │
│   │ Storage  │ │Streams │ │PubSub   │ │Replica │ │Trans-│          │
│   │(Conc.Map)│ │(R/W    │ │Manager  │ │Client  │ │action│          │
│   │+SortedSet│ │ Lock)  │ │(Conc.   │ │(Offset │ │Queue │          │
│   │+CacheExp │ │        │ │ Map+CoW)│ │ Track) │ │      │          │
│   └──────────┘ └────────┘ └─────────┘ └────────┘ └──────┘          │
│        │                                    │                      │
│        ▼                                    ▼                      │
│   ┌──────────┐                     ┌──────────────┐                │
│   │RDB Loader│                     │ Propagation  │                │
│   │(Binary   │                     │ (Fan-out to  │                │
│   │ Parse)   │                     │  replicas)   │                │
│   └──────────┘                     └──────────────┘                │
└────────────────────────────────────────────────────────────────────┘

```

**Network model:** One virtual thread per connection via `Thread.ofVirtual()`. Each client gets its own `TrackedInputStream`/`TrackedOutputStream` (buffered, 8 KB) — tracked byte counts drive replication offset accounting.

**Concurrency strategy:** `ConcurrentHashMap` for the main key-value store. `ReadWriteLock` + `Condition` variables for streams (blocking `XREAD`). `ReentrantLock` + `Condition` for `BLPOP` blocking. `CopyOnWriteArraySet` for pub/sub subscriber lists. No global lock on the hot path.

## 36 Implemented Commands

| Category         | Commands                                                  |
| ---------------- | --------------------------------------------------------- |
| **Strings**      | `GET` · `SET` (with PX expiry) · `INCR`                   |
| **Lists**        | `LPUSH` · `RPUSH` · `LPOP` · `BLPOP` · `LRANGE` · `LLEN`  |
| **Sorted Sets**  | `ZADD` · `ZRANGE` · `ZRANK` · `ZCARD` · `ZSCORE` · `ZREM` |
| **Streams**      | `XADD` · `XRANGE` · `XREAD` (blocking)                    |
| **Geospatial**   | `GEOADD` · `GEOPOS` · `GEODIST` · `GEOSEARCH`             |
| **Pub/Sub**      | `SUBSCRIBE` · `UNSUBSCRIBE` · `PUBLISH`                   |
| **Transactions** | `MULTI` · `EXEC` · `DISCARD`                              |
| **Server**       | `PING` · `ECHO` · `CONFIG GET` · `INFO` · `KEYS` · `TYPE` |
| **Replication**  | `PSYNC` · `REPLCONF` · `WAIT`                             |

## Performance

Benchmarked on Intel Core i3-1115G4 (2C/4T, 4.1 GHz boost), 12 GB RAM, Ubuntu, JDK 23.

```

$ redis-benchmark -h 127.0.0.1 -p 6380 -t set,get -n 500000 -c 50 -q

SET: 83,998 req/s p50 = 0.303 ms
GET: 84,495 req/s p50 = 0.295 ms

```

| Command | This Project | Redis 7.x (same machine) | Ratio |
| ------- | ------------ | ------------------------ | ----- |
| **SET** | 83,998 ops/s | 133,244 ops/s            | 63.0% |
| **GET** | 84,495 ops/s | 132,978 ops/s            | 63.5% |

**Profiling-driven optimization.** Initial throughput was ~1,200 ops/s. Used async-profiler flame graphs to identify:

- **Unbuffered I/O** on socket streams → added 8 KB `BufferedInputStream`/`BufferedOutputStream` wrappers (**~70× speedup**)
- **`ByteArrayOutputStream` allocations** in RESP serialization → replaced with pre-sized `byte[]` + `System.arraycopy` (**reduced GC pressure**)
- **String concatenation** in protocol encoding → eliminated intermediate `String` objects

Flame graphs: [`before`](benchmarks/before/cpu_flamegraph.png) · [`after`](benchmarks/after/cpu_flamegraph.png)

## Key Engineering Decisions

### RESP Protocol — Zero-dependency wire protocol

Implemented the full [Redis Serialization Protocol](https://redis.io/docs/reference/protocol-spec/): Simple Strings, Errors, Integers, Bulk Strings, and Arrays. The `Deserializer` is a hand-written recursive-descent parser over raw `InputStream` bytes — no tokenizer, no regex, no library.

### Geospatial — Geohash encoding with Haversine distance

`GEOADD` encodes (longitude, latitude) into a 52-bit geohash via bit-interleaving. `GEODIST` decodes hashes back to coordinates and computes great-circle distance using the Haversine formula. `GEOSEARCH` does radius filtering with `BYRADIUS`.

### Sorted Sets — Dual-index structure

`TreeMap<Double, TreeSet<String>>` for score-ordered iteration + `HashMap<String, Double>` for O(1) member→score lookups. Handles duplicate scores correctly via per-score buckets.

### Streams — ReadWriteLock with Condition-based blocking

`XREAD BLOCK` uses `writeLock().newCondition()` — writers signal waiting readers on new entries. Auto-incrementing sequence numbers with millisecond-timestamp prefixes, matching Redis's `<ms>-<seq>` ID format.

### Replication — Offset-tracked command propagation

Master tracks a global `AtomicLong` replication offset. On `PSYNC`, the master sends an RDB snapshot, then streams every mutating command to replicas via a `BlockingQueue<CommandResponse>`. `WAIT` blocks until N replicas acknowledge up to the current offset.

### RDB Persistence — Binary format parser

Parses Redis's RDB v11 binary format: length-encoded strings, integer encodings (8/16/32-bit), key expiration timestamps (seconds and milliseconds), and auxiliary metadata fields.

### Transactions — Command queue with atomicity

`MULTI` begins buffering; commands return `QUEUED` instead of executing. `EXEC` replays the queue sequentially. `DISCARD` drops it. The `Command.isQueueable()` flag prevents `MULTI`/`EXEC`/`DISCARD` from being queued themselves.

### Key Expiration — Lazy eviction

`CacheEntry<T>` wraps every value with an `until` timestamp. Expiration is checked lazily on access (`isExpired()` → remove), avoiding background timer overhead.

## Quick Start

```sh
# Build
git clone https://github.com/Md-Talim/codecrafters-redis-java.git
cd codecrafters-redis-java
mvn compile

# Run (default port 6380)
./my_redis.sh

# Run on custom port with RDB persistence
./my_redis.sh --port 6381 --dir /tmp --dbfilename dump.rdb

# Run as replica
./my_redis.sh --port 6382 --replicaof "localhost 6381"
```

Connect with any standard Redis client:

```sh
redis-cli -p 6380
```

### Tests & CI

```sh
mvn test
```

CI runs the same test suite on every push/PR via GitHub Actions (see `.github/workflows/ci.yml`).

### Docker — One-Command Replication Demo

Spin up a master with two replicas:

```sh
docker compose up --build
```

This starts three containers on a shared network:

| Service    | Host Port | Role    |
| ---------- | --------- | ------- |
| `master`   | 6380      | Master  |
| `replica1` | 6381      | Replica |
| `replica2` | 6382      | Replica |

```sh
redis-cli -p 6380 SET hello world    # write to master
redis-cli -p 6381 GET hello          # read from replica → "world"
redis-cli -p 6382 GET hello          # read from replica → "world"
redis-cli -p 6380 WAIT 2 5000       # confirm 2 replicas acknowledged → (integer) 2
```

```sh
docker compose down                  # tear down
```

## Project Structure

```
src/main/java/redis/
├── Main.java                        # Entry point, arg parsing, server loop
├── Redis.java                       # Core engine: command dispatch, replication, blocking ops
├── client/
│   ├── Client.java                  # Per-connection handler (virtual thread)
│   └── ReplicaClient.java           # Replica handshake + command replay
├── command/
│   ├── Command.java                 # Interface: execute(client, args) → response
│   ├── CommandRegistry.java         # Maps command names → implementations
│   ├── core/                        # GET, SET, INCR, PING, ECHO, CONFIG, INFO, KEYS, TYPE
│   ├── list/                        # LPUSH, RPUSH, LPOP, BLPOP, LRANGE, LLEN
│   ├── sortedset/                   # ZADD, ZRANGE, ZRANK, ZCARD, ZSCORE, ZREM
│   ├── stream/                      # XADD, XRANGE, XREAD
│   ├── geospatial/                  # GEOADD, GEOPOS, GEODIST, GEOSEARCH
│   ├── pubsub/                      # SUBSCRIBE, UNSUBSCRIBE, PUBLISH
│   ├── replication/                 # PSYNC, REPLCONF, WAIT
│   └── transaction/                 # MULTI, EXEC, DISCARD
├── configuration/                   # CLI arg parsing (--port, --dir, --replicaof)
├── pubsub/PubSubManager.java        # Channel → subscribers, message fan-out
├── rdb/RDBLoader.java               # RDB v11 binary format parser
├── resp/                            # RESP protocol: Deserializer + type hierarchy
│   ├── Deserializer.java            # Recursive-descent RESP parser
│   └── type/                        # RValue, BulkString, SimpleString, RArray, etc.
├── store/
│   ├── Storage.java                 # ConcurrentHashMap + sorted set store
│   ├── CacheEntry.java              # Value wrapper with lazy expiration
│   └── SortedSet.java               # TreeMap + HashMap dual-index
├── stream/                          # Stream entries, ID generation, range queries
│   ├── Stream.java                  # Core stream with R/W lock + blocking reads
│   └── identifier/                  # Millisecond, Unique, Wildcard ID types
└── util/
    ├── TrackedInputStream.java      # Buffered + byte-counting input
    └── TrackedOutputStream.java     # Buffered + byte-counting output
```

## Acknowledgments

- Built as part of the [CodeCrafters "Build Your Own Redis" challenge](https://codecrafters.io/challenges/redis)
- Replication implementation informed by studying [Enzo Caceres' approach](https://github.com/Caceresenzo/codecrafters--build-your-own-redis--java/)

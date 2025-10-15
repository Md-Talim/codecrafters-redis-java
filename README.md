[![progress-banner](https://backend.codecrafters.io/progress/redis/a14b4bba-5598-4db4-90bf-0e60a6bf2499)](https://app.codecrafters.io/users/Md-Talim?r=2qF)

# Redis Server Implementation in Java

This project is a Java implementation of a Redis server, built as part of the [Codecrafters "Build Your Own Redis" challenge](https://codecrafters.io/challenges/redis). It demonstrates key concepts in building distributed systems, network protocols, and in-memory databases.

## What This Project Does

This Redis implementation covers the fundamental components of a modern in-memory data store:

- **RESP Protocol:** Complete implementation of the Redis Serialization Protocol for client-server communication.
- **Multi-threaded Server:** Uses Java's virtual threads to handle concurrent client connections efficiently.
- **Data Structures:** Support for strings, lists, sorted sets, streams, and geospatial data with Redis-compatible operations.
- **Persistence:** RDB file loading and parsing for data durability.
- **Replication:** Master-slave replication with automatic synchronization.
- **Pub/Sub Messaging:** Publisher-subscriber pattern for real-time messaging.
- **Transactions:** ACID-compliant transaction support with MULTI/EXEC/DISCARD.
- **Expiration:** Time-based key expiration with automatic cleanup.
- **Geospatial Operations:** Location-based queries and distance calculations using geographical coordinates.

## Key Features

### Core Data Operations

- **String Operations:** `GET`, `SET`, `INCR` with optional expiration times
- **Configuration:** `CONFIG GET` for server configuration retrieval
- **Key Management:** `KEYS`, `TYPE` commands for key introspection
- **Server Info:** `INFO`, `PING`, `ECHO` for server status and diagnostics

### Advanced Data Structures

#### Lists

- **Push Operations:** `LPUSH`, `RPUSH` for adding elements
- **Pop Operations:** `LPOP`, `BLPOP` (blocking) for removing elements
- **Range Operations:** `LRANGE` for retrieving ranges of elements
- **Metadata:** `LLEN` for list length information

#### Sorted Sets

- **Insertion:** `ZADD` for adding scored members
- **Ranking:** `ZRANK` for member position queries
- **Range Queries:** `ZRANGE` for retrieving members by rank
- **Metadata:** `ZCARD`, `ZSCORE` for set information
- **Removal:** `ZREM` for removing members

#### Streams

- **Stream Creation:** `XADD` for adding entries with unique IDs
- **Range Queries:** `XRANGE` for retrieving entries by ID range
- **Blocking Reads:** `XREAD` with optional blocking for real-time consumption

#### Geospatial Data

- **Location Storage:** `GEOADD` for storing geographical coordinates with location names
- **Position Retrieval:** `GEOPOS` for retrieving coordinates of stored locations
- **Distance Calculation:** `GEODIST` for calculating distances between two locations
- **Proximity Search:** `GEOSEARCH` for finding locations within a specified radius

### Messaging & Communication

- **Pub/Sub:** `SUBSCRIBE`, `UNSUBSCRIBE`, `PUBLISH` for message broadcasting
- **Transactions:** `MULTI`, `EXEC`, `DISCARD` for atomic command execution
- **Replication:** `PSYNC`, `REPLCONF`, `WAIT` for master-slave synchronization

### System Features

- **RDB Loading:** Automatic loading of Redis Database files on startup
- **Key Expiration:** Automatic cleanup of expired keys with millisecond precision
- **Blocking Operations:** Non-blocking server design with blocking client operations
- **Error Handling:** Comprehensive error reporting following Redis conventions

## Architecture Overview

### Network Layer

The server uses Java's virtual threads to handle client connections concurrently. Each client connection runs in its own virtual thread, allowing for high concurrency with minimal resource overhead.

### Command Processing

Commands are processed using the Command pattern, with each Redis command implemented as a separate class that implements the `Command` interface.

### Storage Engine

The storage layer provides a thread-safe, concurrent map with support for:

- Key-value storage with expiration
- Type-specific operations (lists, sorted sets, streams)
- Atomic operations and transactions
- Geospatial data encoding and querying

### RESP Protocol

Complete implementation of the Redis Serialization Protocol (RESP) for:

- Parsing client requests
- Serializing server responses
- Handling different data types (strings, arrays, integers, errors)

## How It Works Internally

The Redis server processes client requests through several stages:

1. **Connection Handling:** When a client connects, the server creates a new `Client` instance running in a virtual thread.

2. **Request Parsing:** The `Deserializer` class parses incoming RESP-formatted requests into `RValue` objects representing the command and arguments.

3. **Command Routing:** The `Redis` class routes parsed commands to the appropriate `Command` implementation based on the command name.

4. **Command Execution:** Each command class implements the `Command` interface with an `execute` method that:
    - Validates arguments
    - Performs the requested operation
    - Returns a properly formatted response

5. **Response Serialization:** Results are serialized back to RESP format and sent to the client.

6. **State Management:** The server maintains:
    - **Storage:** Thread-safe key-value store with expiration
    - **Replication:** List of connected replica servers
    - **Pub/Sub:** Channel subscriptions and message routing
    - **Transactions:** Queued commands for atomic execution

## How to Set Up and Run

### Prerequisites

- Java 21 or later (for virtual thread support)
- Maven 3.6+ for dependency management

### Installation

1. Clone the repository:

    ```sh
    git clone https://github.com/Md-Talim/codecrafters-redis-java.git
    cd codecrafters-redis-java
    ```

2. Build the project:
    ```sh
    mvn compile
    ```

### Running the Server

**Basic Usage:**

```sh
./your_program.sh
```

**With Configuration Options:**

```sh
./your_program.sh --port 6380 --dir /tmp --dbfilename dump.rdb
```

**As a Replica Server:**

```sh
./your_program.sh --port 6380 --replicaof "localhost 6379"
```

**Available Configuration Options:**

| Option         | Description                   | Default | Example                        |
| :------------- | :---------------------------- | :------ | :----------------------------- |
| `--port`       | Port number for the server    | 6379    | `--port 6380`                  |
| `--dir`        | Directory for RDB files       | -       | `--dir /tmp`                   |
| `--dbfilename` | RDB filename                  | -       | `--dbfilename dump.rdb`        |
| `--replicaof`  | Master server for replication | -       | `--replicaof "localhost 6379"` |

### Testing with Redis CLI

Once the server is running, you can connect using the standard Redis CLI:

```sh
redis-cli -p 6379
```

### Example Usage

```redis
# Basic string operations
127.0.0.1:6379> SET mykey "Hello, Redis!"
OK
127.0.0.1:6379> GET mykey
"Hello, Redis!"

# List operations
127.0.0.1:6379> LPUSH mylist "world" "hello"
(integer) 2
127.0.0.1:6379> LRANGE mylist 0 -1
1) "hello"
2) "world"

# Sorted set operations
127.0.0.1:6379> ZADD myset 1 "one" 2 "two" 3 "three"
(integer) 3
127.0.0.1:6379> ZRANGE myset 0 -1
1) "one"
2) "two"
3) "three"

# Stream operations
127.0.0.1:6379> XADD mystream * field1 value1 field2 value2
"1234567890-0"
127.0.0.1:6379> XRANGE mystream - +
1) 1) "1234567890-0"
   2) 1) "field1"
      2) "value1"
      3) "field2"
      4) "value2"

# Geospatial operations
127.0.0.1:6379> GEOADD locations -122.4194 37.7749 "San Francisco"
(integer) 1
127.0.0.1:6379> GEOADD locations -74.0060 40.7128 "New York"
(integer) 1
127.0.0.1:6379> GEODIST locations "San Francisco" "New York"
"4129554.895234"
127.0.0.1:6379> GEOPOS locations "San Francisco" "New York"
1) 1) "-122.419403"
   2) "37.774900"
2) 1) "-74.006000"
   2) "40.712800"
127.0.0.1:6379> GEOSEARCH locations FROMLONLAT -122.4 37.8 BYRADIUS 50 km
1) "San Francisco"

# Pub/Sub messaging
127.0.0.1:6379> SUBSCRIBE news
Reading messages... (press Ctrl-C to quit)

# In another terminal:
127.0.0.1:6379> PUBLISH news "Hello subscribers!"
(integer) 1

# Transactions
127.0.0.1:6379> MULTI
OK
127.0.0.1:6379> SET key1 "value1"
QUEUED
127.0.0.1:6379> SET key2 "value2"
QUEUED
127.0.0.1:6379> EXEC
1) OK
2) OK
```

## Project Structure

```
redis-java/
├── src/main/java/redis/
│   ├── Main.java                    # Application entry point
│   ├── Redis.java                   # Core server orchestration
│   ├── client/                      # Client connection handling
│   │   ├── Client.java              # Individual client management
│   │   └── ReplicaClient.java       # Replica server client
│   ├── command/                     # Redis command implementations
│   │   ├── Command.java             # Command interface
│   │   ├── CommandRegistry.java     # Command registration
│   │   ├── core/                    # Basic commands (GET, SET, etc.)
│   │   ├── list/                    # List operations (LPUSH, RPUSH, etc.)
│   │   ├── sortedset/              # Sorted set operations (ZADD, ZRANGE, etc.)
│   │   ├── stream/                 # Stream operations (XADD, XREAD, etc.)
│   │   ├── geospatial/             # Geospatial commands (GEOADD, GEODIST, etc.)
│   │   ├── pubsub/                 # Pub/Sub commands
│   │   ├── replication/            # Replication commands
│   │   └── transaction/            # Transaction commands
│   ├── configuration/              # Server configuration management
│   ├── pubsub/                     # Pub/Sub message handling
│   ├── rdb/                        # RDB file parsing and loading
│   ├── resp/                       # RESP protocol implementation
│   ├── store/                      # Storage engine and data structures
│   ├── stream/                     # Stream data structure implementation
│   └── util/                       # Utility classes (tracked I/O streams)
├── your_program.sh                 # Server startup script
└── README.md                       # This file
```

## Implemented Redis Commands

### String Commands

- `GET key` - Get value of key
- `SET key value [PX milliseconds]` - Set key to value with optional expiration
- `INCR key` - Increment integer value of key

### List Commands

- `LPUSH key element...` - Prepend elements to list
- `RPUSH key element...` - Append elements to list
- `LPOP key [count]` - Remove and return first element(s)
- `BLPOP key timeout` - Blocking version of LPOP
- `LRANGE key start stop` - Get range of elements
- `LLEN key` - Get list length

### Sorted Set Commands

- `ZADD key score member` - Add member with score
- `ZRANGE key start stop` - Get members by rank range
- `ZRANK key member` - Get rank of member
- `ZCARD key` - Get number of members
- `ZSCORE key member` - Get score of member
- `ZREM key member` - Remove member

### Stream Commands

- `XADD key id field value...` - Add entry to stream
- `XRANGE key start end` - Get entries by ID range
- `XREAD [BLOCK milliseconds] STREAMS key... id...` - Read from streams

### Geospatial Commands

- `GEOADD key longitude latitude member` - Add geographic location to sorted set
- `GEOPOS key member [member...]` - Get longitude and latitude of members
- `GEODIST key member1 member2` - Calculate distance between two members in meters
- `GEOSEARCH key FROMLONLAT longitude latitude BYRADIUS radius unit` - Search for members within radius

### Pub/Sub Commands

- `SUBSCRIBE channel...` - Subscribe to channels
- `UNSUBSCRIBE [channel...]` - Unsubscribe from channels
- `PUBLISH channel message` - Publish message to channel

### Transaction Commands

- `MULTI` - Start transaction
- `EXEC` - Execute queued commands
- `DISCARD` - Discard queued commands

### Server Commands

- `PING` - Test server connectivity
- `ECHO message` - Echo message back
- `CONFIG GET parameter` - Get configuration parameter
- `INFO` - Get server information
- `KEYS pattern` - Get keys matching pattern
- `TYPE key` - Get type of key

### Replication Commands

- `PSYNC replicationid offset` - Synchronize with master
- `REPLCONF option value` - Configure replication
- `WAIT numreplicas timeout` - Wait for replica acknowledgments

## Technical Challenges & Solutions

### Concurrency & Thread Safety

- **Challenge:** Handle thousands of concurrent connections efficiently.
- **Solution:** Used Java 21's virtual threads for lightweight concurrency and `ConcurrentHashMap` for thread-safe storage.

### RESP Protocol Implementation

- **Challenge:** Parse and serialize Redis protocol messages correctly.
- **Solution:** Implemented a robust `Deserializer` and `RValue` hierarchy with proper serialization methods.

### Blocking Operations

- **Challenge:** Implement blocking commands like `BLPOP` without blocking the server.
- **Solution:** Used `ReentrantLock` with `Condition` variables for efficient waiting and notification.

### Memory Management

- **Challenge:** Automatic expiration of keys without memory leaks.
- **Solution:** Implemented lazy expiration checking during key access with `CacheEntry` wrapper objects.

### Replication Consistency

- **Challenge:** Keep replica servers synchronized with master state.
- **Solution:** Implemented command propagation with offset tracking and acknowledgment waiting. This was one of the most complex parts of the project, and I benefited from studying [Enzo Caceres' implementation](https://github.com/Caceresenzo/codecrafters--build-your-own-redis--java/) to understand the intricacies of the PSYNC protocol and replica management.

### Stream Data Structure

- **Challenge:** Implement Redis streams with unique ID generation and range queries.
- **Solution:** Created custom `Stream` class with proper ID comparison and concurrent read/write operations.

## Performance Characteristics

- **Throughput:** Capable of handling thousands of concurrent connections using virtual threads
- **Memory:** Efficient memory usage with lazy expiration and concurrent data structures
- **Latency:** Low-latency responses through optimized command routing and minimal allocations
- **Scalability:** Master-slave replication support for horizontal scaling
- **Geospatial Performance:** Efficient geospatial queries using geohash encoding and optimized distance calculations

## Testing

The implementation has been tested against the CodeCrafters test suite, which validates:

- Protocol compliance with official Redis specification
- Correct behavior for all implemented commands
- Proper error handling and edge cases
- Replication functionality
- Transaction atomicity
- Pub/Sub message delivery
- Geospatial accuracy and performance

## Acknowledgments

- This project implements the Redis server as described in the [CodeCrafters "Build Your Own Redis" challenge](https://codecrafters.io/challenges/redis)
- Thanks to the Redis team for creating such an elegant and well-documented protocol

## Learning Resources

- [Redis Protocol Specification](https://redis.io/docs/reference/protocol-spec/)
- [Redis Commands Reference](https://redis.io/commands/)
- [CodeCrafters Redis Challenge](https://codecrafters.io/challenges/redis)
- [Java Virtual Threads Documentation](https://openjdk.org/jeps/444)
- [Redis Geospatial Commands](https://redis.io/commands/?group=geo)
- [Geohash Algorithm](https://en.wikipedia.org/wiki/Geohash)
- [Haversine Formula](https://en.wikipedia.org/wiki/Haversine_formula)

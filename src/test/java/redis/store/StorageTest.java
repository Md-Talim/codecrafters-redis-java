package redis.store;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import redis.resp.type.BulkString;

class StorageTest {

    private Storage storage;

    @BeforeEach
    void setUp() {
        storage = new Storage();
    }

    @Test
    void setAndGet() {
        storage.set("key", new BulkString("value"));

        assertThat(storage.get("key")).isNotNull();
        assertThat(storage.get("key").toString()).isEqualTo("value");
    }

    @Test
    void getMissingKeyReturnsNull() {
        assertThat(storage.get("nonexistent")).isNull();
    }

    @Test
    void expiredKeyReturnsNull() throws InterruptedException {
        storage.set("key", new BulkString("value"), 50);

        // Should exist immediately
        assertThat(storage.get("key")).isNotNull();

        // Wait for expiratoin
        Thread.sleep(100);

        // Should be gone (lazy eviction on read)
        assertThat(storage.get("key")).isNull();
    }

    @Test
    void keysReturnsAllKeys() {
        storage.set("a", new BulkString("1"));
        storage.set("b", new BulkString("2"));
        storage.set("c", new BulkString("3"));

        assertThat(storage.keys()).containsExactlyInAnyOrder("a", "b", "c");
    }
}

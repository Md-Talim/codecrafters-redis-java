package redis.store;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SortedSetTest {

    private SortedSet set;

    @BeforeEach
    void setUp() {
        set = new SortedSet();
    }

    @Test
    void addReturnsTrueForNewMember() {
        assertThat(set.add("alice", 1.0)).isTrue();
    }

    @Test
    void addReturnsFalseForExistingMember() {
        assertThat(set.add("alice", 1.0)).isTrue();
        assertThat(set.add("alice", 1.0)).isFalse();
    }

    @Test
    void addReturnsFalseWhenUpdatingExistingMemberScore() {
        set.add("alice", 1.0);
        assertThat(set.add("alice", 2.0)).isFalse();
    }

    @Test
    void rankReflectsScoreOrder() {
        set.add("alice", 3.0);
        set.add("bob", 1.0);
        set.add("charlie", 2.0);

        assertThat(set.rank("bob")).isEqualTo(0);
        assertThat(set.rank("charlie")).isEqualTo(1);
        assertThat(set.rank("alice")).isEqualTo(2);
    }

    @Test
    void rankReturnsNegativeOneForMissing() {
        assertThat(set.rank("ghost")).isEqualTo(-1);
    }

    @Test
    void sizeTracksCorrectly() {
        assertThat(set.size()).isEqualTo(0);
        set.add("a", 1.0);
        set.add("b", 2.0);
        assertThat(set.size()).isEqualTo(2);
    }

    @Test
    void removeDeletesMember() {
        set.add("alice", 1.0);
        assertThat(set.remove("alice")).isTrue();
        assertThat(set.rank("alice")).isEqualTo(-1);
        assertThat(set.size()).isEqualTo(0);
    }

    @Test
    void removeMissingMemberReturnsFalse() {
        assertThat(set.remove("nonexistent")).isFalse();
    }

    @Test
    void getScoreReturnsCorrectValue() {
        set.add("alice", 42.5);
        assertThat(set.getScore("alice")).isEqualTo(42.5);
        assertThat(set.getScore("nobody")).isNull();
    }
}

package redis.store;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class SortedSet {

    // Member -> Member score for O(1) member lookups
    private final Map<String, Double> memberScores = new HashMap<>();

    // Score -> Set of members (handles duplicate scores correctly)
    private final TreeMap<Double, Set<String>> scoreToMembers = new TreeMap<>();

    public boolean add(String member, double score) {
        Double oldScore = memberScores.get(member);

        if (oldScore != null) {
            if (oldScore.equals(score)) {
                return false; // No change needed
            }

            // Remove from old score bucket
            Set<String> oldBucket = scoreToMembers.get(oldScore);
            oldBucket.remove(member);
            if (oldBucket.isEmpty()) {
                scoreToMembers.remove(oldScore);
            }
        }

        memberScores.put(member, score);
        scoreToMembers
            .computeIfAbsent(score, _ -> new LinkedHashSet<>())
            .add(member);

        return oldScore == null; // true if new member added
    }
}

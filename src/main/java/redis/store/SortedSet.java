package redis.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import redis.resp.type.BulkString;
import redis.resp.type.RValue;

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
        scoreToMembers.computeIfAbsent(score, _ -> new TreeSet<>()).add(member);

        System.out.println(memberScores);
        System.out.println(scoreToMembers);

        return oldScore == null; // true if new member added
    }

    public int rank(String member) {
        Double targetScore = memberScores.get(member);
        if (targetScore == null) {
            return -1;
        }

        int rank = 0;
        for (var score : scoreToMembers.keySet()) {
            var scoreMembers = scoreToMembers.get(score);
            if (score.equals(targetScore)) {
                for (String scoreMember : scoreMembers) {
                    if (scoreMember.equals(member)) return rank;
                    rank++;
                }
            } else {
                rank += scoreMembers.size();
            }
        }

        return -1;
    }

    public List<RValue> getMembers() {
        List<RValue> members = new ArrayList<>();

        for (var score : scoreToMembers.keySet()) {
            var scoreMembers = scoreToMembers.get(score);

            for (String scoreMember : scoreMembers) {
                members.add(new BulkString(scoreMember));
            }
        }

        return members;
    }

    public List<RValue> getRange(int start, int stop) {
        List<RValue> result = new ArrayList<>();

        int currentIndex = 0;
        int targetSize = stop - start;

        for (var score : scoreToMembers.keySet()) {
            var scoreMembers = scoreToMembers.get(score);

            for (String member : scoreMembers) {
                if (currentIndex >= start && result.size() < targetSize) {
                    result.add(new BulkString(member));
                }

                currentIndex++;

                if (result.size() >= targetSize) {
                    return result;
                }
            }
        }

        return result;
    }

    public int size() {
        return memberScores.size();
    }

    public Double getScore(String member) {
        return memberScores.get(member);
    }
}

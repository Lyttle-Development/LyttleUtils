package com.lyttledev.lyttleutils.types.Message;

import java.util.ArrayList;
import java.util.List;

public class Replacements {
    private final List<ReplacementEntry> entries;

    private Replacements(List<ReplacementEntry> entries) {
        this.entries = entries;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<ReplacementEntry> entries = new ArrayList<>();

        public Builder add(String key, String value) {
            entries.add(new ReplacementEntry(new ReplacementsKey(key), value));
            return this;
        }

        public Replacements build() {
            return new Replacements(entries);
        }
    }

    public static Replacements fromStringPairs(String[][] pairs) {
        ReplacementEntry[] entries = new ReplacementEntry[pairs.length];
        for (int i = 0; i < pairs.length; i++) {
            String rawKey = pairs[i][0];
            String value = pairs[i][1];
            entries[i] = new ReplacementEntry(new ReplacementsKey(rawKey), value);
        }
        return new Replacements(List.of(entries));
    }

    public List<ReplacementEntry> getAll() {
        return entries;
    }

    public String getValueForKey(String rawKey) {
        ReplacementsKey key = new ReplacementsKey(rawKey);
        for (ReplacementEntry entry : entries) {
            if (entry.getKey().getValue().equals(key.getValue())) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (ReplacementEntry entry : entries) {
            sb.append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }
}

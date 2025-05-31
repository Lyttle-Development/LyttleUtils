package com.lyttledev.lyttleutils.types.Message;

public class ReplacementEntry {
    private final ReplacementsKey key;
    private final String value;

    public ReplacementEntry(ReplacementsKey key, String value) {
        this.key = key;
        this.value = value;
    }

    public ReplacementsKey getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}

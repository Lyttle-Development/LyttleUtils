package com.lyttledev.lyttleutils.types.Message;

public class ReplacementEntry {
    private final String key;
    private final String value;

    public ReplacementEntry(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}

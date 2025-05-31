package com.lyttledev.lyttleutils.types.Message;

public class ReplacementsKey {
    private final String value;

    public ReplacementsKey(String value) {
        if (!value.matches("[A-Z_]+")) {
            throw new IllegalArgumentException("Only A-Z and _ are allowed.");
        }
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}

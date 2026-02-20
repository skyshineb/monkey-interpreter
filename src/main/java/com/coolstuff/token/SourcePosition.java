package com.coolstuff.token;

public record SourcePosition(int line, int column) {
    public static final SourcePosition UNKNOWN = new SourcePosition(0, 0);

    @Override
    public String toString() {
        return "%d:%d".formatted(line, column);
    }
}

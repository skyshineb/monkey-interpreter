package com.coolstuff.token;

import java.util.Objects;

/**
 * @param type class values
 */
public record Token(TokenType type, String token, SourcePosition position) {
    public Token(TokenType type, String token) {
        this(type, token, SourcePosition.UNKNOWN);
    }

    public Token(TokenType type, String token, int line, int column) {
        this(type, token, new SourcePosition(line, column));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Token other)) {
            return false;
        }

        return type == other.type && Objects.equals(token, other.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, token);
    }
}

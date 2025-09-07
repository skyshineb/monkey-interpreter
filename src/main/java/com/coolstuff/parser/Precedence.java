package com.coolstuff.parser;

import com.coolstuff.token.TokenType;

public enum Precedence {
    LOWEST,
    EQUALS,         // == or !=
    LESS_GREATER,   // > or <
    SUM,            // + or -
    PRODUCT,        // * or /
    PREFIX,         // -X or !X
    CALL,           // myFunction(X)
    INDEX;          // array[index]

    public static Precedence precedenceForToken(TokenType type) {
        return switch (type) {
            case LPAREN -> Precedence.CALL;
            case EQ, NOT_EQ -> Precedence.EQUALS;
            case LT, GT -> Precedence.LESS_GREATER;
            case PLUS, MINUS -> Precedence.SUM;
            case ASTERISK, SLASH -> Precedence.PRODUCT;
            case LBRACKET -> Precedence.INDEX;
            default -> Precedence.LOWEST;
        };
    }
}

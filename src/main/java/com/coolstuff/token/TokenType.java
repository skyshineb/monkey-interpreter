package com.coolstuff.token;

public enum TokenType {
    // static token definitions
    ILLEGAL,
    EOF("eof"),

    // identifiers + literals
    IDENT,
    INT,
    STRING,

    // operators
    ASSIGN("="),
    PLUS("+"),
    MINUS("-"),
    BANG("!"),
    ASTERISK("*"),
    SLASH("/"),

    LT("<"),
    GT(">"),
    LTE("<="),
    GTE(">="),
    EQ("=="),
    NOT_EQ("!="),
    AND("&&"),
    OR("||"),

    // delimeters
    COMMA(","),
    SEMICOLON(";"),
    COLON(":"),

    LPAREN("("),
    RPAREN(")"),
    LBRACE("{"),
    RBRACE("}"),
    LBRACKET("["),
    RBRACKET("]"),

    // keywords
    FUNCTION("fn"),
    LET("let"),
    TRUE("true"),
    FALSE("false"),
    IF("if"),
    ELSE("else"),
    RETURN("return"),
    WHILE("while"),
    BREAK("break"),
    CONTINUE("continue");

    private final String literal;

    TokenType(String literal) {
        this.literal = literal;
    }

    TokenType() {
        this.literal = null;
    }

    public Token createToken(String literal) {
        return new Token(this, literal);
    }

    public Token createToken(String literal, int line, int column) {
        return new Token(this, literal, line, column);
    }

    public Token token() {
        if (literal == null) {
            throw new IllegalArgumentException(
                    "TokenType %s doesn't have a default Token. Create one using 'createToken'".formatted(this.name())
            );
        }
        return new Token(this, literal);
    }

    public Token token(int line, int column) {
        if (literal == null) {
            throw new IllegalArgumentException(
                    "TokenType %s doesn't have a default Token. Create one using 'createToken'".formatted(this.name())
            );
        }
        return new Token(this, literal, line, column);
    }
}

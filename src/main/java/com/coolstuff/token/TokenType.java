package com.coolstuff.token;

public enum TokenType {
    // static token definitions
    ILLEGAL,
    EOF("eof"),

    // identifiers + literals
    IDENT,
    INT,

    // operators
    ASSIGN("="),
    PLUS("+"),
    MINUS("-"),
    BANG("!"),
    ASTERISK("*"),
    SLASH("/"),

    LT("<"),
    GT(">"),

    // delimeters
    COMMA(","),
    SEMICOLON(";"),

    LPAREN("("),
    RPAREN(")"),
    LBRACE("{"),
    RBRACE("}"),

    // keywords
    FUNCTION("fn"),
    LET("let"),
    TRUE("true"),
    FALSE("false"),
    IF("if"),
    ELSE("else"),
    RETURN("return");

    private final Token token;

    TokenType(String literal) {
        token = new Token(this, literal);
    }
    TokenType() {
        token = null;
    }

    public Token createToken(String literal) {
        return new Token(this, literal);
    }

    public Token token() {
        if (token == null) {
            throw new IllegalArgumentException(
                    "TokenType %s doesn't have a default Token. Create one using 'createToken'".formatted(this.name())
            );
        }
        return token;
    }
}

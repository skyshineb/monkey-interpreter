package com.coolstuff.token;

public class Tokens {
    // static token definitions
    public final static String ILLEGAL = "ILLEGAL";
    public final static String EOF = "EOF";

    // identifiers + literals
    public final static String IDENT = "IDENT"; // add, foobar, x, y
    public final static String INT = "INT"; // 12345

    // operators
    public final static String ASSIGN = "=";
    public final static String PLUS = "+";

    // delimeters
    public final static String COMMA = ",";
    public final static String SEMICOLON = ";";

    public final static String LPAREN = "(";
    public final static String RPAREN = ")";
    public final static String LBRACE = "{";
    public final static String RBRACE = "}";

    // keywords
    public final static String FUNCTION = "FUNCTION";
    public final static String LET = "LET";
}

package com.coolstuff.parser;

public enum Precedence {
    LOWEST,
    EQUALS,         // == or !=
    LESS_GREATER,   // > or <
    SUM,            // + or -
    PRODUCT,        // * or /
    PREFIX,         // -X or !X
    CALL;           // myFunction(X)
}

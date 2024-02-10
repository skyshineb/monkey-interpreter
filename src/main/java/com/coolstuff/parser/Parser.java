package com.coolstuff.parser;

import com.coolstuff.ast.Program;
import com.coolstuff.lexer.Lexer;
import com.coolstuff.token.Token;

public class Parser {
    final Lexer lexer;

    Token curToken;
    Token peekToken;

    public Parser(Lexer lexer) {
        this.lexer = lexer;

        nextToken();
        nextToken();
    }

    void nextToken() {
        this.curToken = peekToken;
        this.peekToken = lexer.nextToken();
    }

    public Program parseProgram() {
        return null;
    }
}

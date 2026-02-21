package com.coolstuff;

import com.coolstuff.lexer.Lexer;
import com.coolstuff.token.TokenType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LexerPositionTest {

    @Test
    void tracksSingleLinePositions() {
        var lexer = new Lexer("let five = 5;");

        var letToken = lexer.nextToken();
        var identToken = lexer.nextToken();
        var assignToken = lexer.nextToken();
        var intToken = lexer.nextToken();

        Assertions.assertEquals(TokenType.LET, letToken.type());
        Assertions.assertEquals(1, letToken.position().line());
        Assertions.assertEquals(1, letToken.position().column());

        Assertions.assertEquals(TokenType.IDENT, identToken.type());
        Assertions.assertEquals(1, identToken.position().line());
        Assertions.assertEquals(5, identToken.position().column());

        Assertions.assertEquals(TokenType.ASSIGN, assignToken.type());
        Assertions.assertEquals(1, assignToken.position().line());
        Assertions.assertEquals(10, assignToken.position().column());

        Assertions.assertEquals(TokenType.INT, intToken.type());
        Assertions.assertEquals(1, intToken.position().line());
        Assertions.assertEquals(12, intToken.position().column());
    }

    @Test
    void tracksMultiLinePositions() {
        var lexer = new Lexer("let a = 1;\nlet b = 2;");

        for (int i = 0; i < 5; i++) {
            lexer.nextToken();
        }

        var secondLet = lexer.nextToken();
        Assertions.assertEquals(TokenType.LET, secondLet.type());
        Assertions.assertEquals(2, secondLet.position().line());
        Assertions.assertEquals(1, secondLet.position().column());
    }

    @Test
    void tracksTabsAndSpacesAsColumns() {
        var lexer = new Lexer("\t  foo");

        var token = lexer.nextToken();
        Assertions.assertEquals(TokenType.IDENT, token.type());
        Assertions.assertEquals(1, token.position().line());
        Assertions.assertEquals(4, token.position().column());
    }

    @Test
    void tracksPositionsAfterComments() {
        var lexer = new Lexer("let x = 1; # ignore this\n# whole line\nlet y = 2;");

        for (int i = 0; i < 5; i++) {
            lexer.nextToken();
        }

        var secondLet = lexer.nextToken();
        Assertions.assertEquals(TokenType.LET, secondLet.type());
        Assertions.assertEquals(3, secondLet.position().line());
        Assertions.assertEquals(1, secondLet.position().column());
    }

}

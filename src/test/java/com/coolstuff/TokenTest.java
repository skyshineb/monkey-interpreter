package com.coolstuff;

import com.coolstuff.lexer.Lexer;
import com.coolstuff.token.Tokens;
import org.junit.jupiter.api.Test;

public class TokenTest {

    @Test
    public void testNextToken(){
        var input = "=+(){},;";
        Lexer lexer = new Lexer(input);
        assert lexer.nextToken() == Tokens.ASSIGN;
        assert lexer.nextToken() == Tokens.PLUS;
        assert lexer.nextToken() == Tokens.LPAREN;
        assert lexer.nextToken() == Tokens.RPAREN;
        assert lexer.nextToken() == Tokens.LBRACE;
        assert lexer.nextToken() == Tokens.RBRACE;
        assert lexer.nextToken() == Tokens.COMMA;
        assert lexer.nextToken() == Tokens.SEMICOLON;

    }
}

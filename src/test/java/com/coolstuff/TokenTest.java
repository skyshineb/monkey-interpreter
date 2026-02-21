package com.coolstuff;

import com.coolstuff.lexer.Lexer;
import com.coolstuff.token.Token;
import com.coolstuff.token.TokenType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TokenTest {

    @Test
    public void testNextTokenSymbols(){
        var input = "=+(){},;";
        Lexer lexer = new Lexer(input);
        TokenType[] expected = {
                TokenType.ASSIGN,
                TokenType.PLUS,
                TokenType.LPAREN,
                TokenType.RPAREN,
                TokenType.LBRACE,
                TokenType.RBRACE,
                TokenType.COMMA,
                TokenType.SEMICOLON,
                TokenType.EOF,
        };
        Assertions.assertDoesNotThrow(() -> {
            for (TokenType t : expected) {
                TokenType r = lexer.nextToken().type();
                if (r != t) throw new AssertionError(String.format("Expected %s but got %s\n", t.name(), r.name()));
            }
        });
    }

    @Test
    public void testNextTokenMultiLineInput(){
        var input = """
                let five = 5;
                let ten = 10;
                
                let add = fn(x, y) {
                    x + y;
                };
                
                let result = add(five, ten);
                !-/*5;
                5 < 10 > 5;
                
                if (5 < 10) {
                return true;
                } else {
                return false;
                }

                while (10 > 1) {
                continue;
                break;
                }
                
                10 == 10;
                10 != 9;
                10 <= 10;
                10 >= 9;
                true && false;
                true || false;
                &
                |
                "foobar"
                "foo bar"
                [1, 2];
                {"foo": "bar"}
                """;
        Lexer lexer = new Lexer(input);
        Token[] expected = {
                new Token(TokenType.LET, "let"),
                new Token(TokenType.IDENT, "five"),
                new Token(TokenType.ASSIGN, "="),
                new Token(TokenType.INT, "5"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.LET, "let"),
                new Token(TokenType.IDENT, "ten"),
                new Token(TokenType.ASSIGN, "="),
                new Token(TokenType.INT, "10"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.LET, "let"),
                new Token(TokenType.IDENT, "add"),
                new Token(TokenType.ASSIGN, "="),
                new Token(TokenType.FUNCTION, "fn"),
                new Token(TokenType.LPAREN, "("),
                new Token(TokenType.IDENT, "x"),
                new Token(TokenType.COMMA, ","),
                new Token(TokenType.IDENT, "y"),
                new Token(TokenType.RPAREN, ")"),
                new Token(TokenType.LBRACE, "{"),
                new Token(TokenType.IDENT, "x"),
                new Token(TokenType.PLUS, "+"),
                new Token(TokenType.IDENT, "y"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.RBRACE, "}"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.LET, "let"),
                new Token(TokenType.IDENT, "result"),
                new Token(TokenType.ASSIGN, "="),
                new Token(TokenType.IDENT, "add"),
                new Token(TokenType.LPAREN, "("),
                new Token(TokenType.IDENT, "five"),
                new Token(TokenType.COMMA, ","),
                new Token(TokenType.IDENT, "ten"),
                new Token(TokenType.RPAREN, ")"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.BANG, "!"),
                new Token(TokenType.MINUS, "-"),
                new Token(TokenType.SLASH, "/"),
                new Token(TokenType.ASTERISK, "*"),
                new Token(TokenType.INT, "5"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.INT, "5"),
                new Token(TokenType.LT, "<"),
                new Token(TokenType.INT, "10"),
                new Token(TokenType.GT, ">"),
                new Token(TokenType.INT, "5"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.IF, "if"),
                new Token(TokenType.LPAREN, "("),
                new Token(TokenType.INT, "5"),
                new Token(TokenType.LT, "<"),
                new Token(TokenType.INT, "10"),
                new Token(TokenType.RPAREN, ")"),
                new Token(TokenType.LBRACE, "{"),
                new Token(TokenType.RETURN, "return"),
                new Token(TokenType.TRUE, "true"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.RBRACE, "}"),
                new Token(TokenType.ELSE, "else"),
                new Token(TokenType.LBRACE, "{"),
                new Token(TokenType.RETURN, "return"),
                new Token(TokenType.FALSE, "false"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.RBRACE, "}"),
                new Token(TokenType.WHILE, "while"),
                new Token(TokenType.LPAREN, "("),
                new Token(TokenType.INT, "10"),
                new Token(TokenType.GT, ">"),
                new Token(TokenType.INT, "1"),
                new Token(TokenType.RPAREN, ")"),
                new Token(TokenType.LBRACE, "{"),
                new Token(TokenType.CONTINUE, "continue"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.BREAK, "break"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.RBRACE, "}"),
                new Token(TokenType.INT, "10"),
                new Token(TokenType.EQ, "=="),
                new Token(TokenType.INT, "10"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.INT, "10"),
                new Token(TokenType.NOT_EQ, "!="),
                new Token(TokenType.INT, "9"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.INT, "10"),
                new Token(TokenType.LTE, "<="),
                new Token(TokenType.INT, "10"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.INT, "10"),
                new Token(TokenType.GTE, ">="),
                new Token(TokenType.INT, "9"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.TRUE, "true"),
                new Token(TokenType.AND, "&&"),
                new Token(TokenType.FALSE, "false"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.TRUE, "true"),
                new Token(TokenType.OR, "||"),
                new Token(TokenType.FALSE, "false"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.ILLEGAL, "&"),
                new Token(TokenType.ILLEGAL, "|"),
                new Token(TokenType.STRING, "foobar"),
                new Token(TokenType.STRING, "foo bar"),
                new Token(TokenType.LBRACKET, "["),
                new Token(TokenType.INT, "1"),
                new Token(TokenType.COMMA, ","),
                new Token(TokenType.INT, "2"),
                new Token(TokenType.RBRACKET, "]"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.LBRACE, "{"),
                new Token(TokenType.STRING, "foo"),
                new Token(TokenType.COLON, ":"),
                new Token(TokenType.STRING, "bar"),
                new Token(TokenType.RBRACE, "}"),
                new Token(TokenType.EOF, "eof"),
        };
        Assertions.assertDoesNotThrow(() -> {
            for (Token t : expected) {
                Token r = lexer.nextToken();
                if (!r.equals(t)) throw new AssertionError(String.format("Expected (%s, %s) but got (%s, %s)\n", t.type(), t.token(), r.type(), r.token()));
            }
        });
    }

    @Test
    public void testNextTokenHashComments(){
        var input = """
                let five = 5; # inline comment
                # full line comment
                let ten = 10;
                #
                ten;
                """;
        Lexer lexer = new Lexer(input);
        Token[] expected = {
                new Token(TokenType.LET, "let"),
                new Token(TokenType.IDENT, "five"),
                new Token(TokenType.ASSIGN, "="),
                new Token(TokenType.INT, "5"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.LET, "let"),
                new Token(TokenType.IDENT, "ten"),
                new Token(TokenType.ASSIGN, "="),
                new Token(TokenType.INT, "10"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.IDENT, "ten"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.EOF, "eof"),
        };
        Assertions.assertDoesNotThrow(() -> {
            for (Token t : expected) {
                Token r = lexer.nextToken();
                if (!r.equals(t)) throw new AssertionError(String.format("Expected (%s, %s) but got (%s, %s)\n", t.type(), t.token(), r.type(), r.token()));
            }
        });
    }

}

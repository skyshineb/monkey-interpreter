package com.coolstuff.lexer;

import com.coolstuff.token.Token;
import com.coolstuff.token.TokenType;

public class Lexer {
    final String input;
    int position; // current position int the input (points to current char)
    int readPosition; // current reading position (after current char)
    char ch; // current char under examination

    public Lexer(String input) {
        this.input = input;
        readChar();
    }

    public void readChar() {
        if (readPosition >= input.length()) {
            ch = 0;
        } else {
            ch = input.charAt(readPosition);
        }
        position = readPosition;
        readPosition += 1;
    }

    public Token nextToken() {
        Token tok;
        skipWhitespaces();

        switch (ch) {
            case '=' -> {
                if (peekChar() == '=') {
                    readChar();
                    tok = TokenType.EQ.token();
                } else {
                    tok = TokenType.ASSIGN.token();
                }


            }
            case '+' -> tok = TokenType.PLUS.token();
            case '-' -> tok = TokenType.MINUS.token();
            case '!' -> {
                if (peekChar() == '=') {
                    readChar();
                    tok = TokenType.NOT_EQ.token();
                } else {
                    tok = TokenType.BANG.token();
                }
            }
            case '/' -> tok = TokenType.SLASH.token();
            case '*' -> tok = TokenType.ASTERISK.token();
            case '<' -> tok = TokenType.LT.token();
            case '>' -> tok = TokenType.GT.token();
            case ';' -> tok = TokenType.SEMICOLON.token();
            case '(' -> tok = TokenType.LPAREN.token();
            case ')' -> tok = TokenType.RPAREN.token();
            case ',' -> tok = TokenType.COMMA.token();
            case '{' -> tok = TokenType.LBRACE.token();
            case '}' -> tok = TokenType.RBRACE.token();
            case '"' -> tok = new Token(TokenType.STRING, readString());
            case 0 -> tok = TokenType.EOF.token();
            default -> {
                if (Character.isJavaIdentifierStart(ch)) {
                    String ident = readIdentifier();
                    return switch (ident) {
                        case "fn" -> TokenType.FUNCTION.token();
                        case "let" -> TokenType.LET.token();
                        case "true" -> TokenType.TRUE.token();
                        case "false" -> TokenType.FALSE.token();
                        case "if" -> TokenType.IF.token();
                        case "else" -> TokenType.ELSE.token();
                        case "return" -> TokenType.RETURN.token();
                        default -> TokenType.IDENT.createToken(ident);
                    };
                } else if (Character.isDigit(ch)) {
                    return TokenType.INT.createToken(readNumber());
                } else {
                    tok = new Token(TokenType.ILLEGAL, Character.toString(ch));
                }
            }
        }
        readChar();
        return tok;
    }

    private void skipWhitespaces() {
        while (Character.isWhitespace(ch)) {
            readChar();
        }
    }

    private char peekChar() {
        if (readPosition >= input.length()) {
            return 0;
        } else {
            return input.charAt(readPosition);
        }
    }

    public String readIdentifier() {
        var pos = this.position;
        while (Character.isJavaIdentifierStart(ch)) {
            readChar();
        }
        return input.substring(pos, position);
    }

    public String readNumber() {
        var pos = this.position;
        while (Character.isDigit(ch)) {
            readChar();
        }
        return input.substring(pos, position);
    }

    public String readString() {
        var pos = this.position + 1;
        do {
            readChar();
        } while (ch != '"' && ch != 0);
        return input.substring(pos, this.position);
    }
}

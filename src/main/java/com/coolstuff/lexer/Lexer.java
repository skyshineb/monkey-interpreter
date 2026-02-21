package com.coolstuff.lexer;

import com.coolstuff.token.Token;
import com.coolstuff.token.TokenType;

public class Lexer {
    final String input;
    int position; // current position int the input (points to current char)
    int readPosition; // current reading position (after current char)
    char ch; // current char under examination
    int line = 1;
    int column = 1;
    int currentLine = 1;
    int currentColumn = 1;

    public Lexer(String input) {
        this.input = input;
        readChar();
    }

    public void readChar() {
        if (readPosition >= input.length()) {
            ch = 0;
            currentLine = line;
            currentColumn = column;
        } else {
            ch = input.charAt(readPosition);
            currentLine = line;
            currentColumn = column;
        }

        if (ch == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }

        position = readPosition;
        readPosition += 1;
    }

    public Token nextToken() {
        Token tok;
        skipIgnored();
        var tokenLine = currentLine;
        var tokenColumn = currentColumn;

        switch (ch) {
            case '=' -> {
                if (peekChar() == '=') {
                    readChar();
                    tok = TokenType.EQ.token(tokenLine, tokenColumn);
                } else {
                    tok = TokenType.ASSIGN.token(tokenLine, tokenColumn);
                }
            }
            case '+' -> tok = TokenType.PLUS.token(tokenLine, tokenColumn);
            case '-' -> tok = TokenType.MINUS.token(tokenLine, tokenColumn);
            case '!' -> {
                if (peekChar() == '=') {
                    readChar();
                    tok = TokenType.NOT_EQ.token(tokenLine, tokenColumn);
                } else {
                    tok = TokenType.BANG.token(tokenLine, tokenColumn);
                }
            }
            case '&' -> {
                if (peekChar() == '&') {
                    readChar();
                    tok = TokenType.AND.token(tokenLine, tokenColumn);
                } else {
                    tok = new Token(TokenType.ILLEGAL, Character.toString(ch), tokenLine, tokenColumn);
                }
            }
            case '|' -> {
                if (peekChar() == '|') {
                    readChar();
                    tok = TokenType.OR.token(tokenLine, tokenColumn);
                } else {
                    tok = new Token(TokenType.ILLEGAL, Character.toString(ch), tokenLine, tokenColumn);
                }
            }
            case '/' -> tok = TokenType.SLASH.token(tokenLine, tokenColumn);
            case '*' -> tok = TokenType.ASTERISK.token(tokenLine, tokenColumn);
            case '<' -> {
                if (peekChar() == '=') {
                    readChar();
                    tok = TokenType.LTE.token(tokenLine, tokenColumn);
                } else {
                    tok = TokenType.LT.token(tokenLine, tokenColumn);
                }
            }
            case '>' -> {
                if (peekChar() == '=') {
                    readChar();
                    tok = TokenType.GTE.token(tokenLine, tokenColumn);
                } else {
                    tok = TokenType.GT.token(tokenLine, tokenColumn);
                }
            }
            case ';' -> tok = TokenType.SEMICOLON.token(tokenLine, tokenColumn);
            case ':' -> tok = TokenType.COLON.token(tokenLine, tokenColumn);
            case '(' -> tok = TokenType.LPAREN.token(tokenLine, tokenColumn);
            case ')' -> tok = TokenType.RPAREN.token(tokenLine, tokenColumn);
            case ',' -> tok = TokenType.COMMA.token(tokenLine, tokenColumn);
            case '{' -> tok = TokenType.LBRACE.token(tokenLine, tokenColumn);
            case '}' -> tok = TokenType.RBRACE.token(tokenLine, tokenColumn);
            case '[' -> tok = TokenType.LBRACKET.token(tokenLine, tokenColumn);
            case ']' -> tok = TokenType.RBRACKET.token(tokenLine, tokenColumn);
            case '"' -> tok = new Token(TokenType.STRING, readString(), tokenLine, tokenColumn);
            case 0 -> tok = TokenType.EOF.token(tokenLine, tokenColumn);
            default -> {
                if (Character.isJavaIdentifierStart(ch)) {
                    String ident = readIdentifier();
                    return switch (ident) {
                        case "fn" -> TokenType.FUNCTION.token(tokenLine, tokenColumn);
                        case "let" -> TokenType.LET.token(tokenLine, tokenColumn);
                        case "true" -> TokenType.TRUE.token(tokenLine, tokenColumn);
                        case "false" -> TokenType.FALSE.token(tokenLine, tokenColumn);
                        case "if" -> TokenType.IF.token(tokenLine, tokenColumn);
                        case "else" -> TokenType.ELSE.token(tokenLine, tokenColumn);
                        case "return" -> TokenType.RETURN.token(tokenLine, tokenColumn);
                        case "while" -> TokenType.WHILE.token(tokenLine, tokenColumn);
                        case "break" -> TokenType.BREAK.token(tokenLine, tokenColumn);
                        case "continue" -> TokenType.CONTINUE.token(tokenLine, tokenColumn);
                        default -> TokenType.IDENT.createToken(ident, tokenLine, tokenColumn);
                    };
                } else if (Character.isDigit(ch)) {
                    return TokenType.INT.createToken(readNumber(), tokenLine, tokenColumn);
                } else {
                    tok = new Token(TokenType.ILLEGAL, Character.toString(ch), tokenLine, tokenColumn);
                }
            }
        }
        readChar();
        return tok;
    }

    private void skipIgnored() {
        boolean skipping;
        do {
            skipping = false;

            while (Character.isWhitespace(ch)) {
                readChar();
                skipping = true;
            }

            if (ch == '#') {
                skipComment();
                skipping = true;
            }
        } while (skipping);
    }

    private void skipComment() {
        while (ch != '\n' && ch != 0) {
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

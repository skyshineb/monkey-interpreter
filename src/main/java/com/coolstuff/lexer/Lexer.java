package com.coolstuff.lexer;

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
}

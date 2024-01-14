package com.coolstuff.repl;

import com.coolstuff.lexer.Lexer;
import com.coolstuff.token.Token;
import com.coolstuff.token.TokenType;

import java.util.Scanner;

public class REPL {
    final String PROMPT = ">> ";
    public void start() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.printf(PROMPT);
            String input = scanner.nextLine();


            Lexer l = new Lexer(input);
            for (Token t = l.nextToken(); t.type() != TokenType.EOF; t = l.nextToken()) {
                System.out.printf("%s\n", t);
            }
        }
    }
}

package com.coolstuff.repl;

import com.coolstuff.ast.Program;
import com.coolstuff.evaluator.EvaluationException;
import com.coolstuff.evaluator.Evaluator;
import com.coolstuff.evaluator.object.MonkeyObject;
import com.coolstuff.lexer.Lexer;
import com.coolstuff.parser.Parser;

import java.util.List;
import java.util.Scanner;

public class REPL {
    final String MONKEY_FACE = """
                            __,__
                   .--.  .-"     "-.  .--.
                  / .. \\/  .-. .-.  \\/ .. \\
                 | |  '|  /   Y   \\  |'  | |
                 | \\   \\  \\ 0 | 0 /  /   / |
                  \\ '- ,\\.-"`` ``"-./, -' /
                   `'-' /_   ^ ^   _\\ '-'`
                       |  \\._   _./  |
                       \\   \\ `~` /   /
                        '._ '-=-' _.'
                           '~---~'
            """;
    final String PROMPT = ">> ";
    public void start() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.printf(PROMPT);
            String input = scanner.nextLine();

            Lexer l = new Lexer(input);
            Parser p = new Parser(l);
            Evaluator e = new Evaluator();
            Program program = p.parseProgram();
            if (!p.getErrors().isEmpty()) {
                printParseErrors(p.getErrors());
                continue;
            }
            try {
                MonkeyObject<?> evaluated = e.eval(program);
                System.out.println(evaluated.inspect());

            } catch (EvaluationException exc) {
                System.out.println(exc.getMessage());
            }
        }
    }

    private void printParseErrors(List<String> errors) {
        System.out.printf("%s\n", MONKEY_FACE);
        System.out.printf("Woops! We ran into some monkey business here!\n");
        for (String err : errors) {
            System.out.printf("\t%s\n", err);
        }
    }
}

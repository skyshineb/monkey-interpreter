package com.coolstuff.repl;

import com.coolstuff.ast.Program;
import com.coolstuff.evaluator.EvaluationException;
import com.coolstuff.evaluator.Evaluator;
import com.coolstuff.evaluator.object.MonkeyObject;
import com.coolstuff.lexer.Lexer;
import com.coolstuff.parser.Parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
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
                   `'-' /_   ^ ^   _\\ '-'
                       |  \\._   _./  |
                       \\   \\ `~` /   /
                        '._ '-=-' _.'
                           '~---~'
            """;
    final String PROMPT = ">> ";

    private final Scanner scanner;
    private final Appendable out;

    public REPL() {
        this(System.in, System.out);
    }

    public REPL(InputStream input, PrintStream out) {
        this(new Scanner(input), out);
    }


    public REPL(Scanner scanner, Appendable out) {
        this.scanner = scanner;
        this.out = out;
    }

    public void start() {
        while (true) {
            print(PROMPT);
            String input = scanner.nextLine();
            evaluateInput(input);
        }
    }

    public void runUntilEof() {
        while (scanner.hasNextLine()) {
            print(PROMPT);
            String input = scanner.nextLine();

            if (isSessionTerminationCommand(input)) {
                break;
            }

            evaluateInput(input);
        }
    }

    private boolean isSessionTerminationCommand(String input) {
        return ":quit".equals(input) || ":exit".equals(input);
    }

    private void evaluateInput(String input) {
        Lexer l = new Lexer(input);
        Parser p = new Parser(l);
        Evaluator e = new Evaluator();
        Program program = p.parseProgram();
        if (!p.getErrors().isEmpty()) {
            printParseErrors(p.getErrors());
            return;
        }
        try {
            MonkeyObject<?> evaluated = e.eval(program);
            println(evaluated.inspect());

        } catch (EvaluationException exc) {
            println(exc.getMessage());
        }
    }

    private void printParseErrors(List<String> errors) {
        println(MONKEY_FACE);
        println("Woops! We ran into some monkey business here!");
        for (String err : errors) {
            println("\t" + err);
        }
    }

    private void print(String text) {
        append(text);
    }

    private void println(String text) {
        append(text + "\n");
    }

    private void append(String text) {
        try {
            out.append(text);
        } catch (IOException exc) {
            throw new UncheckedIOException(exc);
        }
    }
}

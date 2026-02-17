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
    final String SECONDARY_PROMPT = ".. ";

    private final Scanner scanner;
    private final Appendable out;
    private final Evaluator evaluator;

    public REPL() {
        this(System.in, System.out);
    }

    public REPL(InputStream input, PrintStream out) {
        this(new Scanner(input), out);
    }


    public REPL(Scanner scanner, Appendable out) {
        this.scanner = scanner;
        this.out = out;
        this.evaluator = new Evaluator();
    }

    public void start() {
        var inputBuffer = new StringBuilder();
        while (true) {
            print(inputBuffer.isEmpty() ? PROMPT : SECONDARY_PROMPT);
            readAndEvaluateInputLine(inputBuffer, scanner.nextLine(), false);
        }
    }

    public void runUntilEof() {
        var inputBuffer = new StringBuilder();

        while (scanner.hasNextLine()) {
            print(inputBuffer.isEmpty() ? PROMPT : SECONDARY_PROMPT);
            var input = scanner.nextLine();
            if (readAndEvaluateInputLine(inputBuffer, input, true)) {
                break;
            }
        }
    }

    private boolean readAndEvaluateInputLine(StringBuilder inputBuffer, String inputLine, boolean stopOnSessionTerminationCommand) {
        if (stopOnSessionTerminationCommand && inputBuffer.isEmpty() && isSessionTerminationCommand(inputLine)) {
            return true;
        }

        if (!inputBuffer.isEmpty()) {
            inputBuffer.append('\n');
        }
        inputBuffer.append(inputLine);

        if (!isInputComplete(inputBuffer.toString())) {
            return false;
        }

        evaluateInput(inputBuffer.toString());
        inputBuffer.setLength(0);
        return false;
    }

    private boolean isSessionTerminationCommand(String input) {
        return ":quit".equals(input) || ":exit".equals(input);
    }

    private boolean isInputComplete(String input) {
        int openBraces = 0;
        int openParentheses = 0;
        int openBrackets = 0;
        boolean inString = false;
        boolean escaped = false;

        for (char currentChar : input.toCharArray()) {
            if (inString) {
                if (escaped) {
                    escaped = false;
                    continue;
                }

                if (currentChar == '\\') {
                    escaped = true;
                    continue;
                }

                if (currentChar == '"') {
                    inString = false;
                }
                continue;
            }

            switch (currentChar) {
                case '"' -> inString = true;
                case '{' -> openBraces++;
                case '}' -> {
                    if (openBraces == 0) {
                        return true;
                    }
                    openBraces--;
                }
                case '(' -> openParentheses++;
                case ')' -> {
                    if (openParentheses == 0) {
                        return true;
                    }
                    openParentheses--;
                }
                case '[' -> openBrackets++;
                case ']' -> {
                    if (openBrackets == 0) {
                        return true;
                    }
                    openBrackets--;
                }
                default -> {
                }
            }
        }

        return !inString && openBraces == 0 && openParentheses == 0 && openBrackets == 0;
    }

    private void evaluateInput(String input) {
        Lexer l = new Lexer(input);
        Parser p = new Parser(l);
        Program program = p.parseProgram();
        if (!p.getErrors().isEmpty()) {
            printParseErrors(p.getErrors());
            return;
        }
        try {
            MonkeyObject<?> evaluated = evaluator.eval(program);
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

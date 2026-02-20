package com.coolstuff.repl;

import com.coolstuff.ast.Program;
import com.coolstuff.evaluator.Environment;
import com.coolstuff.evaluator.EvaluationException;
import com.coolstuff.evaluator.Evaluator;
import com.coolstuff.evaluator.object.MonkeyObject;
import com.coolstuff.lexer.Lexer;
import com.coolstuff.parser.Parser;
import com.coolstuff.token.Token;
import com.coolstuff.token.TokenType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
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
    private final BufferedReader inputReader;
    private final InputStream sourceInput;
    private final Appendable out;
    private final Evaluator evaluator;
    private final InputAccumulator inputAccumulator;
    private PendingInputAction pendingInputAction = PendingInputAction.EVALUATE;

    public REPL() {
        this(System.in, System.out);
    }

    public REPL(InputStream input, PrintStream out) {
        this.scanner = null;
        this.inputReader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        this.sourceInput = input;
        this.out = out;
        this.evaluator = new Evaluator();
        this.inputAccumulator = new InputAccumulator();
    }


    public REPL(Scanner scanner, Appendable out) {
        this.scanner = scanner;
        this.inputReader = null;
        this.sourceInput = null;
        this.out = out;
        this.evaluator = new Evaluator();
        this.inputAccumulator = new InputAccumulator();
    }

    public void start() {
        var inputBuffer = new StringBuilder();
        print(PROMPT);
        while (true) {
            if (readAndEvaluateInputLine(inputBuffer, readLineOrThrow(), true)) {
                return;
            }
            if (inputBuffer.isEmpty()) {
                print(PROMPT);
            } else if (shouldPrintSecondaryPrompt()) {
                print(SECONDARY_PROMPT);
            }
        }
    }

    public void runUntilEof() {
        var inputBuffer = new StringBuilder();
        var input = readLineForRunUntilEof();
        if (input == null) {
            return;
        }

        print(PROMPT);
        while (input != null) {
            if (readAndEvaluateInputLine(inputBuffer, input, true)) {
                break;
            }
            if (inputBuffer.isEmpty()) {
                print(PROMPT);
            } else if (shouldPrintSecondaryPrompt()) {
                print(SECONDARY_PROMPT);
            }
            input = readLineForRunUntilEof();
        }
    }

    private String readLineOrThrow() {
        if (scanner != null) {
            return scanner.nextLine();
        }

        try {
            String line = inputReader.readLine();
            if (line == null) {
                throw new NoSuchElementException();
            }
            return line;
        } catch (IOException exc) {
            throw new UncheckedIOException(exc);
        }
    }

    private String readLineForRunUntilEof() {
        if (scanner != null) {
            return scanner.hasNextLine() ? scanner.nextLine() : null;
        }

        try {
            return inputReader.readLine();
        } catch (IOException exc) {
            throw new UncheckedIOException(exc);
        }
    }

    private boolean shouldPrintSecondaryPrompt() {
        if (inputReader != null) {
            try {
                return !inputReader.ready();
            } catch (IOException exc) {
                return true;
            }
        }

        if (sourceInput == null) {
            return true;
        }

        try {
            return sourceInput.available() == 0;
        } catch (IOException exc) {
            return true;
        }
    }

    private boolean readAndEvaluateInputLine(StringBuilder inputBuffer, String inputLine, boolean stopOnSessionTerminationCommand) {
        var trimmedInput = inputLine.trim();

        if (stopOnSessionTerminationCommand && inputBuffer.isEmpty() && isSessionTerminationCommand(trimmedInput)) {
            return true;
        }

        if (trimmedInput.startsWith(":")) {
            handleMetaCommand(inputBuffer, trimmedInput);
            return false;
        }

        if (!inputBuffer.isEmpty()) {
            inputBuffer.append('\n');
        }
        inputBuffer.append(inputLine);

        if (!inputAccumulator.isInputComplete(inputBuffer.toString())) {
            return false;
        }

        processCompletedInput(inputBuffer.toString());
        inputBuffer.setLength(0);
        return false;
    }

    private void processCompletedInput(String input) {
        switch (pendingInputAction) {
            case TOKENS -> {
                printTokens(input);
                pendingInputAction = PendingInputAction.EVALUATE;
            }
            case AST -> {
                printAst(input);
                pendingInputAction = PendingInputAction.EVALUATE;
            }
            case EVALUATE -> evaluateInput(input);
        }
    }

    private void handleMetaCommand(StringBuilder inputBuffer, String trimmedInput) {
        if (!inputBuffer.isEmpty()) {
            println("Meta-commands are only allowed when the multiline buffer is empty.");
            return;
        }

        var parts = trimmedInput.split("\\s+", 2);
        var command = parts[0];
        var commandInput = parts.length > 1 ? parts[1] : "";

        switch (command) {
            case ":help" -> printHelp();
            case ":tokens" -> {
                if (commandInput.isBlank()) {
                    pendingInputAction = PendingInputAction.TOKENS;
                    println("Token debug mode: enter the next complete input to inspect tokens.");
                } else {
                    printTokens(commandInput);
                }
            }
            case ":ast" -> {
                if (commandInput.isBlank()) {
                    pendingInputAction = PendingInputAction.AST;
                    println("AST debug mode: enter the next complete input to inspect the parsed AST.");
                } else {
                    printAst(commandInput);
                }
            }
            case ":env" -> printEnvironmentBindings();
            case ":quit", ":exit" -> {
                // handled before generic command processing
            }
            default -> println("Unknown command: " + command + ". Type :help for available commands.");
        }
    }

    private boolean isSessionTerminationCommand(String input) {
        return ":quit".equals(input) || ":exit".equals(input);
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
            println(exc.getRuntimeError().formatMultiline());
        }
    }

    private void printHelp() {
        println("Available commands:");
        println("  :help                Show this help message.");
        println("  :tokens [input]      Show tokens for inline input, or for the next complete input when omitted.");
        println("  :ast [input]         Show Program.string() output for inline input, or for the next complete input when omitted.");
        println("  :env                 Show current environment bindings.");
        println("  :quit / :exit        Exit the REPL.");
        println("Note: meta-commands are only accepted when the multiline buffer is empty.");
    }

    private void printTokens(String input) {
        println("TOKENS:");
        var lexer = new Lexer(input);
        Token token;
        do {
            token = lexer.nextToken();
            println("  %s('%s') @ %s".formatted(token.type(), token.token(), token.position()));
        } while (token.type() != TokenType.EOF);
    }

    private void printAst(String input) {
        var parser = new Parser(new Lexer(input));
        var program = parser.parseProgram();

        if (!parser.getErrors().isEmpty()) {
            printParseErrors(parser.getErrors());
            return;
        }

        println("AST:");
        println(program.string());
    }

    private void printEnvironmentBindings() {
        Environment environment = evaluator.getEnvironment();
        var bindings = environment.snapshotCurrentScope();

        println("ENV:");
        if (bindings.isEmpty()) {
            println("  (empty)");
            return;
        }

        bindings.entrySet().stream()
                .sorted(Comparator.comparing(java.util.Map.Entry::getKey))
                .forEach(entry -> println("  %s = %s".formatted(entry.getKey(), entry.getValue().inspect())));
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

    private enum PendingInputAction {
        EVALUATE,
        TOKENS,
        AST
    }
}

package com.coolstuff;

import com.coolstuff.evaluator.EvaluationException;
import com.coolstuff.evaluator.Evaluator;
import com.coolstuff.lexer.Lexer;
import com.coolstuff.parser.Parser;
import com.coolstuff.token.Token;
import com.coolstuff.token.TokenType;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class CliRunner {
    private static final String USAGE = "Usage: monkey [run <path> | --tokens <path> | --ast <path>]";

    public int run(String[] args, PrintStream out, PrintStream err) {
        if (args.length != 2) {
            printUsage(err);
            return 1;
        }

        var command = args[0];
        if (!isSupportedCommand(command)) {
            printUsage(err);
            return 1;
        }

        var sourcePathString = args[1];
        Path sourcePath;
        String input;

        try {
            sourcePath = Path.of(sourcePathString);
            input = Files.readString(sourcePath, StandardCharsets.UTF_8);
        } catch (InvalidPathException | IOException exc) {
            err.println("Failed to read file: " + sourcePathString + " (" + exc.getMessage() + ")");
            return 1;
        }

        return switch (command) {
            case "run" -> runProgram(input, out, err);
            case "--tokens" -> printTokens(input, out);
            case "--ast" -> printAst(input, out, err);
            default -> throw new IllegalStateException("Unexpected command: " + command);
        };
    }

    private boolean isSupportedCommand(String command) {
        return switch (command) {
            case "run", "--tokens", "--ast" -> true;
            default -> false;
        };
    }

    public static void printUsage(PrintStream err) {
        err.println(USAGE);
    }

    private int runProgram(String input, PrintStream out, PrintStream err) {
        var parser = new Parser(new Lexer(input));
        var program = parser.parseProgram();

        if (!parser.getErrors().isEmpty()) {
            parser.getErrors().forEach(err::println);
            return 1;
        }

        var evaluator = new Evaluator();
        try {
            var result = evaluator.eval(program);
            out.println(result.inspect());
            return 0;
        } catch (EvaluationException exc) {
            err.println(exc.getRuntimeError().formatMultiline());
            return 1;
        }
    }

    private int printTokens(String input, PrintStream out) {
        var lexer = new Lexer(input);
        Token token;
        do {
            token = lexer.nextToken();
            out.println("%s('%s') @ %s".formatted(token.type(), token.token(), token.position()));
        } while (token.type() != TokenType.EOF);

        return 0;
    }

    private int printAst(String input, PrintStream out, PrintStream err) {
        var parser = new Parser(new Lexer(input));
        var program = parser.parseProgram();

        if (!parser.getErrors().isEmpty()) {
            parser.getErrors().forEach(err::println);
            return 1;
        }

        out.println(program.string());
        return 0;
    }
}

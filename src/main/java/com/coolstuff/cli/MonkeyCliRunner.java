package com.coolstuff.cli;

import com.coolstuff.evaluator.Evaluator;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class MonkeyCliRunner {
    private static final String USAGE = "Usage: monkey [run <path> | --tokens <path> | --ast <path>]";

    private final MonkeyPipeline pipeline;

    public MonkeyCliRunner() {
        this(new MonkeyPipeline());
    }

    MonkeyCliRunner(MonkeyPipeline pipeline) {
        this.pipeline = pipeline;
    }

    public int run(String[] args, PrintStream out, PrintStream err) {
        if (args.length != 2) {
            printUsage(err);
            return 1;
        }

        var mode = Mode.fromCommand(args[0]);
        if (mode == null) {
            printUsage(err);
            return 1;
        }

        var sourcePathString = args[1];
        String input;
        try {
            var sourcePath = Path.of(sourcePathString);
            input = Files.readString(sourcePath, StandardCharsets.UTF_8);
        } catch (InvalidPathException | IOException exc) {
            err.println("Failed to read file: " + sourcePathString + " (" + exc.getMessage() + ")");
            return 1;
        }

        return switch (mode) {
            case RUN -> runProgram(input, out, err);
            case TOKENS -> printTokens(input, out);
            case AST -> printAst(input, out, err);
        };
    }

    public static void printUsage(PrintStream err) {
        err.println(USAGE);
    }

    private int runProgram(String input, PrintStream out, PrintStream err) {
        var result = pipeline.evaluate(input, new Evaluator());

        if (result.hasParseErrors()) {
            result.parseErrors().forEach(err::println);
            return 1;
        }

        if (result.hasEvaluationError()) {
            err.println(result.evaluationException().getRuntimeError().formatMultiline());
            return 1;
        }

        out.println(result.value().inspect());
        return 0;
    }

    private int printTokens(String input, PrintStream out) {
        pipeline.tokenStream(input).forEach(out::println);
        return 0;
    }

    private int printAst(String input, PrintStream out, PrintStream err) {
        var parseResult = pipeline.parseProgram(input);
        if (!parseResult.errors().isEmpty()) {
            parseResult.errors().forEach(err::println);
            return 1;
        }

        out.println(parseResult.program().string());
        return 0;
    }

    enum Mode {
        RUN,
        TOKENS,
        AST;

        static Mode fromCommand(String command) {
            return switch (command) {
                case "run" -> RUN;
                case "--tokens" -> TOKENS;
                case "--ast" -> AST;
                default -> null;
            };
        }
    }
}

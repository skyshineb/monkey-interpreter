package com.coolstuff.cli;

import com.coolstuff.ast.AstPrinter;
import com.coolstuff.evaluator.Evaluator;

import java.io.PrintStream;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.stream.Collectors;

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
        var result = execute(args);
        out.print(result.stdoutText());
        err.print(result.stderrText());
        return result.exitCode();
    }

    CliResult execute(String[] args) {
        if (args.length != 2) {
            return CliResult.usageError(usageText());
        }

        var mode = Mode.fromCommand(args[0]);
        if (mode == null) {
            return CliResult.usageError(usageText());
        }

        Path sourcePath;
        String input;
        try {
            sourcePath = Path.of(args[1]);
        } catch (InvalidPathException exc) {
            return CliResult.error("Invalid path: %s%n".formatted(args[1]));
        }

        try {
            input = ScriptSourceLoader.load(sourcePath);
        } catch (ScriptSourceLoader.ScriptLoadException exc) {
            return CliResult.error(exc.toCliMessage() + System.lineSeparator());
        }

        return switch (mode) {
            case RUN -> runProgram(sourcePath, input);
            case TOKENS -> printTokens(input);
            case AST -> printAst(sourcePath, input);
        };
    }

    public static void printUsage(PrintStream err) {
        err.print(usageText());
    }

    private static String usageText() {
        return USAGE + System.lineSeparator();
    }

    private CliResult runProgram(Path sourcePath, String input) {
        var result = pipeline.evaluate(input, new Evaluator());

        if (result.hasParseErrors()) {
            return CliResult.error(formatParseErrors(sourcePath, result.parseErrors()));
        }

        if (result.hasEvaluationError()) {
            return CliResult.error(formatRuntimeError(sourcePath, result.evaluationException()));
        }

        return CliResult.success(result.value().inspect() + System.lineSeparator());
    }

    private CliResult printTokens(String input) {
        var output = pipeline.tokenStream(input).stream()
                .collect(Collectors.joining(System.lineSeparator(), "", System.lineSeparator()));
        return CliResult.success(output);
    }

    private CliResult printAst(Path sourcePath, String input) {
        var parseResult = pipeline.parseProgram(input);
        if (!parseResult.errors().isEmpty()) {
            return CliResult.error(formatParseErrors(sourcePath, parseResult.errors()));
        }

        return CliResult.success(AstPrinter.print(parseResult.program()));
    }

    private String formatParseErrors(Path sourcePath, java.util.List<String> parseErrors) {
        var details = parseErrors.stream()
                .map(error -> "- " + error)
                .collect(Collectors.joining(System.lineSeparator()));

        return "Parse errors in %s:%n%s%n".formatted(sourcePath, details);
    }

    private String formatRuntimeError(Path sourcePath, com.coolstuff.evaluator.EvaluationException exc) {
        return "Runtime error in %s:%n%s%n".formatted(sourcePath, exc.getRuntimeError().formatMultiline());
    }

    record CliResult(int exitCode, String stdoutText, String stderrText) {
        static CliResult success(String stdoutText) {
            return new CliResult(0, stdoutText, "");
        }

        static CliResult error(String stderrText) {
            return new CliResult(1, "", stderrText);
        }

        static CliResult usageError(String stderrText) {
            return new CliResult(2, "", stderrText);
        }
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

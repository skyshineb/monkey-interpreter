package com.coolstuff.cli;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class MonkeyCliRunnerTest {

    @TempDir
    Path tempDir;

    @Test
    public void runModeEvaluatesProgram() throws Exception {
        Path source = tempDir.resolve("program.monkey");
        Files.writeString(source, "40 + 2;", StandardCharsets.UTF_8);

        var outBuffer = new ByteArrayOutputStream();
        var errBuffer = new ByteArrayOutputStream();

        var exitCode = new MonkeyCliRunner().run(
                new String[]{"run", source.toString()},
                new PrintStream(outBuffer, true, StandardCharsets.UTF_8),
                new PrintStream(errBuffer, true, StandardCharsets.UTF_8)
        );

        Assertions.assertEquals(0, exitCode);
        Assertions.assertEquals("42\n", outBuffer.toString(StandardCharsets.UTF_8));
        Assertions.assertEquals("", errBuffer.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void astModeReturnsFormattedParseErrors() throws Exception {
        Path source = tempDir.resolve("bad.monkey");
        Files.writeString(source, "let x = ;", StandardCharsets.UTF_8);

        var result = new MonkeyCliRunner().execute(new String[]{"--ast", source.toString()});

        Assertions.assertEquals(1, result.exitCode());
        Assertions.assertEquals("", result.stdoutText());
        Assertions.assertTrue(result.stderrText().startsWith("Parse errors in " + source));
        Assertions.assertTrue(result.stderrText().contains("- no prefix parse function"));
    }

    @Test
    public void runModeReturnsFormattedRuntimeErrors() throws Exception {
        Path source = tempDir.resolve("runtime.monkey");
        Files.writeString(source, "unknown;", StandardCharsets.UTF_8);

        var result = new MonkeyCliRunner().execute(new String[]{"run", source.toString()});

        Assertions.assertEquals(1, result.exitCode());
        Assertions.assertEquals("", result.stdoutText());
        Assertions.assertTrue(result.stderrText().startsWith("Runtime error in " + source));
        Assertions.assertTrue(result.stderrText().contains("Identifier not found"));
    }
}

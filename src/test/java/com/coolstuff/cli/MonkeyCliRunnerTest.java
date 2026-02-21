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
    public void astModeReturnsParseErrors() throws Exception {
        Path source = tempDir.resolve("bad.monkey");
        Files.writeString(source, "let x = ;", StandardCharsets.UTF_8);

        var outBuffer = new ByteArrayOutputStream();
        var errBuffer = new ByteArrayOutputStream();

        var exitCode = new MonkeyCliRunner().run(
                new String[]{"--ast", source.toString()},
                new PrintStream(outBuffer, true, StandardCharsets.UTF_8),
                new PrintStream(errBuffer, true, StandardCharsets.UTF_8)
        );

        Assertions.assertEquals(1, exitCode);
        Assertions.assertEquals("", outBuffer.toString(StandardCharsets.UTF_8));
        Assertions.assertTrue(errBuffer.toString(StandardCharsets.UTF_8).contains("no prefix parse function"));
    }
}

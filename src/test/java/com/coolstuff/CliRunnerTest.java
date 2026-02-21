package com.coolstuff;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class CliRunnerTest {

    @TempDir
    Path tempDir;

    @Test
    public void runCommandEvaluatesProgram() throws Exception {
        Path source = tempDir.resolve("program.monkey");
        Files.writeString(source, "1 + 2;\n", StandardCharsets.UTF_8);

        ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();

        int exitCode = new CliRunner().run(
                new String[]{"run", source.toString()},
                new PrintStream(outBuffer, true, StandardCharsets.UTF_8),
                new PrintStream(errBuffer, true, StandardCharsets.UTF_8)
        );

        Assertions.assertEquals(0, exitCode);
        Assertions.assertEquals("3\n", outBuffer.toString(StandardCharsets.UTF_8));
        Assertions.assertEquals("", errBuffer.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void tokensCommandPrintsTokens() throws Exception {
        Path source = tempDir.resolve("tokens.monkey");
        Files.writeString(source, "let x = 5;", StandardCharsets.UTF_8);

        ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();

        int exitCode = new CliRunner().run(
                new String[]{"--tokens", source.toString()},
                new PrintStream(outBuffer, true, StandardCharsets.UTF_8),
                new PrintStream(errBuffer, true, StandardCharsets.UTF_8)
        );

        String output = outBuffer.toString(StandardCharsets.UTF_8);
        Assertions.assertEquals(0, exitCode);
        Assertions.assertTrue(output.contains("LET('let')"));
        Assertions.assertTrue(output.contains("IDENT('x')"));
        Assertions.assertTrue(output.contains("EOF('eof')"));
        Assertions.assertEquals("", errBuffer.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void astCommandPrintsProgramString() throws Exception {
        Path source = tempDir.resolve("ast.monkey");
        Files.writeString(source, "let x = 5;", StandardCharsets.UTF_8);

        ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();

        int exitCode = new CliRunner().run(
                new String[]{"--ast", source.toString()},
                new PrintStream(outBuffer, true, StandardCharsets.UTF_8),
                new PrintStream(errBuffer, true, StandardCharsets.UTF_8)
        );

        Assertions.assertEquals(0, exitCode);
        Assertions.assertEquals("let x = 5;\n", outBuffer.toString(StandardCharsets.UTF_8));
        Assertions.assertEquals("", errBuffer.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void invalidArgumentsPrintUsageAndReturnError() {
        ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();

        int exitCode = new CliRunner().run(
                new String[]{"run"},
                new PrintStream(outBuffer, true, StandardCharsets.UTF_8),
                new PrintStream(errBuffer, true, StandardCharsets.UTF_8)
        );

        Assertions.assertEquals(1, exitCode);
        Assertions.assertEquals("", outBuffer.toString(StandardCharsets.UTF_8));
        Assertions.assertEquals(
                "Usage: monkey [run <path> | --tokens <path> | --ast <path>]\n",
                errBuffer.toString(StandardCharsets.UTF_8)
        );
    }
}

package com.coolstuff;

import com.coolstuff.cli.MonkeyCommandRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class MainCliTest {

    @TempDir
    Path tempDir;

    @Test
    public void monkeyWithoutArgsStartsReplPath() {
        var result = runCommand(new String[]{}, ":exit\n");

        Assertions.assertEquals(0, result.exitCode());
        Assertions.assertEquals("", result.stderr());
        Assertions.assertTrue(result.stdout().contains("Welcome to the Monkey programming language!"));
        Assertions.assertTrue(result.stdout().contains("Feel free by typing commands"));
        Assertions.assertTrue(result.stdout().contains(">> "));
    }

    @Test
    public void runValidMonkeyFileSucceeds() throws Exception {
        var source = writeScript("valid.monkey", "1 + 2;\n");

        var result = runCommand(new String[]{"run", source.toString()}, "");

        Assertions.assertEquals(0, result.exitCode());
        Assertions.assertEquals("3\n", result.stdout());
        Assertions.assertEquals("", result.stderr());
    }


    @Test
    public void benchValidMonkeyFileSucceedsAndPrintsTiming() throws Exception {
        var source = writeScript("valid.monkey", "1 + 2;\n");

        var result = runCommand(new String[]{"bench", source.toString()}, "");

        Assertions.assertEquals(0, result.exitCode());
        Assertions.assertEquals("3\n", result.stdout());
        Assertions.assertTrue(result.stderr().startsWith("Execution time: "));
        Assertions.assertTrue(result.stderr().endsWith(" ms\n"));
    }

    @Test
    public void tokensModePrintsDeterministicTokenStreamWithPositions() throws Exception {
        var source = writeScript("valid.monkey", "let x = 5;");

        var result = runCommand(new String[]{"--tokens", source.toString()}, "");

        Assertions.assertEquals(0, result.exitCode());
        Assertions.assertEquals("""
                LET('let') @ 1:1
                IDENT('x') @ 1:5
                ASSIGN('=') @ 1:7
                INT('5') @ 1:9
                SEMICOLON(';') @ 1:10
                EOF('eof') @ 1:11
                """, result.stdout());
        Assertions.assertEquals("", result.stderr());
    }

    @Test
    public void astModePrintsDeterministicAstOutput() throws Exception {
        var source = writeScript("valid.monkey", "let x = 5;");

        var result = runCommand(new String[]{"--ast", source.toString()}, "");

        Assertions.assertEquals(0, result.exitCode());
        Assertions.assertEquals("""
                Program
                  LetStatement
                    Name
                      Identifier(x)
                    Value
                      IntegerLiteral(5)
                """, result.stdout());
        Assertions.assertEquals("", result.stderr());
    }

    @Test
    public void invalidArgumentsPrintUsageAndExitCodeTwo() {
        var result = runCommand(new String[]{"run"}, "");

        Assertions.assertEquals(2, result.exitCode());
        Assertions.assertEquals("", result.stdout());
        Assertions.assertEquals(
                "Usage: monkey [run <path> | bench <path> | --tokens <path> | --ast <path>]\n",
                result.stderr()
        );
    }

    @Test
    public void missingFilePrintsErrorAndExitCodeOne() {
        var missing = tempDir.resolve("missing.monkey");

        var result = runCommand(new String[]{"run", missing.toString()}, "");

        Assertions.assertEquals(1, result.exitCode());
        Assertions.assertEquals("", result.stdout());
        Assertions.assertEquals("File not found: " + missing + "\n", result.stderr());
    }

    @Test
    public void parseErrorsInScriptModePrintToStderrAndExitOne() throws Exception {
        var source = writeScript("parse-error.monkey", "let x = ;\n");

        var result = runCommand(new String[]{"run", source.toString()}, "");

        Assertions.assertEquals(1, result.exitCode());
        Assertions.assertEquals("", result.stdout());
        Assertions.assertTrue(result.stderr().startsWith("Parse errors in " + source));
        Assertions.assertTrue(result.stderr().contains("- no prefix parse function"));
    }

    @Test
    public void runtimeErrorsInScriptModePrintToStderrAndExitOne() throws Exception {
        var source = writeScript("runtime-error.monkey", "5 + true;\n");

        var result = runCommand(new String[]{"run", source.toString()}, "");

        Assertions.assertEquals(1, result.exitCode());
        Assertions.assertEquals("", result.stdout());
        Assertions.assertTrue(result.stderr().startsWith("Runtime error in " + source));
        Assertions.assertTrue(result.stderr().contains("TYPE_MISMATCH"));
    }

    private Path writeScript(String name, String source) throws Exception {
        var path = tempDir.resolve(name);
        Files.writeString(path, source, StandardCharsets.UTF_8);
        return path;
    }

    private TestResult runCommand(String[] args, String stdinText) {
        var in = new ByteArrayInputStream(stdinText.getBytes(StandardCharsets.UTF_8));
        var outBuffer = new ByteArrayOutputStream();
        var errBuffer = new ByteArrayOutputStream();

        var exitCode = new MonkeyCommandRunner().run(
                args,
                in,
                new PrintStream(outBuffer, true, StandardCharsets.UTF_8),
                new PrintStream(errBuffer, true, StandardCharsets.UTF_8)
        );

        return new TestResult(
                exitCode,
                outBuffer.toString(StandardCharsets.UTF_8),
                errBuffer.toString(StandardCharsets.UTF_8)
        );
    }

    record TestResult(int exitCode, String stdout, String stderr) {
    }
}

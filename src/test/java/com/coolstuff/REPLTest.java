package com.coolstuff;

import com.coolstuff.repl.REPL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;

public class REPLTest {

    @Test
    public void testRunUntilEofSmokeSession() {
        var output = runSession("1 + 2\n");

        Assertions.assertTrue(output.contains(">> "), "Expected REPL prompt in output");
        Assertions.assertTrue(output.contains("3"), "Expected expression result in output");
    }

    @Test
    public void testRunUntilEofHandlesParseErrors() {
        var output = runSession("let x = ;\n");

        Assertions.assertTrue(output.contains("Woops! We ran into some monkey business here!"));
        Assertions.assertTrue(output.contains("no prefix parse function for ; found"));
    }

    @Test
    public void testRunUntilEofHandlesEvaluationErrors() {
        var output = runSession("5 + true;\n");

        Assertions.assertTrue(output.contains("Error[TYPE_MISMATCH] at 1:3: Operation + not supported for types INTEGER and BOOLEAN"));
    }

    @Test
    public void testRunUntilEofStopsOnServiceCommand() {
        var output = runSession(":quit\n1 + 2\n");

        Assertions.assertTrue(output.contains(">> "));
        Assertions.assertFalse(output.contains("3"));
    }

    @Test
    public void stateIsPreservedAcrossInputs() {
        var output = runSession("let a = 5;\nlet b = a;\nb;\n");

        Assertions.assertTrue(output.contains("5"));
    }

    @Test
    public void sessionsAreIsolatedFromEachOther() {
        runSession("let a = 5;\n");
        var output = runSession("a;\n");

        Assertions.assertTrue(output.contains("Identifier not found: a"));
    }


    @Test
    public void multilineIfElseIsEvaluatedAsSingleUnit() {
        var output = runSession("""
                if (5 < 10) {
                return true;
                } else {
                return false;
                }
                """);

        Assertions.assertTrue(output.contains("true"));
        Assertions.assertFalse(output.contains("Woops! We ran into some monkey business here!"));
        Assertions.assertFalse(output.contains(".. "));
    }

    @Test
    public void mixedEndToEndFlowHandlesErrorsAndRecovers() {
        var output = runSession("""
                let threshold = 10;
                let value = 8;
                if (value < threshold) {
                value + 1;
                } else {
                value - 1;
                }
                unknown + 1;
                let recovered = threshold + value;
                recovered;
                """);

        Assertions.assertTrue(output.contains("9"));
        Assertions.assertTrue(output.contains("Identifier not found: unknown"));
        Assertions.assertTrue(output.contains("18"));
    }

    @Test
    public void testStartAcceptsServiceCommandTermination() {
        byte[] input = ":exit\n".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(input);
        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outputBuffer, true, StandardCharsets.UTF_8);

        REPL repl = new REPL(in, out);
        repl.start();

        String output = normalizeLineEndings(outputBuffer.toString(StandardCharsets.UTF_8));
        Assertions.assertEquals(">> ", output);
    }

    @Test
    public void testStartKeepsInteractiveBehaviorAndThrowsOnEof() {
        byte[] input = "1 + 2\n".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(input);
        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outputBuffer, true, StandardCharsets.UTF_8);

        REPL repl = new REPL(in, out);

        Assertions.assertThrows(NoSuchElementException.class, repl::start);

        String output = normalizeLineEndings(outputBuffer.toString(StandardCharsets.UTF_8));
        Assertions.assertTrue(output.contains(">> "));
        Assertions.assertTrue(output.contains("3"));
    }

    @Test
    public void testStartPrintsPromptBeforeReadingInput() throws Exception {
        PipedOutputStream writer = new PipedOutputStream();
        PipedInputStream in = new PipedInputStream(writer);
        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outputBuffer, true, StandardCharsets.UTF_8);

        REPL repl = new REPL(in, out);
        Thread replThread = Thread.ofVirtual().start(() -> Assertions.assertThrows(NoSuchElementException.class, repl::start));

        waitUntilOutputContains(outputBuffer, ">> ");
        writer.write("if (5 < 10) {\n".getBytes(StandardCharsets.UTF_8));
        writer.flush();

        waitUntilOutputContains(outputBuffer, ".. ");
        writer.write("return true;\n}\n".getBytes(StandardCharsets.UTF_8));
        writer.close();
        replThread.join();

        String output = normalizeLineEndings(outputBuffer.toString(StandardCharsets.UTF_8));
        Assertions.assertTrue(output.contains(".. "));
        Assertions.assertTrue(output.contains("true"));
    }


    @Test
    public void multilinePastedInputDoesNotPrintContinuationPrompt() {
        var output = runSession("""
                let range = fn(start, end) {
                 let iter = fn(curr, acc) {
                   if (curr > end) {
                     acc;
                   } else {
                     iter(curr + 1, push(acc, curr));
                   }
                 };
                 iter(start, []);
               };
               range(1,3);
                """);

        Assertions.assertFalse(output.contains(".. "));
        Assertions.assertTrue(output.contains("[1, 2, 3]"));
    }

    @Test
    public void lineByLineInputShowsContinuationPrompt() throws Exception {
        PipedOutputStream writer = new PipedOutputStream();
        PipedInputStream in = new PipedInputStream(writer);
        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outputBuffer, true, StandardCharsets.UTF_8);

        REPL repl = new REPL(in, out);
        Thread replThread = Thread.ofVirtual().start(() -> Assertions.assertThrows(NoSuchElementException.class, repl::start));

        waitUntilOutputContains(outputBuffer, ">> ");
        writer.write("if (5 < 10) {\n".getBytes(StandardCharsets.UTF_8));
        writer.flush();

        waitUntilOutputContains(outputBuffer, ".. ");
        writer.write("return true;\n}\n".getBytes(StandardCharsets.UTF_8));
        writer.close();
        replThread.join();

        String output = normalizeLineEndings(outputBuffer.toString(StandardCharsets.UTF_8));
        Assertions.assertTrue(output.contains(".. "));
        Assertions.assertTrue(output.contains("true"));
    }


    @Test
    public void incompleteInputDoesNotPrintParseErrorBeforeCompletion() throws Exception {
        PipedOutputStream writer = new PipedOutputStream();
        PipedInputStream in = new PipedInputStream(writer);
        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outputBuffer, true, StandardCharsets.UTF_8);

        REPL repl = new REPL(in, out);
        Thread replThread = Thread.ofVirtual().start(() -> Assertions.assertThrows(NoSuchElementException.class, repl::start));

        waitUntilOutputContains(outputBuffer, ">> ");
        writer.write("if (5 < 10) {\n".getBytes(StandardCharsets.UTF_8));
        writer.flush();

        waitUntilOutputContains(outputBuffer, ".. ");
        String outputAfterFirstLine = normalizeLineEndings(outputBuffer.toString(StandardCharsets.UTF_8));
        Assertions.assertFalse(outputAfterFirstLine.contains("Woops! We ran into some monkey business here!"));

        writer.write("5;\n}\n".getBytes(StandardCharsets.UTF_8));
        writer.close();
        replThread.join();

        String output = normalizeLineEndings(outputBuffer.toString(StandardCharsets.UTF_8));
        Assertions.assertFalse(output.contains("Woops! We ran into some monkey business here!"));
        Assertions.assertTrue(output.contains("5"));
    }

    @Test
    public void parseErrorShownAfterCompleteMultilineInput() throws Exception {
        PipedOutputStream writer = new PipedOutputStream();
        PipedInputStream in = new PipedInputStream(writer);
        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outputBuffer, true, StandardCharsets.UTF_8);

        REPL repl = new REPL(in, out);
        Thread replThread = Thread.ofVirtual().start(() -> Assertions.assertThrows(NoSuchElementException.class, repl::start));

        waitUntilOutputContains(outputBuffer, ">> ");
        writer.write("if (true) {\n".getBytes(StandardCharsets.UTF_8));
        writer.flush();
        waitUntilOutputContains(outputBuffer, ".. ");

        String outputBeforeCompletion = normalizeLineEndings(outputBuffer.toString(StandardCharsets.UTF_8));
        Assertions.assertFalse(outputBeforeCompletion.contains("Woops! We ran into some monkey business here!"));

        writer.write("let value = ;\n}\n".getBytes(StandardCharsets.UTF_8));
        writer.close();
        replThread.join();

        String output = normalizeLineEndings(outputBuffer.toString(StandardCharsets.UTF_8));
        Assertions.assertEquals(1, countOccurrences(output, "Woops! We ran into some monkey business here!"));
        Assertions.assertTrue(output.contains("no prefix parse function for ; found"));
    }

    @Test
    public void evaluationErrorDoesNotBreakSession() {
        var output = runSession("5 + true;\n10;\n");

        Assertions.assertTrue(output.contains("Error[TYPE_MISMATCH] at 1:3: Operation + not supported for types INTEGER and BOOLEAN"));
        Assertions.assertTrue(output.contains("10"));
    }

    @Test
    public void rendersStructuredRuntimeErrorWithStackTrace() {
        var output = runSession("let add = fn(x, y) { x + y; }; add(1, true);\n");

        Assertions.assertTrue(output.contains("Error[TYPE_MISMATCH]"));
        Assertions.assertTrue(output.contains("Stack trace:"));
        Assertions.assertTrue(output.contains("at add(2 args)"));
        Assertions.assertTrue(output.contains("at <repl>(0 args) @ 1:1"));
    }


    @Test
    public void helpCommandIncludesAllSupportedCommands() {
        var output = runSession(":help\n");

        Assertions.assertTrue(output.contains(":help"));
        Assertions.assertTrue(output.contains(":tokens"));
        Assertions.assertTrue(output.contains(":ast"));
        Assertions.assertTrue(output.contains(":env"));
        Assertions.assertTrue(output.contains(":quit / :exit"));
    }

    @Test
    public void tokensCommandPrintsDeterministicTokenStream() {
        var output = runSession(":tokens let x = 5;\n");

        Assertions.assertTrue(output.contains("TOKENS:"));
        Assertions.assertTrue(output.contains("LET('let') @ 1:1"));
        Assertions.assertTrue(output.contains("IDENT('x') @ 1:5"));
        Assertions.assertTrue(output.contains("ASSIGN('=') @ 1:7"));
        Assertions.assertTrue(output.contains("INT('5') @ 1:9"));
        Assertions.assertTrue(output.contains("SEMICOLON(';') @ 1:10"));
        Assertions.assertTrue(output.contains("EOF('eof') @ 1:11"));
    }

    @Test
    public void astCommandPrintsProgramStringAndParseErrors() {
        var astOutput = runSession(":ast let x = 5;\n");
        Assertions.assertTrue(astOutput.contains("AST:"));
        Assertions.assertTrue(astOutput.contains("let x = 5;"));

        var errorOutput = runSession(":ast let x = ;\n");
        Assertions.assertTrue(errorOutput.contains("Woops! We ran into some monkey business here!"));
        Assertions.assertTrue(errorOutput.contains("no prefix parse function for ; found"));
    }

    @Test
    public void envCommandShowsPersistedBindingsAcrossInputs() {
        var output = runSession("let a = 5;\nlet b = a + 2;\n:env\n");

        Assertions.assertTrue(output.contains("ENV:"));
        Assertions.assertTrue(output.contains("a = 5"));
        Assertions.assertTrue(output.contains("b = 7"));
    }

    @Test
    public void metaCommandsDoNotMutateEvaluatorStateUnexpectedly() {
        var output = runSession("let a = 5;\n:tokens a + 1;\na;\n");

        Assertions.assertTrue(output.contains("TOKENS:"));
        Assertions.assertTrue(output.contains("5"));
        Assertions.assertFalse(output.contains("Identifier not found: a"));
    }

    @Test
    public void mixedSessionWithDebugCommandsAndRecoveryWorks() {
        var output = runSession("""
                let base = 2;
                :env
                :tokens base + 3;
                :ast let total = base + 1;
                unknown;
                let recovered = base + 40;
                :env
                recovered;
                """);

        Assertions.assertTrue(output.contains("ENV:"));
        Assertions.assertTrue(output.contains("TOKENS:"));
        Assertions.assertTrue(output.contains("AST:"));
        Assertions.assertTrue(output.contains("Identifier not found: unknown"));
        Assertions.assertTrue(output.contains("recovered = 42"));
        Assertions.assertTrue(output.contains("42"));
    }

    @Test
    public void metaCommandsAreBlockedDuringMultilineBuffering() {
        var output = runSession("if (true) {\n:env\n5;\n}\n");

        Assertions.assertTrue(output.contains("Meta-commands are only allowed when the multiline buffer is empty."));
        Assertions.assertTrue(output.contains("5"));
    }

    private String runSession(String input) {
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outputBuffer, true, StandardCharsets.UTF_8);

        REPL repl = new REPL(in, out);
        repl.runUntilEof();

        return normalizeLineEndings(outputBuffer.toString(StandardCharsets.UTF_8));
    }

    private void waitUntilOutputContains(ByteArrayOutputStream outputBuffer, String expectedText) throws InterruptedException {
        for (int attempts = 0; attempts < 50; attempts++) {
            String output = normalizeLineEndings(outputBuffer.toString(StandardCharsets.UTF_8));
            if (output.contains(expectedText)) {
                return;
            }
            Thread.sleep(10);
        }

        Assertions.fail("Expected output to contain: " + expectedText);
    }

    private int countOccurrences(String text, String value) {
        var count = 0;
        var index = 0;
        while ((index = text.indexOf(value, index)) != -1) {
            count++;
            index += value.length();
        }
        return count;
    }

    private String normalizeLineEndings(String text) {
        return text.replace("\r\n", "\n");
    }

}

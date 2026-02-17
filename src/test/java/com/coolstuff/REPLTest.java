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

        Assertions.assertTrue(output.contains("Error evaluating the program: Operation + not supported for types INTEGER and BOOLEAN"));
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
    public void testStartKeepsInteractiveBehaviorAndThrowsOnEof() {
        byte[] input = "1 + 2\n".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(input);
        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outputBuffer, true, StandardCharsets.UTF_8);

        REPL repl = new REPL(in, out);

        Assertions.assertThrows(NoSuchElementException.class, repl::start);

        String output = outputBuffer.toString(StandardCharsets.UTF_8);
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
        writer.write("1 + 2\n".getBytes(StandardCharsets.UTF_8));
        writer.close();
        replThread.join();

        String output = outputBuffer.toString(StandardCharsets.UTF_8);
        Assertions.assertTrue(output.contains(">> 3"));
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

        String output = outputBuffer.toString(StandardCharsets.UTF_8);
        Assertions.assertTrue(output.contains(".. "));
        Assertions.assertTrue(output.contains("true"));
    }

    private String runSession(String input) {
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outputBuffer, true, StandardCharsets.UTF_8);

        REPL repl = new REPL(in, out);
        repl.runUntilEof();

        return outputBuffer.toString(StandardCharsets.UTF_8);
    }

    private void waitUntilOutputContains(ByteArrayOutputStream outputBuffer, String expectedText) throws InterruptedException {
        for (int attempts = 0; attempts < 50; attempts++) {
            String output = outputBuffer.toString(StandardCharsets.UTF_8);
            if (output.contains(expectedText)) {
                return;
            }
            Thread.sleep(10);
        }

        Assertions.fail("Expected output to contain: " + expectedText);
    }

}

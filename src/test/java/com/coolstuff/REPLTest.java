package com.coolstuff;

import com.coolstuff.repl.REPL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

    private String runSession(String input) {
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outputBuffer, true, StandardCharsets.UTF_8);

        REPL repl = new REPL(in, out);
        repl.runUntilEof();

        return outputBuffer.toString(StandardCharsets.UTF_8);
    }
}

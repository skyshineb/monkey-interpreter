package com.coolstuff;

import com.coolstuff.repl.REPL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class REPLTest {

    @Test
    public void testRunUntilEofSmokeSession() {
        byte[] input = "1 + 2\n".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(input);
        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outputBuffer, true, StandardCharsets.UTF_8);

        REPL repl = new REPL(in, out);
        repl.runUntilEof();

        String output = outputBuffer.toString(StandardCharsets.UTF_8);
        Assertions.assertTrue(output.contains(">> "), "Expected REPL prompt in output");
        Assertions.assertTrue(output.contains("3"), "Expected expression result in output");
    }
}

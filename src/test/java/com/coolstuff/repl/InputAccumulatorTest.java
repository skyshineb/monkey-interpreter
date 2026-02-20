package com.coolstuff.repl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class InputAccumulatorTest {

    private final InputAccumulator inputAccumulator = new InputAccumulator();

    @Test
    void incompleteWhenCurlyBraceIsNotClosed() {
        Assertions.assertFalse(inputAccumulator.isInputComplete("let a = {"));
    }

    @Test
    void completeForClosedNestedConstruction() {
        Assertions.assertTrue(inputAccumulator.isInputComplete("if ((1 < 2) && true) { [1, {\"a\": 1}] }"));
    }

    @Test
    void ignoresBracketsInsideString() {
        Assertions.assertTrue(inputAccumulator.isInputComplete("let text = \"{[(not a bracket)]}\";"));
    }

    @Test
    void handlesEscapedQuotesInsideString() {
        Assertions.assertTrue(inputAccumulator.isInputComplete("let quote = \"He said: \\\"hi\\\"\";"));
    }

    @Test
    void unmatchedClosingDelimiterCompletesInputImmediately() {
        Assertions.assertTrue(inputAccumulator.isInputComplete("){"));
    }

    @Test
    void unmatchedClosingDelimiterInsideStringDoesNotCompleteInput() {
        Assertions.assertFalse(inputAccumulator.isInputComplete("let a = \"}\" + {"));
    }

    @Test
    void multilineFunctionLiteralCompletesAfterClosingBrace() {
        Assertions.assertFalse(inputAccumulator.isInputComplete("let add = fn(x, y) {\nx + y;"));
        Assertions.assertTrue(inputAccumulator.isInputComplete("let add = fn(x, y) {\nx + y;\n};"));
    }

    @Test
    void nestedWhileAndFunctionBlocksAreTracked() {
        Assertions.assertFalse(inputAccumulator.isInputComplete("while (x < 10) {\nlet next = fn(y) {\ny + 1;\n};"));
        Assertions.assertTrue(inputAccumulator.isInputComplete("while (x < 10) {\nlet next = fn(y) {\ny + 1;\n};\n}"));
    }

    @Test
    void bracesInsideMultilineStringDoNotAffectCompleteness() {
        Assertions.assertTrue(inputAccumulator.isInputComplete("let template = \"line1\\n{line2 [x]}\\n\";"));
    }
}

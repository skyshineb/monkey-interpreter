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
}

package com.coolstuff.cli;

import com.coolstuff.evaluator.Evaluator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MonkeyPipelineTest {

    @Test
    public void tokenStreamIncludesEofToken() {
        var pipeline = new MonkeyPipeline();

        var lines = pipeline.tokenStream("let x = 5;");

        Assertions.assertTrue(lines.getFirst().contains("LET('let')"));
        Assertions.assertTrue(lines.getLast().contains("EOF('eof')"));
    }

    @Test
    public void parseProgramReturnsErrorsForInvalidInput() {
        var pipeline = new MonkeyPipeline();

        var result = pipeline.parseProgram("let x = ;");

        Assertions.assertFalse(result.errors().isEmpty());
    }

    @Test
    public void evaluateReturnsValueOnHappyPath() {
        var pipeline = new MonkeyPipeline();

        var result = pipeline.evaluate("1 + 2;", new Evaluator());

        Assertions.assertFalse(result.hasParseErrors());
        Assertions.assertFalse(result.hasEvaluationError());
        Assertions.assertEquals("3", result.value().inspect());
    }

    @Test
    public void evaluateReturnsEvaluationErrorOnRuntimeFailure() {
        var pipeline = new MonkeyPipeline();

        var result = pipeline.evaluate("unknown;", new Evaluator());

        Assertions.assertTrue(result.hasEvaluationError());
        Assertions.assertTrue(result.evaluationException().getRuntimeError().message().contains("Identifier not found"));
    }
}

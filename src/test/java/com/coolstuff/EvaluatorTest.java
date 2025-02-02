package com.coolstuff;

import com.coolstuff.evaluator.EvaluationException;
import com.coolstuff.evaluator.Evaluator;
import com.coolstuff.evaluator.object.MonkeyBoolean;
import com.coolstuff.evaluator.object.MonkeyInteger;
import com.coolstuff.evaluator.object.MonkeyObject;
import com.coolstuff.lexer.Lexer;
import com.coolstuff.parser.Parser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class EvaluatorTest {

    private record EvalIntegerTestCase(String input, Long expected) {}

    @Test
    public void testEvalIntegerExpression() throws EvaluationException {
        var tests = List.of(
                new EvalIntegerTestCase("5", 5L),
                new EvalIntegerTestCase("10", 10L),
                new EvalIntegerTestCase("-5", -5L),
                new EvalIntegerTestCase("-10", -10L)
        );

        for (var test : tests) {
            var evaluated = testEval(test.input);
            testIntegerObject(evaluated, test.expected);
        }
    }

    private record EvalBooleanTestCase(String input, boolean expected) {}

    @Test
    public void testEvalBooleanExpression() throws EvaluationException {
        var tests = List.of(
            new EvalBooleanTestCase("true", true),
            new EvalBooleanTestCase("false", false)
        );

        for (var test : tests) {
            var evaluated = testEval(test.input);
            testBooleanObject(evaluated, test.expected);
        }
    }

    private MonkeyObject<?> testEval(String input) throws EvaluationException {
        var l = new Lexer(input);
        var p = new Parser(l);
        var evaluator = new Evaluator();

        return evaluator.eval(p.parseProgram());
    }

    @Test
    public void testBangOperator() throws EvaluationException {
        var tests = List.of(
                new EvalBooleanTestCase("!true", false),
                new EvalBooleanTestCase("!false", true),
                new EvalBooleanTestCase("!5", false),
                new EvalBooleanTestCase("!!true", true),
                new EvalBooleanTestCase("!!false", false),
                new EvalBooleanTestCase("!!5", true)
        );

        for (var test : tests) {
            var evaluated = testEval(test.input);
            testBooleanObject(evaluated, test.expected);
        }
    }

    private void testBooleanObject(MonkeyObject<?> monkeyObject, boolean expected) {
        Assertions.assertInstanceOf(MonkeyBoolean.class, monkeyObject);
        Assertions.assertEquals(expected, monkeyObject.getObject());
    }

    public void testIntegerObject(MonkeyObject<?> monkeyObject, Long expected) {
        Assertions.assertInstanceOf(MonkeyInteger.class, monkeyObject);
        Assertions.assertEquals(expected, monkeyObject.getObject());
    }
}

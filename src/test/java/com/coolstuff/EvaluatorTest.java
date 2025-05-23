package com.coolstuff;

import com.coolstuff.evaluator.EvaluationException;
import com.coolstuff.evaluator.Evaluator;
import com.coolstuff.evaluator.object.*;
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
                new EvalIntegerTestCase("-10", -10L),
                new EvalIntegerTestCase("5 + 5 + 5 + 5 - 10", 10L),
                new EvalIntegerTestCase("2 * 2 * 2 * 2 * 2", 32L),
                new EvalIntegerTestCase("-50 + 100 + -50", 0L),
                new EvalIntegerTestCase("5 * 2 + 10", 20L),
                new EvalIntegerTestCase("5 + 2 * 10", 25L),
                new EvalIntegerTestCase("20 + 2 * -10", 0L),
                new EvalIntegerTestCase("50 / 2 * 2 + 10", 60L),
                new EvalIntegerTestCase("2 * (5 + 10)", 30L),
                new EvalIntegerTestCase("3 * 3 * 3 + 10", 37L),
                new EvalIntegerTestCase("3 * (3 * 3) + 10", 37L),
                new EvalIntegerTestCase("(5 + 10 * 2 + 15 / 3) * 2 + -10", 50L)
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
            new EvalBooleanTestCase("false", false),
            new EvalBooleanTestCase("1 < 2", true),
            new EvalBooleanTestCase("1 > 2", false),
            new EvalBooleanTestCase("1 < 1", false),
            new EvalBooleanTestCase("1 > 1", false),
            new EvalBooleanTestCase("1 == 1", true),
            new EvalBooleanTestCase("1 != 1", false),
            new EvalBooleanTestCase("1 == 2", false),
            new EvalBooleanTestCase("1 != 2", true),
            new EvalBooleanTestCase("true == true", true),
            new EvalBooleanTestCase("false == false", true),
            new EvalBooleanTestCase("true == false", false),
            new EvalBooleanTestCase("true != false", true),
            new EvalBooleanTestCase("false != true", true),
            new EvalBooleanTestCase("(1 < 2) == true", true),
            new EvalBooleanTestCase("(1 < 2) == false", false),
            new EvalBooleanTestCase("(1 > 2) == true", false),
            new EvalBooleanTestCase("(1 > 2) == false", true)
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

    private record IfElseExpressionTestCase(String input, Object expected) {}

    @Test
    public void testIfElseExpressions() throws EvaluationException {
        var tests = List.of(
                new IfElseExpressionTestCase("if (true) { 10 }", 10L),
                new IfElseExpressionTestCase("if (false) { 10 }", null),
                new IfElseExpressionTestCase("if (1) { 10 }", 10L),
                new IfElseExpressionTestCase("if (1 < 2) { 10 }", 10L),
                new IfElseExpressionTestCase("if (1 > 2) { 10 }", null),
                new IfElseExpressionTestCase("if (1 > 2) { 10 } else { 20 }", 20L),
                new IfElseExpressionTestCase("if (1 < 2) { 10 } else { 20 }", 10L)
        );

        for (var test : tests) {
            var evaluated = testEval(test.input);
            testObject(evaluated, test.expected);
        }
    }

    @Test
    public void testReturnStatements() throws EvaluationException {
        var tests = List.of(
                new EvalIntegerTestCase("return 10;", 10L),
                new EvalIntegerTestCase("return 10; 9;", 10L),
                new EvalIntegerTestCase("return 2 * 5; 9;", 10L),
                new EvalIntegerTestCase("9; return 2 * 5; 9;", 10L)
        );

        for (var test : tests) {
            var evaluated = testEval(test.input);
            testIntegerObject(evaluated, test.expected);
        }
    }

    @Test
    public void testErrorHandling() {
        var tests = List.of(
                List.of(
                        "5 + true;",
                        "Error evaluating the program: Operation + not supported for types INTEGER and BOOLEAN"
                ),
                List.of(
                        "5 + true; 5;",
                        "Error evaluating the program: Operation + not supported for types INTEGER and BOOLEAN"
                ),
                List.of(
                        "-true",
                        "Error evaluating the program: Operation - not supported for type BOOLEAN"
                ),
                List.of(
                        "true + false;",
                        "Error evaluating the program: Operation + not supported for types BOOLEAN and BOOLEAN"
                ),
                List.of(
                        "5; true + false; 5",
                        "Error evaluating the program: Operation + not supported for types BOOLEAN and BOOLEAN"
                ),
                List.of(
                        "if (10 > 1) { true + false; }",
                        "Error evaluating the program: Operation + not supported for types BOOLEAN and BOOLEAN"
                ),
                List.of(
                        """
                                if (10 > 1) {
                                    if (10 > 1) {
                                        return true + false;
                                    }
                                    return 1;
                                }""",
                        "Error evaluating the program: Operation + not supported for types BOOLEAN and BOOLEAN"
                ),
                List.of(
                        "foobar",
                        "Error evaluating the program: Identifier not found: foobar"
                        ),
                List.of(
                        "\"Hello\" - \"World\"",
                        "Error evaluating the program: Operation - not supported for types STRING and STRING"
                )
        );

        for (var test : tests) {
            EvaluationException exception = null;
            System.out.print(test.getFirst());
            try {
                testEval(test.getFirst());
            } catch (EvaluationException e) {
                exception = e;
            }
            Assertions.assertNotNull(exception);
            Assertions.assertEquals(test.get(1), exception.getMessage());
            System.out.println(" ---> OK");
        }
    }

    private record LetTestCase(String input, Long expected) {}

    @Test
    public void testLetStatements() throws EvaluationException {
        var tests = List.of(
                new LetTestCase("let a = 5; a;", 5L),
                new LetTestCase("let a = 5 * 5; a;", 25L),
                new LetTestCase("let a = 5; let b = a; b;", 5L),
                new LetTestCase("let a = 5; let b = a; let c = a + b + 5; c;", 15L)
        );

        for (var test : tests) {
            var evaluated = testEval(test.input);
            testIntegerObject(evaluated, test.expected);
        }
    }

    @Test
    public void testFunctionObject() throws EvaluationException {
        var input = "fn(x){ x + 2; } ;";
        var evaluated = testEval(input);

        Assertions.assertInstanceOf(MonkeyFunction.class, evaluated);
        var function = (MonkeyFunction) evaluated;
        Assertions.assertEquals(1, function.getFunctionLiteral().parameters().length);
        Assertions.assertEquals("x" ,function.getFunctionLiteral().parameters()[0].value());

        var expectedBody = "(x + 2)";

        Assertions.assertEquals(expectedBody, function.getFunctionLiteral().body().string());
    }

    private record FunctionApplicationTestCase(String input, Long expected) {}

    @Test
    public void testFunctionApplication() throws EvaluationException {
        var tests = List.of(
                new FunctionApplicationTestCase("let identity = fn(x) { x; }; identity(5);", 5L),
                new FunctionApplicationTestCase("let identity = fn(x) { return x; }; identity(5);", 5L),
                new FunctionApplicationTestCase("let double = fn(x) { x * 2; }; double(5);", 10L),
                new FunctionApplicationTestCase("let add = fn(x, y) { x + y; }; add(5, 5);", 10L),
                new FunctionApplicationTestCase("let add = fn(x, y) { x + y; }; add(5 + 5, add(5, 5));", 20L),
                new FunctionApplicationTestCase("fn(x) { x; }(5)", 5L)
        );

        for (var test : tests) {
            var evaluated = testEval(test.input);
            testIntegerObject(evaluated, test.expected);
        }
    }

    @Test
    public void testClosures() throws EvaluationException {
        var input = """
                let newAdder = fn(x) {
                fn(y) { x + y };
                };
                let addTwo = newAdder(2);
                addTwo(2);""";

        var evaluated = testEval(input);
        testIntegerObject(evaluated, 4L);
    }

    @Test
    public void testStrings() throws EvaluationException {
        var input = "\"Hello World!\"";
        var evaluated = testEval(input);
        testStringObject(evaluated, "Hello World!");
    }

    @Test
    public void testStringConcatenation() throws EvaluationException {
        var input = "\"Hello\" + \"World\"";
        var evaluated = testEval(input);
        testStringObject(evaluated, "HelloWorld");
    }


    private record BuiltInFunctionsTestCase(String input, Object expected) {}
    @Test
    public void testBuiltInFunctions() throws EvaluationException {
        var tests = List.of(
                new BuiltInFunctionsTestCase("len(\"\")", 0L),
                new BuiltInFunctionsTestCase("len(\"four\")", 4L),
                new BuiltInFunctionsTestCase("len(\"hello world\")", 11L),
                new BuiltInFunctionsTestCase("len(1)", "Error evaluating the program: Argument to `len` not supported, got INTEGER"),
                new BuiltInFunctionsTestCase("len(\"one\", \"two\")", "Error evaluating the program: Wrong number of arguments. Expected 1, got 2")
        );

        for (var test : tests) {
            try {
                var evaluated = testEval(test.input);
                testObject(evaluated, test.expected);
            } catch (EvaluationException e) {
                Assertions.assertEquals(test.expected, e.getMessage());
            }

        }
    }

    private void testStringObject(MonkeyObject<?> monkeyObject, Object expected) {
        Assertions.assertInstanceOf(MonkeyString.class, monkeyObject);
        Assertions.assertEquals(expected, monkeyObject.getObject());
    }

    private void testObject(MonkeyObject<?> evaluated, Object expected) {
        switch (evaluated.getType()) {
            case INTEGER -> testIntegerObject(evaluated, (Long) expected);
            case BOOLEAN -> testBooleanObject(evaluated, (Boolean) expected);
            case NULL -> testNullObject(evaluated);
            default -> throw new IllegalArgumentException("Not expecting here " + evaluated.getType());
        }
    }

    private void testNullObject(MonkeyObject<?> monkeyObject) {
        Assertions.assertInstanceOf(MonkeyNull.class, monkeyObject);
        Assertions.assertNull(monkeyObject.getObject());
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

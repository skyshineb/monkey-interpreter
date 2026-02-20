package com.coolstuff;

import com.coolstuff.evaluator.EvaluationException;
import com.coolstuff.evaluator.Evaluator;
import com.coolstuff.evaluator.HashKey;
import com.coolstuff.evaluator.RuntimeErrorType;
import com.coolstuff.evaluator.object.*;
import com.coolstuff.lexer.Lexer;
import com.coolstuff.parser.Parser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
            new EvalBooleanTestCase("1 <= 1", true),
            new EvalBooleanTestCase("1 <= 0", false),
            new EvalBooleanTestCase("2 >= 1", true),
            new EvalBooleanTestCase("2 >= 3", false),
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
            new EvalBooleanTestCase("(1 > 2) == false", true),
            new EvalBooleanTestCase("true && true", true),
            new EvalBooleanTestCase("true && false", false),
            new EvalBooleanTestCase("false || true", true),
            new EvalBooleanTestCase("false || false", false)
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
    public void testShortCircuitBooleanExpressions() throws EvaluationException {
        var tests = List.of(
                new EvalBooleanTestCase("false && (1 / 0 > 0)", false),
                new EvalBooleanTestCase("true || (1 / 0 > 0)", true)
        );

        for (var test : tests) {
            var evaluated = testEval(test.input);
            testBooleanObject(evaluated, test.expected);
        }
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
                new IfElseExpressionTestCase("if (1 < 2) { 10 } else { 20 }", 10L),
                new IfElseExpressionTestCase("if (false) { 1 } else if (true) { 2 }", 2L),
                new IfElseExpressionTestCase("if (false) {1} else if (false) {2} else {3}", 3L),
                new IfElseExpressionTestCase("if (true) {1} else if (true) {2} else {3}", 1L),
                new IfElseExpressionTestCase("if (false) {1} else if (false) {2} else if (true) {3} else if (false) {4} else {5}", 3L)
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
    public void testWhileLoopAccumulation() throws EvaluationException {
        var input = "let i = 0; let sum = 0; while (i < 5) { let sum = sum + i; let i = i + 1; } sum;";

        var evaluated = testEval(input);
        testIntegerObject(evaluated, 10L);
    }

    @Test
    public void testWhileLoopContinue() throws EvaluationException {
        var input = "let i = 0; let sum = 0; while (i < 6) { let i = i + 1; if (i == 3) { continue; } let sum = sum + i; } sum;";

        var evaluated = testEval(input);
        testIntegerObject(evaluated, 18L);
    }

    @Test
    public void testWhileLoopContinueDoesNotLeakControlSignal() throws EvaluationException {
        var input = "let i = 0; while (i < 1) { let i = i + 1; continue; } 99;";

        var evaluated = testEval(input);
        testIntegerObject(evaluated, 99L);
    }

    @Test
    public void testWhileLoopBreak() throws EvaluationException {
        var input = "let i = 0; while (i < 10) { let i = i + 1; if (i == 4) { break; } } i;";

        var evaluated = testEval(input);
        testIntegerObject(evaluated, 4L);
    }

    @Test
    public void testNestedWhileBreakAffectsNearestLoop() throws EvaluationException {
        var input = "let outer = 0; while (outer < 3) { let outer = outer + 1; let inner = 0; while (inner < 10) { let inner = inner + 1; break; } } outer;";

        var evaluated = testEval(input);
        testIntegerObject(evaluated, 3L);
    }

    @Test
    public void testReturnInsideWhileInsideFunction() throws EvaluationException {
        var input = "let loop = fn() { let i = 0; while (i < 5) { if (i == 3) { return i; } let i = i + 1; } return 99; }; loop();";

        var evaluated = testEval(input);
        testIntegerObject(evaluated, 3L);
    }

    @Test
    public void testBreakAndContinueOutsideLoopErrors() {
        var tests = List.of(
                List.of("break;", RuntimeErrorType.INVALID_CONTROL_FLOW, "`break` not allowed outside loop"),
                List.of("continue;", RuntimeErrorType.INVALID_CONTROL_FLOW, "`continue` not allowed outside loop")
        );

        for (var test : tests) {
            var exception = Assertions.assertThrows(EvaluationException.class, () -> testEval((String) test.getFirst()));
            Assertions.assertEquals(test.get(1), exception.getRuntimeError().type());
            Assertions.assertEquals(test.get(2), exception.getRuntimeError().message());
        }
    }

    @Test
    public void testErrorHandling() {
        var tests = List.of(
                List.of("5 + true;", RuntimeErrorType.TYPE_MISMATCH, "Operation + not supported for types INTEGER and BOOLEAN"),
                List.of("5 + true; 5;", RuntimeErrorType.TYPE_MISMATCH, "Operation + not supported for types INTEGER and BOOLEAN"),
                List.of("-true", RuntimeErrorType.TYPE_MISMATCH, "Operation - not supported for type BOOLEAN"),
                List.of("true + false;", RuntimeErrorType.TYPE_MISMATCH, "Operation + not supported for types BOOLEAN and BOOLEAN"),
                List.of("5; true + false; 5", RuntimeErrorType.TYPE_MISMATCH, "Operation + not supported for types BOOLEAN and BOOLEAN"),
                List.of("if (10 > 1) { true + false; }", RuntimeErrorType.TYPE_MISMATCH, "Operation + not supported for types BOOLEAN and BOOLEAN"),
                List.of("""
                                if (10 > 1) {
                                    if (10 > 1) {
                                        return true + false;
                                    }
                                    return 1;
                                }""", RuntimeErrorType.TYPE_MISMATCH, "Operation + not supported for types BOOLEAN and BOOLEAN"),
                List.of("foobar", RuntimeErrorType.UNKNOWN_IDENTIFIER, "Identifier not found: foobar"),
                List.of("\"Hello\" - \"World\"", RuntimeErrorType.UNSUPPORTED_OPERATION, "Operation - not supported for types STRING and STRING")
        );

        for (var test : tests) {
            var exception = Assertions.assertThrows(EvaluationException.class, () -> testEval((String) test.getFirst()));
            Assertions.assertEquals(test.get(1), exception.getRuntimeError().type());
            Assertions.assertEquals(test.get(2), exception.getRuntimeError().message());
        }
    }

    @Test
    public void testStructuredErrorsAndStackFrames() {
        var typeMismatch = Assertions.assertThrows(EvaluationException.class, () -> testEval("5 + true;"));
        Assertions.assertEquals(RuntimeErrorType.TYPE_MISMATCH, typeMismatch.getRuntimeError().type());
        Assertions.assertEquals(1, typeMismatch.getRuntimeError().position().line());
        Assertions.assertEquals(3, typeMismatch.getRuntimeError().position().column());

        var unknownIdentifier = Assertions.assertThrows(EvaluationException.class, () -> testEval("foobar"));
        Assertions.assertEquals(RuntimeErrorType.UNKNOWN_IDENTIFIER, unknownIdentifier.getRuntimeError().type());
        Assertions.assertEquals(1, unknownIdentifier.getRuntimeError().position().line());
        Assertions.assertEquals(1, unknownIdentifier.getRuntimeError().position().column());

        var notCallable = Assertions.assertThrows(EvaluationException.class, () -> testEval("let a = 5; a(1);"));
        Assertions.assertEquals(RuntimeErrorType.NOT_CALLABLE, notCallable.getRuntimeError().type());

        var invalidHashKey = Assertions.assertThrows(EvaluationException.class, () -> testEval("{\"foo\": 5}[fn(x){x}]") );
        Assertions.assertEquals(RuntimeErrorType.INVALID_HASH_KEY, invalidHashKey.getRuntimeError().type());

        var breakMisuse = Assertions.assertThrows(EvaluationException.class, () -> testEval("break;"));
        Assertions.assertEquals(RuntimeErrorType.INVALID_CONTROL_FLOW, breakMisuse.getRuntimeError().type());

        var nested = Assertions.assertThrows(EvaluationException.class, () -> testEval("let c = fn(x){ x + true; }; let b = fn(x){ c(x); }; let a = fn(x){ b(x); }; a(1);"));
        Assertions.assertEquals(RuntimeErrorType.TYPE_MISMATCH, nested.getRuntimeError().type());
        Assertions.assertTrue(nested.getRuntimeError().stackFrames().size() >= 3);
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
                new BuiltInFunctionsTestCase("len(1)", "Argument to `len` not supported, got INTEGER"),
                new BuiltInFunctionsTestCase("len(\"one\", \"two\")", "Wrong number of arguments. Expected 1, got 2"),
                new BuiltInFunctionsTestCase("len([1,2])", 2L),
                new BuiltInFunctionsTestCase("first([1,2])", 1L),
                new BuiltInFunctionsTestCase("first([-5,2])", -5L),
                new BuiltInFunctionsTestCase("first([\"arra\",2])", "arra"),
                new BuiltInFunctionsTestCase("first([])", null),
                new BuiltInFunctionsTestCase("last([1,2,8])", 8L),
                new BuiltInFunctionsTestCase("last([-5])", -5L),
                new BuiltInFunctionsTestCase("last([\"arra\",2,\"a\"])", "a"),
                new BuiltInFunctionsTestCase("rest([\"arra\",2,\"a\"])", List.of(2L, "a")),
                new BuiltInFunctionsTestCase("rest([1])", List.of()),
                new BuiltInFunctionsTestCase("rest([])", null),
                new BuiltInFunctionsTestCase("rest([1 * 1, 2 * 2, 3 * 3])", List.of(4L, 9L)),
                new BuiltInFunctionsTestCase( "len(rest([1 * 1, 2 * 2, 3 * 3]))", 2L),
                new BuiltInFunctionsTestCase( "rest(rest(rest(rest([1 * 1, 2 * 2, 3 * 3]))))", null),
                new BuiltInFunctionsTestCase("push([], \"test\")", List.of("test")),
                new BuiltInFunctionsTestCase("push(push([], \"test\"), 28)", List.of("test", 28L)),
                new BuiltInFunctionsTestCase("let arr = []; push(arr, 10); arr", (List.of()))
        );

        for (var test : tests) {
            try {
                var evaluated = testEval(test.input);
                testObject(evaluated, test.expected);
            } catch (EvaluationException e) {
                Assertions.assertEquals(test.expected, e.getRuntimeError().message());
            }

        }
    }

    @Test
    public void testArrayLiterals() throws EvaluationException {
        var input = "[1, 2 * 2, 3 + 3]";

        var evaluated = testEval(input);
        var array = Assertions.assertInstanceOf(MonkeyArray.class, evaluated);
        Assertions.assertEquals(3, array.getObject().size());

        testIntegerObject(array.getObject().get(0), 1L);
        testIntegerObject(array.getObject().get(1), 4L);
        testIntegerObject(array.getObject().get(2), 6L);
    }

    private record ArrayIndexExpressionTestCase(String input, Object expected){}
    @Test
    public void testArrayIndexExpression() {
        var tests = List.of(
                new ArrayIndexExpressionTestCase("[1, 2, 3][0]", 1L),
                new ArrayIndexExpressionTestCase("[1, 2, 3][1]", 2L),
                new ArrayIndexExpressionTestCase("[1, 2, 3][2]", 3L),
                new ArrayIndexExpressionTestCase("let i = 0; [1][i];", 1L),
                new ArrayIndexExpressionTestCase("[1, 2, 3][1 + 1];", 3L),
                new ArrayIndexExpressionTestCase("let myArray = [1, 2, 3]; myArray[2];", 3L),
                new ArrayIndexExpressionTestCase("let myArray = [1, 2, 3]; myArray[0] + myArray[1] + myArray[2];", 6L),
                new ArrayIndexExpressionTestCase("let myArray = [1, 2, 3]; let i = myArray[0]; myArray[i]", 2L),
                new ArrayIndexExpressionTestCase("[1, 2, 3][3]", null),
                new ArrayIndexExpressionTestCase("[1, 2, 3][-1]", null)
        );

        for (var test : tests) {
            try {
                var evaluated = testEval(test.input);
                testObject(evaluated, test.expected);
            } catch (EvaluationException e) {
                Assertions.assertEquals(test.expected, e.getRuntimeError().message());
            }

        }
    }

    @Test
    public void testStringHashKey() {
        var hello1 = new MonkeyString("Hello world");
        var hello2 = new MonkeyString("Hello world");
        var diff1 = new MonkeyString("My name is johnny");
        var diff2 = new MonkeyString("My name is johnny");

        Assertions.assertEquals(hello1.hashKey(), hello2.hashKey());
        Assertions.assertEquals(diff1.hashKey(), diff2.hashKey());
        Assertions.assertNotEquals(hello1.hashKey(), diff1.hashKey());
    }

    @Test
    public void testHashLiterals() throws EvaluationException {
        var test = "let two = \"two\";\n" +
                "{\n" +
                "\"one\": 10 - 9,\n" +
                "two: 1 + 1,\n" +
                "\"thr\" + \"ee\": 6 / 2,\n" +
                "4: 4,\n" +
                "true: 5,\n" +
                "false: 6\n" +
                "}";
        var evaluated = testEval(test);
        var hashObj = Assertions.assertInstanceOf(MonkeyHash.class, evaluated);
        var expected = Map.of(
                new HashKey(new MonkeyString("one")), 1L,
                new HashKey(new MonkeyString("two")), 2L,
                new HashKey(new MonkeyString("three")), 3L,
                new HashKey(new MonkeyInteger(4L)), 4L,
                new HashKey(new MonkeyBoolean(true)), 5L,
                new HashKey(new MonkeyBoolean(false)), 6L
        );
       for (var entry : expected.entrySet()) {
           Assertions.assertTrue(hashObj.getObject().containsKey(entry.getKey()), "Map not contain expected key: " + entry.getKey());
           testObject(hashObj.getObject().get(entry.getKey()), entry.getValue());
       }
    }

    private record HashIndexExpressionTestCase(String input, Object expected) {}
    @Test
    public void testHashIndexExpression() throws EvaluationException {
        var tests = List.of(
                new HashIndexExpressionTestCase("{\"foo\": 5}[\"foo\"]", 5L),
                new HashIndexExpressionTestCase("{\"foo\": 5}[\"bar\"]", null),
                new HashIndexExpressionTestCase("let key = \"foo\"; {\"foo\": 5}[key]", 5L),
                new HashIndexExpressionTestCase("{}[\"foo\"]", null),
                new HashIndexExpressionTestCase("{5: 5}[5]", 5L),
                new HashIndexExpressionTestCase("{true: 5}[true]", 5L),
                new HashIndexExpressionTestCase("{false: 5}[false]", 5L)
        );

        for (var test : tests) {
            var evaluated = testEval(test.input);
            testObject(evaluated, test.expected);
        }
    }

    @Test
    public void testHashErrorHandling() {
        var test = "{\"name\": \"Monkey\"}[fn(x) { x }];";
        try {
            var evaluated = testEval(test);
        } catch (EvaluationException e ) {
            Assertions.assertEquals(RuntimeErrorType.INVALID_HASH_KEY, e.getRuntimeError().type());
            Assertions.assertEquals("Index to an hash must be an Expression that yields an Int, String or Boolean", e.getRuntimeError().message());
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
            case STRING -> testStringObject(evaluated, (String) expected);
            case ARRAY_OBJ -> testArrayObject(evaluated, (List<?>) expected);
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

    private void testArrayObject(MonkeyObject<?> object, List<?> expected) {
        var array = Assertions.assertInstanceOf(MonkeyArray.class, object);
        Assertions.assertEquals(expected.size(), array.getObject().size());

        for (int i = 0; i < expected.size(); i++) {
            testObject(array.getObject().get(i), expected.get(i));
        }
    }
}

package com.coolstuff;

import com.coolstuff.ast.*;
import com.coolstuff.ast.Nodes.ExpressionStatement;
import com.coolstuff.ast.Nodes.LetStatement;
import com.coolstuff.ast.Nodes.ReturnStatement;
import com.coolstuff.lexer.Lexer;
import com.coolstuff.parser.Parser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

public class ParserTest {

    private record LetStatementTestCase(String input, String expectedIdentifier, Object expectedValue) {}

    @Test
    public void testLetStatements() {
        var tests = List.of(
                new LetStatementTestCase("let x = 5;", "x", 5),
                new LetStatementTestCase("let y = true;", "y", true),
                new LetStatementTestCase("let foobar = y;", "foobar", "y")
        );

        for (var test : tests) {
            var program = buildProgram(test.input);
            Assertions.assertNotNull(program);
            Assertions.assertEquals(1, program.statements().length);

            var stmt = Assertions.assertInstanceOf(LetStatement.class, program.statements()[0]);

            Assertions.assertTrue(testLetStatement(stmt, test.expectedIdentifier));

            var value = stmt.value();
            testLiteralExpression(value, test.expectedValue);
        }
    }

    private boolean testLetStatement(Statement s, String name) {
        if (!Objects.equals(s.tokenLiteral(), "let")) {
            Assertions.fail("s.tokenLiteral is not 'let'");
        }
        if (!(s instanceof LetStatement)) {
            Assertions.fail("s is not LetStatement");
        }

        LetStatement stmt = (LetStatement) s;
        if (!stmt.name().value().equals(name)) {
            Assertions.fail("statement value is not eq name");
        }
        if (!stmt.name().tokenLiteral().equals(name)) {
            Assertions.fail("statement tokenLiteral is not eq name");
        }

        return true;
    }

    private record ReturnStatementTestCase(String input, Object returnValue) {}

    @Test
    public void testReturnStatements() {
        var tests = List.of(
                new ReturnStatementTestCase("return 5;", 5L),
                new ReturnStatementTestCase("return 10;", 10L),
                new ReturnStatementTestCase("return 993322;", 993322L)
        );
        for (var test : tests) {
            var program = buildProgram(test.input);
            Assertions.assertEquals(1, program.statements().length);

            var stmt = Assertions.assertInstanceOf(ReturnStatement.class, program.statements()[0]);
            Assertions.assertEquals("return", stmt.tokenLiteral());

            var value = stmt.returnValue();
            testLiteralExpression(value, test.returnValue);
        }
    }

    @Test
    public void testIdentifierExpression() {
        var input = "foobar;";

        var program = buildProgram(input);

        Assertions.assertEquals(1, program.statements().length);
        Assertions.assertInstanceOf(ExpressionStatement.class, program.statements()[0]);

        var stmt = (ExpressionStatement) program.statements()[0];
        Assertions.assertInstanceOf(IdentifierExpression.class, stmt.expression());

        var ident = (IdentifierExpression) stmt.expression();
        Assertions.assertEquals("foobar", ident.value());
        Assertions.assertEquals("foobar", ident.tokenLiteral());
    }

    @Test
    public void testIntegerLiteralExpression() {
        var input = "5;";

        var program = buildProgram(input);

        Assertions.assertEquals(1, program.statements().length);
        Assertions.assertInstanceOf(ExpressionStatement.class, program.statements()[0]);

        var stmt = (ExpressionStatement) program.statements()[0];
        Assertions.assertInstanceOf(IntegerLiteralExpression.class, stmt.expression());

        var intLiter = (IntegerLiteralExpression) stmt.expression();
        Assertions.assertEquals(5, intLiter.value());
        Assertions.assertEquals("5", intLiter.tokenLiteral());
    }

    private record BooleanTestRecord(String input, boolean expected){}

    @Test
    public void testBooleanExpression() {
        var input = List.of(
                new BooleanTestRecord("true", true),
                new BooleanTestRecord("false", false)
        );
        for (var testCase : input) {
            var program = buildProgram(testCase.input);

            if (program.statements().length != 1) {
                Assertions.fail("program.statements[] should contain 1 statement");
            }
            Assertions.assertInstanceOf(ExpressionStatement.class, program.statements()[0]);

            var stmt = (ExpressionStatement) program.statements()[0];
            Assertions.assertInstanceOf(BooleanExpression.class, stmt.expression());

            var boolRes = (BooleanExpression) stmt.expression();
            Assertions.assertEquals(testCase.expected, boolRes.value());
        }
    }

    private record PrefixExpressionTestCase(String input, String operator, Object value) {}

    @Test
    public void testParsingPrefixExpressions() {
        var input = List.of(
                new PrefixExpressionTestCase("-5;", "-", 5L),
                new PrefixExpressionTestCase("!15;", "!",15L),
                new PrefixExpressionTestCase("!true;", "!", true),
                new PrefixExpressionTestCase("!false;", "!", false)
        );
        for (var testCase : input) {
            var program = buildProgram(testCase.input);

            if (program.statements().length != 1) {
                Assertions.fail("program.statements[] should contain 1 statement");
            }
            Assertions.assertInstanceOf(ExpressionStatement.class, program.statements()[0]);

            var stmt = (ExpressionStatement) program.statements()[0];
            Assertions.assertInstanceOf(PrefixExpression.class, stmt.expression());

            var prefixExpr = (PrefixExpression) stmt.expression();
            testLiteralExpression(prefixExpr.right(), testCase.value);
            Assertions.assertEquals(testCase.operator, prefixExpr.operator());
        }
    }

    @Test
    public void testParsingStringExpressions() {
        var input = "\"Hello World!\"";
        var program = buildProgram(input);
        if (program.statements().length != 1) {
            Assertions.fail("program.statements[] should contain 1 statement");
        }
        Assertions.assertInstanceOf(ExpressionStatement.class, program.statements()[0]);

        var stmt = (ExpressionStatement) program.statements()[0];
        Assertions.assertInstanceOf(StringLiteralExpression.class, stmt.expression());
        Assertions.assertEquals("Hello World!", program.statements()[0].tokenLiteral());
    }

    private void testIntegerLiteral(Expression exp, Long value) {
        Assertions.assertInstanceOf(IntegerLiteralExpression.class, exp);
        var integ = (IntegerLiteralExpression) exp;

        Assertions.assertEquals(value, integ.value());
        Assertions.assertEquals(value.toString(), integ.tokenLiteral());
    }

    private void testIdentifier(Expression expr, String value) {
        Assertions.assertInstanceOf(IdentifierExpression.class, expr);
        Assertions.assertEquals(value, ((IdentifierExpression)expr).value());
        Assertions.assertEquals(value, expr.tokenLiteral());
    }

    private void testBooleanLiteral(Expression expr, boolean value) {
        Assertions.assertInstanceOf(BooleanExpression.class, expr);
        Assertions.assertEquals(value, ((BooleanExpression)expr).value());
        Assertions.assertEquals(String.valueOf(value), expr.tokenLiteral());
    }

    private void testLiteralExpression(Expression expr, Object expected) {
        switch (expected) {
            case Integer i -> testIntegerLiteral(expr, i.longValue());
            case Long i -> testIntegerLiteral(expr, i);
            case String s -> testIdentifier(expr, s);
            case Boolean b -> testBooleanLiteral(expr, b);
            default ->  throw new AssertionError("Type of exp not handled. got=" + expected.getClass().getSimpleName());
        }
    }

    private record InfixTestRecord(String expression, Object left, String operator, Object right){}

    @Test
    public void testParsingInfixExpressions() {
        var input = List.of(
                new InfixTestRecord("5 + 5;", 5L, "+", 5L),
                new InfixTestRecord("5 - 5;", 5L, "-", 5L),
                new InfixTestRecord("5 * 5;", 5L, "*", 5L),
                new InfixTestRecord("5 / 5;", 5L, "/", 5L),
                new InfixTestRecord("5 > 5;", 5L, ">", 5L),
                new InfixTestRecord("5 < 5;", 5L, "<", 5L),
                new InfixTestRecord("5 == 5;", 5L, "==", 5L),
                new InfixTestRecord("5 != 5;", 5L, "!=", 5L),
                new InfixTestRecord("true == true", true, "==", true),
                new InfixTestRecord("true != false", true, "!=", false),
                new InfixTestRecord("false == false", false, "==", false)
        );
        for (var testCase : input) {
            var program = buildProgram(testCase.expression);

            if (program.statements().length != 1) {
                Assertions.fail("program.statements[] should contain 1 statement");
            }
            var stmt = Assertions.assertInstanceOf(ExpressionStatement.class, program.statements()[0]);

            testInfixExpression(stmt.expression(), testCase.left, testCase.operator, testCase.right);
        }
    }

    @Test
    public void testFunctionLiteralParsing() {
        var input = "fn(x, y) {x+ y;}";
        var program = buildProgram(input);

        Assertions.assertEquals(1, program.statements().length);
        var stmt = Assertions.assertInstanceOf(ExpressionStatement.class, program.statements()[0]);
        var funEx = Assertions.assertInstanceOf(FunctionLiteral.class, stmt.expression());
        Assertions.assertEquals(2, funEx.parameters().length);

        testLiteralExpression(funEx.parameters()[0], "x");
        testLiteralExpression(funEx.parameters()[1], "y");

        Assertions.assertEquals(1, funEx.body().statements().length);

        var bodyStmt = Assertions.assertInstanceOf(ExpressionStatement.class,funEx.body().statements()[0]);

        testInfixExpression(bodyStmt.expression(), "x", "+", "y");
    }

    private record FunctionParameterTestCase(String input, String[] expectedParams){}

    @Test
    public void testFunctionParameterParsing() {
        var testData = List.of(
                new FunctionParameterTestCase("fn() {};", new String[]{}),
                new FunctionParameterTestCase("fn(x) {};", new String[]{"x"}),
                new FunctionParameterTestCase("fn(x, y, z) {};", new String[]{"x", "y", "z"})
        );

        for (var testCase : testData) {
            var program = buildProgram(testCase.input);
            var stmt = (ExpressionStatement) program.statements()[0];
            var funExpr = (FunctionLiteral) stmt.expression();

            Assertions.assertEquals(testCase.expectedParams.length, funExpr.parameters().length);

            for (int i = 0; i < testCase.expectedParams.length; i++) {
                testLiteralExpression(funExpr.parameters()[i], testCase.expectedParams[i]);
            }
        }
    }

    private record OperatorPrecedenceTestCase(String input, String expected){}

    @Test
    public void testOperatorPrecedenceParsing() {
        var testData = List.of(
                new OperatorPrecedenceTestCase("-a * b",
                        "((-a) * b)"),
                new OperatorPrecedenceTestCase("!-a",
                        "(!(-a))"),
                new OperatorPrecedenceTestCase("a + b + c",
                        "((a + b) + c)"),
                new OperatorPrecedenceTestCase("a + b - c",
                        "((a + b) - c)"),
                new OperatorPrecedenceTestCase("a * b * c",
                        "((a * b) * c)"),
                new OperatorPrecedenceTestCase("a * b / c",
                        "((a * b) / c)"),
                new OperatorPrecedenceTestCase("a + b / c",
                        "(a + (b / c))"),
                new OperatorPrecedenceTestCase("a + b * c + d / e - f",
                        "(((a + (b * c)) + (d / e)) - f)"),
                new OperatorPrecedenceTestCase("3 + 4; -5 * 5",
                        "(3 + 4)((-5) * 5)"),
                new OperatorPrecedenceTestCase("5 > 4 == 3 < 4",
                        "((5 > 4) == (3 < 4))"),
                new OperatorPrecedenceTestCase("5 < 4 != 3 > 4",
                        "((5 < 4) != (3 > 4))"),
                new OperatorPrecedenceTestCase("3 + 4 * 5 == 3 * 1 + 4 * 5",
                        "((3 + (4 * 5)) == ((3 * 1) + (4 * 5)))"),
                new OperatorPrecedenceTestCase("true",
                        "true"),
                new OperatorPrecedenceTestCase("false",
                        "false"),
                new OperatorPrecedenceTestCase("3 > 5 == false",
                        "((3 > 5) == false)"),
                new OperatorPrecedenceTestCase("3 < 5 == true",
                        "((3 < 5) == true)"),
                new OperatorPrecedenceTestCase("1 + (2 + 3) + 4",
                        "((1 + (2 + 3)) + 4)"),
                new OperatorPrecedenceTestCase("(5 + 5) * 2",
                        "((5 + 5) * 2)"),
                new OperatorPrecedenceTestCase("2 / (5 + 5)",
                        "(2 / (5 + 5))"),
                new OperatorPrecedenceTestCase("-(5 + 5)",
                        "(-(5 + 5))"),
                new OperatorPrecedenceTestCase("!(true == true)",
                        "(!(true == true))"),
                new OperatorPrecedenceTestCase("a + add(b * c) + d",
                        "((a + add((b * c))) + d)"),
                new OperatorPrecedenceTestCase("add(a, b, 1, 2 * 3, 4 + 5, add(6, 7 * 8))",
                        "add(a, b, 1, (2 * 3), (4 + 5), add(6, (7 * 8)))"),
                new OperatorPrecedenceTestCase("add(a + b + c * d / f + g)",
                        "add((((a + b) + ((c * d) / f)) + g))")
        );

        for (var testCase : testData) {
            var program = buildProgram(testCase.input);

            Assertions.assertEquals(testCase.expected, program.string());
        }
    }

    @Test
    public void testIfExpression() {
        var input = "if (x < y) {x}";
        var program = buildProgram(input);

        Assertions.assertEquals(1, program.statements().length);
        var stmt = Assertions.assertInstanceOf(ExpressionStatement.class, program.statements()[0]);

        var ifExpr = Assertions.assertInstanceOf(IfExpression.class, stmt.expression());

        testInfixExpression(ifExpr.condition(), "x", "<", "y");

        Assertions.assertEquals(1, ifExpr.consequence().statements().length);

        var consequenceExpression = Assertions.assertInstanceOf(ExpressionStatement.class, ifExpr.consequence().statements()[0]);

        testIdentifier(consequenceExpression.expression(), "x");

        Assertions.assertNull(ifExpr.alternative());
    }

    @Test
    public void testIfElseExpression() {
        var input = "if (x < y) {x} else {y}";
        var program = buildProgram(input);

        Assertions.assertEquals(1, program.statements().length);
        var stmt = Assertions.assertInstanceOf(ExpressionStatement.class, program.statements()[0]);

        var ifExpr = Assertions.assertInstanceOf(IfExpression.class, stmt.expression());

        testInfixExpression(ifExpr.condition(), "x", "<", "y");

        Assertions.assertEquals(1, ifExpr.consequence().statements().length);

        var consequence = Assertions.assertInstanceOf(ExpressionStatement.class, ifExpr.consequence().statements()[0]);

        testIdentifier(consequence.expression(), "x");

        Assertions.assertNotNull(ifExpr.alternative());

        var alternative = Assertions.assertInstanceOf(ExpressionStatement.class, ifExpr.alternative().statements()[0]);

        testIdentifier(alternative.expression(), "y");
    }

    @Test
    public void testCallExpression() {
        var input = "add(1, 2 * 3, 4 + 5);";
        var program = buildProgram(input);

        Assertions.assertEquals(1, program.statements().length);
        var stmt = Assertions.assertInstanceOf(ExpressionStatement.class, program.statements()[0]);
        var callExpr = Assertions.assertInstanceOf(CallExpression.class, stmt.expression());

        testIdentifier(callExpr.function(), "add");
        Assertions.assertEquals(3, callExpr.arguments().length);

        testLiteralExpression(callExpr.arguments()[0], 1);
        testInfixExpression(callExpr.arguments()[1], 2L, "*", 3L);
        testInfixExpression(callExpr.arguments()[2], 4L, "+", 5L);
    }

    private record CallParameterTestCase(String input, List<String> expectedArguments) {
    }

    @Test
    public void testCallParameterParsing() {
        var tests = List.of(
                new CallParameterTestCase("func()", List.of()),
                new CallParameterTestCase("func(3 - 1)", List.of("(3 - 1)")),
                new CallParameterTestCase("func(1, 2 * 3 + 1, 98)", List.of("1", "((2 * 3) + 1)", "98"))
        );

        for (var test : tests) {
            var program = buildProgram(test.input);
            Assertions.assertEquals(1, program.statements().length);

            var call = (CallExpression) ((ExpressionStatement) program.statements()[0]).expression();

            Assertions.assertEquals(test.expectedArguments.size(), call.arguments().length);

            for (int i = 0; i < test.expectedArguments.size(); i++) {
                Assertions.assertEquals(test.expectedArguments.get(i), call.arguments()[i].string());
            }
        }
    }

    private Program buildProgram(String input) {
        var parser = new Parser(new Lexer(input));
        var program = parser.parseProgram();
        checkParserErrors(parser);
        return program;
    }

    private void testInfixExpression(Expression expression, Object left, String operator, Object right) {
        if (expression instanceof InfixExpression infixExpression) {
            testLiteralExpression(infixExpression.left(), left);
            Assertions.assertEquals(operator, infixExpression.operator());
            testLiteralExpression(infixExpression.right(), right);
        } else {
            throw new AssertionError(expression.getClass().getSimpleName() + " is not instance of infixExpression");
        }
    }

    private void checkParserErrors(Parser p) {
        var errors = p.getErrors();
        if (errors.isEmpty()) {
            return;
        }
        System.err.printf("Parser had %d errors%n", errors.size());
        for (String error : errors) {
            System.err.printf("Parser error: %s%n", error);
        }
        Assertions.fail();
    }
}

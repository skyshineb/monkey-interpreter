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
import java.util.Map;
import java.util.Objects;

public class ParserTest {

    @Test
    public void testLetStatements() {
        final var input = """
                let x = 5;
                let y = 10;
                let foobar  = 838383;
                
                """;

        Lexer l = new Lexer(input);
        Parser p = new Parser(l);

        Program program = p.parseProgram();
        checkParserErrors(p);
        if (program == null) {
            Assertions.fail("parseProgram returned null");
        }
        if (program.statements().length != 3) {
            Assertions.fail("program.statements[] should contain 3 statements");
        }

        String[] expectedIdentifiers = {
                "x",
                "y",
                "foobar"
        };

        Assertions.assertDoesNotThrow(() -> {
            for (int i = 0; i < expectedIdentifiers.length; i++) {
                Assertions.assertTrue(testLetStatement(program.statements()[i], expectedIdentifiers[i]));
            }
        });
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

    @Test
    public void testReturnStatements() {
        final var input = """
                return 5;
                return 10;
                return 993322;
                """;

        Lexer l = new Lexer(input);
        Parser p = new Parser(l);

        Program program = p.parseProgram();
        checkParserErrors(p);

        if (program.statements().length != 3) {
            Assertions.fail("program.statements[] should contain 3 statements");
        }

        for (Statement stmt : program.statements()) {
            Assertions.assertInstanceOf(ReturnStatement.class, stmt);
            Assertions.assertEquals("return", stmt.tokenLiteral());
        }
    }

    @Test
    public void testIdentifierExpression() {
        var input = "foobar;";

        var l = new Lexer(input);
        var p = new Parser(l);
        var program = p.parseProgram();
        checkParserErrors(p);

        if (program.statements().length != 1) {
            Assertions.fail("program.statements[] should contain 1 statement");
        }
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

        var l = new Lexer(input);
        var p = new Parser(l);
        var program = p.parseProgram();
        checkParserErrors(p);

        if (program.statements().length != 1) {
            Assertions.fail("program.statements[] should contain 1 statement");
        }
        Assertions.assertInstanceOf(ExpressionStatement.class, program.statements()[0]);

        var stmt = (ExpressionStatement) program.statements()[0];
        Assertions.assertInstanceOf(IntegerLiteralExpression.class, stmt.expression());

        var intLiter = (IntegerLiteralExpression) stmt.expression();
        Assertions.assertEquals(5, intLiter.value());
        Assertions.assertEquals("5", intLiter.tokenLiteral());
    }

    @Test
    public void testParsingPrefixExpressions() {
        var input = Map.of("-5;", 5L, "!15;", 15L);
        for (var in : input.entrySet()) {
            var l = new Lexer(in.getKey());
            var p = new Parser(l);
            var program = p.parseProgram();
            checkParserErrors(p);

            if (program.statements().length != 1) {
                Assertions.fail("program.statements[] should contain 1 statement");
            }
            Assertions.assertInstanceOf(ExpressionStatement.class, program.statements()[0]);

            var stmt = (ExpressionStatement) program.statements()[0];
            Assertions.assertInstanceOf(PrefixExpression.class, stmt.expression());

            var prefixExpr = (PrefixExpression) stmt.expression();
            testLiteralExpression(prefixExpr.right(), in.getValue());
        }
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

    private void testLiteralExpression(Expression expr, Object expected) {
        switch (expected) {
            case Integer i -> testIntegerLiteral(expr, i.longValue());
            case Long i -> testIntegerLiteral(expr, i);
            case String s -> testIdentifier(expr, s);
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
                new InfixTestRecord("5 != 5;", 5L, "!=", 5L)
        );
        for (var testCase : input) {
            var l = new Lexer(testCase.expression);
            var p = new Parser(l);
            var program = p.parseProgram();
            checkParserErrors(p);

            if (program.statements().length != 1) {
                Assertions.fail("program.statements[] should contain 1 statement");
            }
            Assertions.assertInstanceOf(ExpressionStatement.class, program.statements()[0]);

            var stmt = (ExpressionStatement) program.statements()[0];
            Assertions.assertInstanceOf(InfixExpression.class, stmt.expression());

            var infixExpr = (InfixExpression) stmt.expression();

            testLiteralExpression(infixExpr.left(),testCase.left);
            testLiteralExpression(infixExpr.right(),testCase.right);

            Assertions.assertEquals(testCase.operator, infixExpr.tokenLiteral());
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
                        "((3 + (4 * 5)) == ((3 * 1) + (4 * 5)))")
        );

        for (var testCase : testData) {
            var l = new Lexer(testCase.input);
            var p = new Parser(l);
            var program = p.parseProgram();
            checkParserErrors(p);

            Assertions.assertEquals(testCase.expected, program.string());
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

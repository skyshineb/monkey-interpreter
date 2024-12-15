package com.coolstuff.evaluator;

import com.coolstuff.ast.IntegerLiteralExpression;
import com.coolstuff.ast.Node;
import com.coolstuff.ast.Nodes.ExpressionStatement;
import com.coolstuff.ast.Program;
import com.coolstuff.ast.Statement;
import com.coolstuff.evaluator.object.MonkeyInteger;
import com.coolstuff.evaluator.object.MonkeyNull;
import com.coolstuff.evaluator.object.MonkeyObject;

public class Evaluator {

    public MonkeyObject<?> eval(Node node) throws EvaluationException {
        return switch (node) {
            case Program astProgram -> evalStatements(astProgram.statements());
            case ExpressionStatement expressionStatement -> eval(expressionStatement.expression());
            case IntegerLiteralExpression integerLiteral -> new MonkeyInteger(integerLiteral.value());
            default -> throw new EvaluationException("Unexpected value: " + node);
        };
    }

    public MonkeyObject<?> evalStatements(Statement[] statements) throws EvaluationException {
        MonkeyObject<?> result = MonkeyNull.INSTANCE;
        var i = 0;
        for (var stmt : statements) {
            result = eval(stmt);
        }

        return result;
    }
}

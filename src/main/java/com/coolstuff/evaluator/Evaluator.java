package com.coolstuff.evaluator;

import com.coolstuff.ast.*;
import com.coolstuff.ast.Nodes.ExpressionStatement;
import com.coolstuff.evaluator.object.MonkeyBoolean;
import com.coolstuff.evaluator.object.MonkeyInteger;
import com.coolstuff.evaluator.object.MonkeyNull;
import com.coolstuff.evaluator.object.MonkeyObject;

public class Evaluator {

    public MonkeyObject<?> eval(Node node) throws EvaluationException {
        return switch (node) {
            case Program astProgram -> evalStatements(astProgram.statements());
            case ExpressionStatement expressionStatement -> eval(expressionStatement.expression());
            case IntegerLiteralExpression integerLiteral -> new MonkeyInteger(integerLiteral.value());
            case BooleanExpression booleanLiteral -> MonkeyBoolean.nativeToMonkey(booleanLiteral.value());
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

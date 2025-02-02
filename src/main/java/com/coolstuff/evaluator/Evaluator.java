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
            case PrefixExpression prefixExpression -> evalPrefixExpression(prefixExpression);
            default -> throw new EvaluationException("Unexpected value: " + node);
        };
    }

    private MonkeyObject<?> evalPrefixExpression(PrefixExpression prefixExpression) throws EvaluationException {
        var expressionResult = eval(prefixExpression.right());

        return switch (prefixExpression.token().type()) {
            case BANG -> MonkeyBoolean.nativeToMonkey(!isTruth(expressionResult));
            case MINUS -> {
                if (expressionResult instanceof MonkeyInteger integer) {
                    yield new MonkeyInteger(-integer.getObject());
                }

                if (expressionResult instanceof MonkeyNull nullInstance) {
                    yield nullInstance;
                }

                throw new EvaluationException(String.format("Operation - not supported for type %s", expressionResult.getType().name()));

            }
            default -> MonkeyNull.INSTANCE;
        };
    }

    private static boolean isTruth(MonkeyObject<?> object) {
        return switch (object) {
            case MonkeyBoolean bool -> bool.getObject();
            case MonkeyNull ignored -> false;
            default -> true;
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

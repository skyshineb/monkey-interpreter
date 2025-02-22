package com.coolstuff.evaluator;

import com.coolstuff.ast.*;
import com.coolstuff.ast.Nodes.BlockStatement;
import com.coolstuff.ast.Nodes.ExpressionStatement;
import com.coolstuff.evaluator.object.*;

public class Evaluator {

    public MonkeyObject<?> eval(Node node) throws EvaluationException {
        return switch (node) {
            case Program astProgram -> evalStatements(astProgram.statements());
            case ExpressionStatement expressionStatement -> eval(expressionStatement.expression());
            case IntegerLiteralExpression integerLiteral -> new MonkeyInteger(integerLiteral.value());
            case BooleanExpression booleanLiteral -> MonkeyBoolean.nativeToMonkey(booleanLiteral.value());
            case PrefixExpression prefixExpression -> evalPrefixExpression(prefixExpression);
            case InfixExpression infixExpression -> evalInfixExpression(infixExpression);
            case BlockStatement blockStatement -> evalStatements(blockStatement.statements());
            case IfExpression ifExpression -> evalIfExpression(ifExpression);
            default -> throw new EvaluationException("Unexpected value: " + node);
        };
    }

    private MonkeyObject<?> evalIfExpression(IfExpression ifExpression) throws EvaluationException {
        var condition = eval(ifExpression.condition());

        if (isTruth(condition)) {
            return eval(ifExpression.consequence());
        } else if (ifExpression.alternative() != null) {
            return eval(ifExpression.alternative());
        } else {
            return MonkeyNull.INSTANCE;
        }
    }

    private MonkeyObject<?> evalInfixExpression(InfixExpression infixExpression) throws EvaluationException {
        var left = eval(infixExpression.left());
        var right = eval(infixExpression.right());

        if (left.getType() == ObjectType.INTEGER && right.getType() == ObjectType.INTEGER) {
            return evalIntegerInfixExpression((MonkeyInteger) left, (MonkeyInteger) right, infixExpression);
        }

        switch (infixExpression.token().type()) {
            case EQ -> {
                return MonkeyBoolean.nativeToMonkey(((MonkeyBoolean) left).getObject() == ((MonkeyBoolean) right).getObject());
            }
            case NOT_EQ -> {
                return MonkeyBoolean.nativeToMonkey(((MonkeyBoolean) left).getObject() != ((MonkeyBoolean) right).getObject());
            }
        }

        return MonkeyNull.INSTANCE;
    }

    private MonkeyObject<?> evalIntegerInfixExpression(MonkeyInteger left, MonkeyInteger right, InfixExpression infixExpression) throws EvaluationException {
        return switch (infixExpression.token().type()) {
            case PLUS -> new MonkeyInteger(left.getObject() + right.getObject());
            case MINUS -> new MonkeyInteger(left.getObject() - right.getObject());
            case ASTERISK -> new MonkeyInteger(left.getObject() * right.getObject());
            case SLASH -> {
                if (right.getObject().equals(0L)) {
                    throw new EvaluationException("Cannot divide by 0!");
                }
                yield new MonkeyInteger(left.getObject() / right.getObject());
            }
            case LT -> MonkeyBoolean.nativeToMonkey(left.getObject() < right.getObject());
            case GT -> MonkeyBoolean.nativeToMonkey(left.getObject() > right.getObject());
            case EQ -> MonkeyBoolean.nativeToMonkey(left.getObject().equals(right.getObject()));
            case NOT_EQ -> MonkeyBoolean.nativeToMonkey(!left.getObject().equals(right.getObject()));
            default -> MonkeyNull.INSTANCE;
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

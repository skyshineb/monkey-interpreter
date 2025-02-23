package com.coolstuff.evaluator;

import com.coolstuff.ast.*;
import com.coolstuff.ast.Nodes.BlockStatement;
import com.coolstuff.ast.Nodes.ExpressionStatement;
import com.coolstuff.ast.Nodes.LetStatement;
import com.coolstuff.ast.Nodes.ReturnStatement;
import com.coolstuff.evaluator.object.*;

import java.util.Optional;

public class Evaluator {

    private final Environment environment;

    public Evaluator() {
        this.environment = new Environment();
    }

    public MonkeyObject<?> eval(Node node) throws EvaluationException {
        return switch (node) {
            case Program astProgram -> evalStatements(astProgram.statements(), true);
            case ExpressionStatement expressionStatement -> eval(expressionStatement.expression());
            case IntegerLiteralExpression integerLiteral -> new MonkeyInteger(integerLiteral.value());
            case BooleanExpression booleanLiteral -> MonkeyBoolean.nativeToMonkey(booleanLiteral.value());
            case PrefixExpression prefixExpression -> evalPrefixExpression(prefixExpression);
            case InfixExpression infixExpression -> evalInfixExpression(infixExpression);
            case BlockStatement blockStatement -> evalStatements(blockStatement.statements(), false);
            case IfExpression ifExpression -> evalIfExpression(ifExpression);
            case ReturnStatement returnStatement -> evalReturnStatement(returnStatement);
            case LetStatement letStatement -> evalLetStatement(letStatement);
            case IdentifierExpression identifierExpression -> evalIdentifierExpression(identifierExpression);
            default -> throw new EvaluationException("Unexpected value: " + node);
        };
    }

    private MonkeyObject<?> evalIdentifierExpression(IdentifierExpression identifierExpression) throws EvaluationException {
        Optional<MonkeyObject<?>> resolvedValue = environment.get(identifierExpression.value());

        if (resolvedValue.isEmpty()) {
            throw new EvaluationException("Identifier not found: %s", identifierExpression.value());
        }

        return resolvedValue.get();
    }

    private MonkeyObject<?> evalLetStatement(LetStatement letStatement) throws EvaluationException {
        var resolvedValue = eval(letStatement.value());
        return environment.set(letStatement.name().value(), resolvedValue);
    }

    private MonkeyObject<?> evalReturnStatement(ReturnStatement returnStatement) throws EvaluationException {
        var returnValue = eval(returnStatement.returnValue());

        return new MonkeyReturn<>(returnValue);
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

        throw new EvaluationException(
                "Operation %s not supported for types %s and %s",
                infixExpression.token().token(),
                left.getType(),
                right.getType()
        );
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
            // should be unreachable
            default -> throw new IllegalStateException("Evaluation BUG: Unexpected value(unreachable code): " + infixExpression.token().token());
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
            // should be unreachable
            default -> throw new IllegalStateException("Evaluation BUG: Unexpected value(unreachable code): " + prefixExpression.token().token());
        };
    }

    private static boolean isTruth(MonkeyObject<?> object) {
        return switch (object) {
            case MonkeyBoolean bool -> bool.getObject();
            case MonkeyNull ignored -> false;
            default -> true;
        };
    }

    public MonkeyObject<?> evalStatements(Statement[] statements, boolean unwrapReturn) throws EvaluationException {
        MonkeyObject<?> result = MonkeyNull.INSTANCE;

        for (var stmt : statements) {
            result = eval(stmt);

            if (result instanceof MonkeyReturn<?> monkeyReturn) {
                if (unwrapReturn) {
                    return monkeyReturn.returnValue;
                }

                return monkeyReturn;
            }
        }

        return result;
    }
}

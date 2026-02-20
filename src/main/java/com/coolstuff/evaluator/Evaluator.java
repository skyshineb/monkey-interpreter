package com.coolstuff.evaluator;

import com.coolstuff.ast.*;
import com.coolstuff.ast.Nodes.BlockStatement;
import com.coolstuff.ast.Nodes.BreakStatement;
import com.coolstuff.ast.Nodes.ContinueStatement;
import com.coolstuff.ast.Nodes.ExpressionStatement;
import com.coolstuff.ast.Nodes.LetStatement;
import com.coolstuff.ast.Nodes.ReturnStatement;
import com.coolstuff.ast.Nodes.WhileStatement;
import com.coolstuff.evaluator.object.*;

import java.util.*;


public class Evaluator {

    private final Environment environment;
    private int loopDepth = 0;

    public Evaluator() {
        this.environment = new Environment();
    }

    public Evaluator(Environment environment) {
        this.environment = environment;
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
            case FunctionLiteral functionLiteral -> evalFunction(functionLiteral);
            case CallExpression callExpression -> evalCallExpression(callExpression);
            case StringLiteralExpression stringLiteral -> new MonkeyString(stringLiteral.value());
            case ArrayLiteral arrayLiteral -> new MonkeyArray(List.of(evalExpressions(arrayLiteral.elements())));
            case IndexExpression indexExpression -> evalIndexExpression(indexExpression);
            case HashLiteral hashLiteral -> evalHashLiteral(hashLiteral);
            case WhileStatement whileStatement -> evalWhileStatement(whileStatement);
            case BreakStatement ignored -> evalBreakStatement();
            case ContinueStatement ignored -> evalContinueStatement();
            default -> throw new EvaluationException("Unexpected value: " + node);
        };
    }

    private MonkeyObject<?> evalHashLiteral(HashLiteral literal) throws EvaluationException {
        Map<HashKey, MonkeyObject<?>> map = new HashMap<>();
        for (var pair : literal.pairs()) {
            var key = eval(pair.key());
            var value = eval(pair.value());

            map.put(new HashKey(key), value);
        }
        return new MonkeyHash(map);
    }

    private MonkeyObject<?> evalIndexExpression(IndexExpression node) throws EvaluationException {
        var left = eval(node.left());
        return switch (left) {
            case MonkeyArray array -> {
                var index = MonkeyArray.verifyIndexIsInteger(eval(node.index()));
                if (index.getObject() < 0 || index.getObject() >= array.getObject().size()) {
                    yield MonkeyNull.INSTANCE;
                }
                yield array.getObject().get(index.getObject().intValue());

            }
            case MonkeyHash hash -> {
                var key = eval(node.index());
                MonkeyHashable.checkIsHashable(key);
                var res = hash.getObject().get(key);
                if (res == null) {
                    yield MonkeyNull.INSTANCE;
                }
                yield res;
            }
            default -> throw new EvaluationException("Index operator not supported for %s", left.getType());
        };
    }

    private MonkeyObject<?> evalCallExpression(CallExpression node) throws EvaluationException {
        var function = eval(node.function());

        if (function instanceof AbstractMonkeyFunction functionToCall) {
            var args = evalExpressions(node.arguments());
            return functionToCall.getObject().apply(node.token(), Arrays.stream(args).toList());
        } else {
            throw new EvaluationException("Not a function: %s", function.inspect());
        }
    }

    private MonkeyObject<?>[] evalExpressions(Expression[] expressions) throws EvaluationException {
        try {
            return Arrays.stream(expressions).map(expr -> {
                try {
                    return eval(expr);
                } catch (EvaluationException e) {
                    throw new RuntimeException(e);
                }
            }).toList().toArray(MonkeyObject[]::new);
        } catch (RuntimeException e) {
            if (e.getCause() instanceof EvaluationException) {
                throw (EvaluationException) e.getCause();
            }
            throw e;
        }
    }

    private MonkeyObject<?> evalFunction(FunctionLiteral functionLiteral) {
        var params = functionLiteral.parameters();
        var body = functionLiteral.body();
        return new MonkeyFunction(environment, functionLiteral);
    }

    private MonkeyObject<?> evalIdentifierExpression(IdentifierExpression identifierExpression) throws EvaluationException {
        Optional<MonkeyObject<?>> resolvedValue = environment.get(identifierExpression.value());

        if (resolvedValue.isEmpty()) {
            return BuiltInFunctions.getFunction(identifierExpression.value()).orElseThrow(() ->
                    new EvaluationException("Identifier not found: %s", identifierExpression.value()));
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

    private MonkeyObject<?> evalWhileStatement(WhileStatement whileStatement) throws EvaluationException {
        MonkeyObject<?> result = MonkeyNull.INSTANCE;
        loopDepth++;

        try {
            while (isTruth(eval(whileStatement.condition()))) {
                result = eval(whileStatement.body());

                if (result instanceof MonkeyBreak) {
                    return MonkeyNull.INSTANCE;
                }

                if (result instanceof MonkeyContinue) {
                    result = MonkeyNull.INSTANCE;
                    continue;
                }

                if (result instanceof MonkeyReturn<?>) {
                    return result;
                }
            }
        } finally {
            loopDepth--;
        }

        return result;
    }

    private MonkeyObject<?> evalBreakStatement() throws EvaluationException {
        if (loopDepth == 0) {
            throw new EvaluationException("`break` not allowed outside loop");
        }

        return MonkeyBreak.INSTANCE;
    }

    private MonkeyObject<?> evalContinueStatement() throws EvaluationException {
        if (loopDepth == 0) {
            throw new EvaluationException("`continue` not allowed outside loop");
        }

        return MonkeyContinue.INSTANCE;
    }

    private MonkeyObject<?> evalInfixExpression(InfixExpression infixExpression) throws EvaluationException {
        var left = eval(infixExpression.left());

        switch (infixExpression.token().type()) {
            case AND -> {
                if (!isTruth(left)) {
                    return MonkeyBoolean.FALSE;
                }

                var right = eval(infixExpression.right());
                return MonkeyBoolean.nativeToMonkey(isTruth(right));
            }
            case OR -> {
                if (isTruth(left)) {
                    return MonkeyBoolean.TRUE;
                }

                var right = eval(infixExpression.right());
                return MonkeyBoolean.nativeToMonkey(isTruth(right));
            }
        }

        var right = eval(infixExpression.right());

        if (left.getType() == ObjectType.INTEGER && right.getType() == ObjectType.INTEGER) {
            return evalIntegerInfixExpression((MonkeyInteger) left, (MonkeyInteger) right, infixExpression);
        } else if (left.getType() == ObjectType.STRING && right.getType() == ObjectType.STRING) {
            return evalStringInfixExpression((MonkeyString) left, (MonkeyString) right, infixExpression);
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
            case LTE -> MonkeyBoolean.nativeToMonkey(left.getObject() <= right.getObject());
            case GTE -> MonkeyBoolean.nativeToMonkey(left.getObject() >= right.getObject());
            case EQ -> MonkeyBoolean.nativeToMonkey(left.getObject().equals(right.getObject()));
            case NOT_EQ -> MonkeyBoolean.nativeToMonkey(!left.getObject().equals(right.getObject()));
            // should be unreachable
            default -> throw new IllegalStateException("Evaluation BUG: Unexpected value(unreachable code): " + infixExpression.token().token());
        };
    }

    private MonkeyObject<?> evalStringInfixExpression(MonkeyString left, MonkeyString right, InfixExpression infixExpression) throws EvaluationException {
        return switch (infixExpression.token().type()) {
            case PLUS -> new MonkeyString(left.getObject() + right.getObject());
            default -> throw new EvaluationException(
                    "Operation %s not supported for types %s and %s",
                    infixExpression.token().token(),
                    left.getType(),
                    right.getType()
            );
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

            if (result instanceof MonkeyBreak || result instanceof MonkeyContinue) {
                return result;
            }
        }

        return result;
    }
}

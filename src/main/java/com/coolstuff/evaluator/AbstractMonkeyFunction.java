package com.coolstuff.evaluator;

import com.coolstuff.evaluator.object.MonkeyObject;
import com.coolstuff.evaluator.object.ObjectType;
import com.coolstuff.token.Token;

public abstract class AbstractMonkeyFunction extends MonkeyObject<MonkeyFunctionInterface> {

    public AbstractMonkeyFunction(ObjectType type) {
        super(type);
    }

    public static void checkArgumentCount(int expected, int actual, Token callToken, Evaluator evaluator) throws EvaluationException {
        if (actual != expected) {
            throw evaluator.error(RuntimeErrorType.INVALID_ARGUMENT, callToken, "Wrong number of arguments. Expected %d, got %d", expected, actual);
        }
    }

    public static void checkArgumentType(MonkeyObject<?> argument, ObjectType expected, String functionName, Token callToken, Evaluator evaluator) throws EvaluationException {
        if (argument.getType() != expected) {
            throw evaluator.error(RuntimeErrorType.TYPE_MISMATCH, callToken, "Argument to `%s` must be %s, got %s", functionName, expected, argument.getType());
        }
    }
}

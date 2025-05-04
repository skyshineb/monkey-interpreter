package com.coolstuff.evaluator;

import com.coolstuff.evaluator.object.MonkeyObject;
import com.coolstuff.evaluator.object.ObjectType;

public abstract class AbstractMonkeyFunction extends MonkeyObject<MonkeyFunctionInterface> {

    public AbstractMonkeyFunction(ObjectType type) {
        super(type);
    }

    public static void checkArgumentCount(int expected, int actual) throws EvaluationException {
        if (actual != expected) {
            throw new EvaluationException("Wrong number of arguments. Expected %d, got %d", expected, actual);
        }
    }

    public static void checkArgumentType(MonkeyObject<?> argument, ObjectType expected, String functionName) throws EvaluationException {
        if (argument.getType() != expected) {
            throw new EvaluationException("Argument to `%s` must be %s, got %s", functionName, expected, argument.getType());
        }
    }
}

package com.coolstuff.evaluator;

import com.coolstuff.evaluator.object.MonkeyObject;

public class EarlyReturnException extends RuntimeException {

    public final MonkeyObject<?> returnValue;

    public EarlyReturnException(MonkeyObject<?> returnValue) {
        this.returnValue = returnValue;
    }
}

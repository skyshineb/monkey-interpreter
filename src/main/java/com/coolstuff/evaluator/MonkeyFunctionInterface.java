package com.coolstuff.evaluator;

import com.coolstuff.evaluator.object.MonkeyObject;
import com.coolstuff.token.Token;

import java.util.List;

@FunctionalInterface
public interface MonkeyFunctionInterface {
    MonkeyObject<?> apply(Token callToken, List<MonkeyObject<?>> arguments, Evaluator evaluator) throws EvaluationException;
}

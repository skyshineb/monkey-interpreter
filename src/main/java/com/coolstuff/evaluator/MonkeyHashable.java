package com.coolstuff.evaluator;

import com.coolstuff.evaluator.object.MonkeyObject;
import com.coolstuff.token.Token;

public interface MonkeyHashable {

    HashKey hashKey();

    static MonkeyHashable checkIsHashable(MonkeyObject<?> object) throws EvaluationException {
        if (object instanceof MonkeyHashable hashable) {
            return hashable;
        }

        throw new EvaluationException("Index to an hash must be an Expression that yields an Int, String or Boolean");
    }
}

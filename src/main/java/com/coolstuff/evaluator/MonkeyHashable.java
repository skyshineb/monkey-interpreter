package com.coolstuff.evaluator;

import com.coolstuff.evaluator.object.MonkeyObject;
import com.coolstuff.token.Token;

public interface MonkeyHashable {

    HashKey hashKey();

    static MonkeyHashable checkIsHashable(MonkeyObject<?> object, Token token, Evaluator evaluator) throws EvaluationException {
        if (object instanceof MonkeyHashable hashable) {
            return hashable;
        }

        throw evaluator.error(RuntimeErrorType.INVALID_HASH_KEY, token, "Index to an hash must be an Expression that yields an Int, String or Boolean");
    }
}

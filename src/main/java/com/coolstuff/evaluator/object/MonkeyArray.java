package com.coolstuff.evaluator.object;

import com.coolstuff.evaluator.EvaluationException;
import com.coolstuff.token.Token;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MonkeyArray extends MonkeyObject<List<MonkeyObject<?>>> {

    public MonkeyArray(List<MonkeyObject<?>> elements) {
        super(ObjectType.ARRAY_OBJ);
        setObject(Collections.unmodifiableList(elements));
    }

    @Override
    public String inspect() {
        return "[%s]".formatted(getObject().stream().map(MonkeyObject::inspect).collect(Collectors.joining(", ")));
    }

    public static MonkeyInteger verifyIndexIsInteger(MonkeyObject<?> index) throws EvaluationException {
        if (index instanceof MonkeyInteger integer) {
            return integer;
        }

        throw new EvaluationException("Index to an array must be an Expression that yields an Int");
    }
}

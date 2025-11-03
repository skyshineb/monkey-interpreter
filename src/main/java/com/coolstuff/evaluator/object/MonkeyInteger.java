package com.coolstuff.evaluator.object;

import com.coolstuff.evaluator.HashKey;
import com.coolstuff.evaluator.MonkeyHashable;

public class MonkeyInteger extends MonkeyObject<Long> implements MonkeyHashable {

    public MonkeyInteger(long value) {
        super(ObjectType.INTEGER);
        setObject(value);
    }

    @Override
    public String inspect() {
        return String.valueOf(getObject());
    }

    @Override
    public HashKey hashKey() {
        return new HashKey(this);
    }
}

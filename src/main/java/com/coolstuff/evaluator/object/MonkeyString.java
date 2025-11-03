package com.coolstuff.evaluator.object;

import com.coolstuff.evaluator.HashKey;
import com.coolstuff.evaluator.MonkeyHashable;

public class MonkeyString extends MonkeyObject<String> implements MonkeyHashable {

    public MonkeyString(String value) {
        super(ObjectType.STRING);
        setObject(value);
    }

    @Override
    public String inspect() {
        return getObject();
    }

    @Override
    public HashKey hashKey() {
        return new HashKey(this);
    }
}

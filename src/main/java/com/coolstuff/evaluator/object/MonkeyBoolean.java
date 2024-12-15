package com.coolstuff.evaluator.object;

public class MonkeyBoolean extends MonkeyObject<Boolean> {

    public MonkeyBoolean(boolean value) {
        super(ObjectType.BOOLEAN);
        setObject(value);
    }

    @Override
    public String inspect() {
        return String.valueOf(getObject());
    }
}

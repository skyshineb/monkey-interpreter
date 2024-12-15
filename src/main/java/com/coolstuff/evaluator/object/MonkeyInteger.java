package com.coolstuff.evaluator.object;

public class MonkeyInteger extends MonkeyObject<Long> {

    public MonkeyInteger(long value) {
        super(ObjectType.INTEGER);
        setObject(value);
    }

    @Override
    public String inspect() {
        return String.valueOf(getObject());
    }
}

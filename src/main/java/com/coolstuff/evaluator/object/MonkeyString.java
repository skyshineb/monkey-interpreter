package com.coolstuff.evaluator.object;

public class MonkeyString extends MonkeyObject<String> {

    public MonkeyString(String value) {
        super(ObjectType.STRING);
        setObject(value);
    }

    @Override
    public String inspect() {
        return getObject();
    }
}

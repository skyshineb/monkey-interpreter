package com.coolstuff.evaluator.object;

import javax.lang.model.type.NullType;

public class MonkeyNull extends MonkeyObject<NullType> {

    public static final MonkeyNull INSTANCE = new MonkeyNull();

    public MonkeyNull() {
        super(ObjectType.NULL);
        setObject(null);
    }

    @Override
    public String inspect() {
        return "null";
    }

}

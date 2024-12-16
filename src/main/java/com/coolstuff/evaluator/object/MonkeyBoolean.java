package com.coolstuff.evaluator.object;

public class MonkeyBoolean extends MonkeyObject<Boolean> {

    public final static MonkeyBoolean TRUE = new MonkeyBoolean(true);
    public final static MonkeyBoolean FALSE = new MonkeyBoolean(false);

    public MonkeyBoolean(boolean value) {
        super(ObjectType.BOOLEAN);
        setObject(value);
    }

    public static MonkeyBoolean nativeToMonkey(boolean bool) {
        return bool ? TRUE : FALSE;
    }

    @Override
    public String inspect() {
        return String.valueOf(getObject());
    }
}

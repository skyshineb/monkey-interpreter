package com.coolstuff.evaluator.object;

public class MonkeyReturn<T> extends MonkeyObject<T> {

    public final MonkeyObject<T> returnValue;

    public MonkeyReturn(MonkeyObject<T> returnValue) {
        super(ObjectType.RETURN_VALUE_OBJ);
        this.returnValue = returnValue;
    }

    @Override
    public String inspect() {
        return "MonkeyReturn(%s)".formatted(returnValue.inspect());
    }
}

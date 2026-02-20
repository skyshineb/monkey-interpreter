package com.coolstuff.evaluator.object;

public class MonkeyBreak extends MonkeyObject<Void> {

    public static final MonkeyBreak INSTANCE = new MonkeyBreak();

    private MonkeyBreak() {
        super(ObjectType.BREAK_OBJ);
    }

    @Override
    public String inspect() {
        return "break";
    }
}

package com.coolstuff.evaluator.object;

public class MonkeyContinue extends MonkeyObject<Void> {

    public static final MonkeyContinue INSTANCE = new MonkeyContinue();

    private MonkeyContinue() {
        super(ObjectType.CONTINUE_OBJ);
    }

    @Override
    public String inspect() {
        return "continue";
    }
}

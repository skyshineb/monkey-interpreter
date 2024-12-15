package com.coolstuff.evaluator.object;

public abstract class MonkeyObject<T> {
    private final ObjectType type;
    private T object;

    public MonkeyObject(ObjectType type) {
        this.type = type;
    }

    public abstract String inspect();

    public void setObject(T object) {
        this.object = object;
    }

    public T getObject() {
        return this.object;
    }

    public ObjectType getType() {
        return this.type;
    }
}

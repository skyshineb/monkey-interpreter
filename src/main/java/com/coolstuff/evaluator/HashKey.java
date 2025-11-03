package com.coolstuff.evaluator;

import com.coolstuff.evaluator.object.MonkeyObject;
import com.coolstuff.evaluator.object.ObjectType;

import java.util.Objects;

public class HashKey {
    private final ObjectType type;
    private final long value;
    private final MonkeyObject originalObject;

    public HashKey(MonkeyObject<?> object) {
        this.type = object.getType();
        this.value = object.getObject().hashCode();
        this.originalObject = object;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HashKey hashKey = (HashKey) o;
        return value == hashKey.value && type == hashKey.type;
    }

    public MonkeyObject getOriginalObject() {
        return originalObject;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }
}

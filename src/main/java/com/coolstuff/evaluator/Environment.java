package com.coolstuff.evaluator;

import com.coolstuff.evaluator.object.MonkeyObject;

import java.util.HashMap;
import java.util.Optional;

public class Environment {
    private final HashMap<String, MonkeyObject<?>> bindings = new HashMap<>();

    public Environment() {}

    public <T> MonkeyObject<T> set(String name, MonkeyObject<T> value) {
        bindings.put(name, value);
        return value;
    }

    public Optional<MonkeyObject<?>> get(String name) {
        var value = bindings.get(name);

        if (value == null) {
            return Optional.empty();
        }

        return Optional.of(value);
    }
}

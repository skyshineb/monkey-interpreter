package com.coolstuff.evaluator;

import com.coolstuff.evaluator.object.MonkeyObject;

import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Environment {
    private final HashMap<String, MonkeyObject<?>> bindings = new HashMap<>();
    private final Environment upper;

    public Environment() {
        this.upper = null;
    }

    public Environment(Environment upper) {
        this.upper = upper;
    }

    public <T> MonkeyObject<T> set(String name, MonkeyObject<T> value) {
        bindings.put(name, value);
        return value;
    }

    public Optional<MonkeyObject<?>> get(String name) {
        var value = bindings.get(name);

        if (value == null) {
            if (upper == null) {
                return Optional.empty();
            }
            return upper.get(name);
        }

        return Optional.of(value);
    }

    public Map<String, MonkeyObject<?>> snapshotCurrentScope() {
        return Map.copyOf(bindings);
    }

    public Map<String, MonkeyObject<?>> snapshotMergedScopes() {
        var merged = new LinkedHashMap<String, MonkeyObject<?>>();
        if (upper != null) {
            merged.putAll(upper.snapshotMergedScopes());
        }
        merged.putAll(bindings);
        return Map.copyOf(merged);
    }
}

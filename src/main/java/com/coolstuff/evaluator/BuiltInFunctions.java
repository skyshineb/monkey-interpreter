package com.coolstuff.evaluator;

import com.coolstuff.evaluator.object.BuiltInFunction;
import com.coolstuff.evaluator.object.MonkeyInteger;
import com.coolstuff.evaluator.object.MonkeyObject;
import com.coolstuff.evaluator.object.MonkeyString;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum BuiltInFunctions {
    LEN("len", (callToken, arguments) -> {
        AbstractMonkeyFunction.checkArgumentCount(1, arguments.size());
        MonkeyObject<?> argument = arguments.get(0);

        // This function will also accept Objects
        return switch (argument) {
            case MonkeyString string -> new MonkeyInteger(string.getObject().length());
            default ->
                    throw new EvaluationException("Argument to `len` not supported, got %s", argument.getType());
        };
    });

    private final String identifier;
    private final BuiltInFunction builtInFunction;

    private static Map<String, BuiltInFunction> functionsMap;

    BuiltInFunctions(String identifier, MonkeyFunctionInterface functionInterface) {
        this.identifier = identifier;
        this.builtInFunction = new BuiltInFunction(functionInterface);
    }

    public static Optional<BuiltInFunction> getFunction(String identifier) {
        if (functionsMap == null) {
            functionsMap = new HashMap<>();

            for (var entry : values()) {
                if (functionsMap.containsKey(entry.identifier)) {
                    throw new IllegalStateException("Function %s already declared".formatted(entry.identifier));
                }
                functionsMap.put(entry.identifier, entry.builtInFunction);
            }
        }

        return Optional.ofNullable(functionsMap.get(identifier));
    }
}

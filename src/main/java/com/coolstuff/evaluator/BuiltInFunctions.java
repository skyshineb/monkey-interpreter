package com.coolstuff.evaluator;

import com.coolstuff.evaluator.object.*;

import java.util.*;

public enum BuiltInFunctions {
    LEN("len", (callToken, arguments) -> {
        AbstractMonkeyFunction.checkArgumentCount(1, arguments.size());
        MonkeyObject<?> argument = arguments.get(0);

        // This function will also accept Objects
        return switch (argument) {
            case MonkeyString string -> new MonkeyInteger(string.getObject().length());
            case MonkeyArray array -> new MonkeyInteger(array.getObject().size());
            default ->
                    throw new EvaluationException("Argument to `len` not supported, got %s", argument.getType());
        };
    }),

    FIRST("first", (callToken, arguments) -> {
        AbstractMonkeyFunction.checkArgumentCount(1, arguments.size());
        MonkeyObject<?> argument = arguments.get(0);

        // This function now only accepts arrays
        return switch (argument) {
            case MonkeyArray array -> !array.getObject().isEmpty() ? array.getObject().getFirst() : MonkeyNull.INSTANCE;
            default -> throw new EvaluationException("Argument to `first` not supported, got %s", argument.getType());
        };
    }),

    LAST("last", (callToken, arguments) -> {
        AbstractMonkeyFunction.checkArgumentCount(1, arguments.size());
        MonkeyObject<?> argument = arguments.get(0);

        // This function now only accepts arrays
        return switch (argument) {
            case MonkeyArray array -> !array.getObject().isEmpty() ? array.getObject().getLast() : MonkeyNull.INSTANCE;
            default -> throw new EvaluationException("Argument to `last` not supported, got %s", argument.getType());
        };
    }),

    REST("rest", (callToken, arguments) -> {
        AbstractMonkeyFunction.checkArgumentCount(1, arguments.size());
        MonkeyObject<?> argument = arguments.get(0);

        // This function now only accepts arrays
        return switch (argument) {
            case MonkeyArray array -> {
                if (!array.getObject().isEmpty()) {
                    yield new MonkeyArray(array.getObject().subList(1, array.getObject().size()));
                } else {
                    yield MonkeyNull.INSTANCE;
                }
            }
            default -> throw new EvaluationException("Argument to `rest` not supported, got %s", argument.getType());
        };
    }),

    PUSH("push", (callToken, arguments) -> {
        AbstractMonkeyFunction.checkArgumentCount(2, arguments.size());
        AbstractMonkeyFunction.checkArgumentType(arguments.get(0), ObjectType.ARRAY_OBJ, "push");
        MonkeyArray array = (MonkeyArray) arguments.get(0);
        var copy = new ArrayList<>(array.getObject());
        copy.add(arguments.get(1));

        return new MonkeyArray(copy);
    }),

    PUTS("puts", (callToken, arguments) -> {
        for (var arg : arguments) {
            System.out.println(arg.inspect());
        }
        return MonkeyNull.INSTANCE;
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

package com.coolstuff.evaluator;

public class EvaluationException extends Exception {
    public EvaluationException(String message, Object... args) {
        super(buildMessage(message, args));
    }

    private static String buildMessage(String message, Object... args) {
        var formattedMessage = message.formatted(args);
        return "Error evaluating the program: %s".formatted(formattedMessage);
    }
}

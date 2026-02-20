package com.coolstuff.evaluator;

import com.coolstuff.token.SourcePosition;

import java.util.List;

public class EvaluationException extends Exception {
    private final RuntimeError runtimeError;

    public EvaluationException(RuntimeError runtimeError) {
        super(runtimeError.formatSingleLine());
        this.runtimeError = runtimeError;
    }

    public RuntimeError getRuntimeError() {
        return runtimeError;
    }

    public static EvaluationException from(RuntimeErrorType type, SourcePosition position, List<StackFrame> stack, String message, Object... args) {
        return new EvaluationException(new RuntimeError(type, message.formatted(args), position, List.copyOf(stack)));
    }
}

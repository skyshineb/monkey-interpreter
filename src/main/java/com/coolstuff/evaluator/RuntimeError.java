package com.coolstuff.evaluator;

import com.coolstuff.token.SourcePosition;

import java.util.List;

public record RuntimeError(RuntimeErrorType type, String message, SourcePosition position, List<StackFrame> stackFrames) {

    public String formatSingleLine() {
        return "Error[%s] at %s: %s".formatted(type, position, message);
    }

    public String formatMultiline() {
        StringBuilder builder = new StringBuilder(formatSingleLine());
        builder.append("\nStack trace:\n");
        if (stackFrames.isEmpty()) {
            builder.append("  at <repl>(0 args) @ 1:1");
            return builder.toString();
        }

        for (var frame : stackFrames) {
            builder.append("  ").append(frame).append("\n");
        }
        builder.append("  at <repl>(0 args) @ 1:1");
        return builder.toString();
    }
}

package com.coolstuff.evaluator;

import com.coolstuff.token.SourcePosition;

public record StackFrame(String functionName, SourcePosition callSite, int argumentCount) {
    @Override
    public String toString() {
        return "at %s(%d args) @ %s".formatted(functionName, argumentCount, callSite);
    }
}

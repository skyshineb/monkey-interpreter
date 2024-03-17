package com.coolstuff.ast;

import java.util.Arrays;

public record Program(Statement[] statements) implements Node {

    @Override
    public String tokenLiteral() {
        if (statements.length > 0) {
            return statements[0].tokenLiteral();
        } else {
            return "";
        }
    }

    @Override
    public String string() {
        StringBuilder builder = new StringBuilder();
        for (Statement s : statements) {
            builder.append(s.string()).append('\n');
        }
        return builder.toString();
    }
}

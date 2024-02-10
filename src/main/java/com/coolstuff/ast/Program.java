package com.coolstuff.ast;

public record Program(Statement[] statements) implements Node {

    @Override
    public String tokenLiteral() {
        if (statements.length > 0) {
            return statements[0].tokenLiteral();
        } else {
            return "";
        }
    }
}

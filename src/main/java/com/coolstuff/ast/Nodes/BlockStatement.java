package com.coolstuff.ast.Nodes;

import com.coolstuff.ast.Expression;
import com.coolstuff.ast.Statement;
import com.coolstuff.token.Token;

public record BlockStatement(Token token, Statement[] statements) implements Statement {
    @Override
    public String tokenLiteral() {
        return token.token();
    }

    @Override
    public String string() {
        StringBuilder builder = new StringBuilder();
        for (Statement s : statements) {
            builder.append(s.string()).append('\n');
        }

        return builder.toString();
    }

    @Override
    public void statementNode() {

    }
}

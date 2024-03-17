package com.coolstuff.ast.Nodes;

import com.coolstuff.ast.Expression;
import com.coolstuff.ast.Statement;
import com.coolstuff.token.Token;

public record ReturnStatement(Token token, Expression returnValue) implements Statement {

    @Override
    public String tokenLiteral() {
        return token.token();
    }

    @Override
    public String string() {
        var builder = new StringBuilder();
        builder.append(tokenLiteral()).append(' ');
        if (returnValue != null) {
            builder.append(returnValue.string());
        }
        builder.append(';');

        return builder.toString();
    }

    @Override
    public void statementNode() {

    }
}

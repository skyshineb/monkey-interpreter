package com.coolstuff.ast.Nodes;

import com.coolstuff.ast.Expression;
import com.coolstuff.ast.IdentifierExpression;
import com.coolstuff.ast.Statement;
import com.coolstuff.token.Token;

public record LetStatement(Token token, IdentifierExpression name, Expression value) implements Statement {

    @Override
    public String tokenLiteral() {
        return token.token();
    }

    @Override
    public String string() {
        var builder = new StringBuilder();
        builder.append(tokenLiteral()).append(' ');
        builder.append(name.string());
        builder.append(" = ");
        if (value != null) {
            builder.append(value.string());
        }
        builder.append(';');

        return builder.toString();
    }

    @Override
    public void statementNode() {}
}

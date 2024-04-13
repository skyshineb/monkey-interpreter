package com.coolstuff.ast;

import com.coolstuff.ast.Nodes.BlockStatement;
import com.coolstuff.token.Token;

public record IfExpression(Token token, Expression condition, BlockStatement consequence, BlockStatement alternative) implements Expression {
    @Override
    public void expressionNode() {

    }

    @Override
    public String tokenLiteral() {
        return token.token();
    }

    @Override
    public String string() {
        StringBuilder builder = new StringBuilder();
        builder.append("if");
        builder.append(condition.string());
        builder.append(' ').append(consequence.string());
        if (alternative != null) {
            builder.append("else ").append(alternative.string());
        }

        return builder.toString();
    }
}

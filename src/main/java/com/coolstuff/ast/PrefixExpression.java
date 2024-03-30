package com.coolstuff.ast;

import com.coolstuff.token.Token;

public record PrefixExpression(Token token, String operator, Expression right) implements Expression {

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
        builder.append('(');
        builder.append(operator);
        builder.append(right.string());
        builder.append(')');

        return builder.toString();
    }
}

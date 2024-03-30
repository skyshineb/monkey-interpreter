package com.coolstuff.ast;

import com.coolstuff.token.Token;

public record InfixExpression(Token token, Expression left, String operator, Expression right) implements Expression{
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
        builder.append(left.string());
        builder.append(' ').append(operator).append(' ');
        builder.append(right.string());
        builder.append(')');

        return builder.toString();
    }
}

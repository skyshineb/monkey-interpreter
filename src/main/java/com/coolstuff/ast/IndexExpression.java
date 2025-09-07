package com.coolstuff.ast;

import com.coolstuff.token.Token;

public record IndexExpression(Token token, Expression left, Expression index)  implements Expression {

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
        builder.append('[');
        builder.append(index.string());
        builder.append(']');
        builder.append(')');

        return builder.toString();
    }
}

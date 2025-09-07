package com.coolstuff.ast;

import com.coolstuff.token.Token;

import java.util.Arrays;
import java.util.stream.Collectors;

public record ArrayLiteral(Token token, Expression[] elements) implements Expression {
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
        builder.append(tokenLiteral());
        builder.append(Arrays.stream(elements).map(Expression::string).collect(Collectors.joining(", ")));
        builder.append("]");
        return builder.toString();
    }
}

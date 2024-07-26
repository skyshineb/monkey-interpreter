package com.coolstuff.ast;

import com.coolstuff.token.Token;

import java.util.Arrays;
import java.util.stream.Collectors;

public record CallExpression(Token token, Expression function, Expression[] arguments) implements Expression{

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
        var args = Arrays.stream(arguments).map(Expression::string).collect(Collectors.joining(", "));
        builder.append(function.string());
        builder.append("(");
        builder.append(args);
        builder.append(")");

        return builder.toString();
    }
}

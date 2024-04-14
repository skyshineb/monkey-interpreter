package com.coolstuff.ast;

import com.coolstuff.ast.Nodes.BlockStatement;
import com.coolstuff.token.Token;

import java.util.Arrays;
import java.util.stream.Collectors;

public record FunctionLiteral(Token token, IdentifierExpression[] parameters, BlockStatement body) implements Expression{

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
        builder.append("(");
        builder.append(Arrays.stream(parameters).map(String::valueOf).collect(Collectors.joining(" ")));
        builder.append(")");
        builder.append(body.string());

        return builder.toString();
    }
}

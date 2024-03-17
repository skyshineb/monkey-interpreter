package com.coolstuff.ast;

import com.coolstuff.token.Token;

public record IdentifierExpression(Token token, String value) implements Expression {

    @Override
    public String tokenLiteral() {
        return token.token();
    }

    @Override
    public String string() {
        return value;
    }

    @Override
    public void expressionNode() {}
}

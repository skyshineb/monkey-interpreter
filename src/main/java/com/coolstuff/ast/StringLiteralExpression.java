package com.coolstuff.ast;

import com.coolstuff.token.Token;

public record StringLiteralExpression(Token token, String value) implements Expression {
    @Override
    public void expressionNode() {

    }

    @Override
    public String tokenLiteral() {
        return token.token();
    }

    @Override
    public String string() {
        return token.token();
    }
}

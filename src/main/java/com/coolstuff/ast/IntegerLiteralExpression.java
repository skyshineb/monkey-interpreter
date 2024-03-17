package com.coolstuff.ast;

import com.coolstuff.token.Token;

public record IntegerLiteralExpression(Token token, long value) implements Expression{

    @Override
    public void expressionNode() {}

    @Override
    public String tokenLiteral() {
        return token.token();
    }

    @Override
    public String string() {
        return token.token();
    }
}

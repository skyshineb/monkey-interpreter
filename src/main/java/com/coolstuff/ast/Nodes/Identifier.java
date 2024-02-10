package com.coolstuff.ast.Nodes;

import com.coolstuff.ast.Expression;
import com.coolstuff.token.Token;

public record Identifier(Token token, String value) implements Expression {

    @Override
    public String tokenLiteral() {
        return token.token();
    }

    @Override
    public void expressionNode() {}
}

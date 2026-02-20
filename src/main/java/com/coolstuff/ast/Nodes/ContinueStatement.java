package com.coolstuff.ast.Nodes;

import com.coolstuff.ast.Statement;
import com.coolstuff.token.Token;

public record ContinueStatement(Token token) implements Statement {
    @Override
    public String tokenLiteral() {
        return token.token();
    }

    @Override
    public String string() {
        return tokenLiteral() + ';';
    }

    @Override
    public void statementNode() {

    }
}

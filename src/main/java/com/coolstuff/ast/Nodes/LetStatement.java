package com.coolstuff.ast.Nodes;

import com.coolstuff.ast.Expression;
import com.coolstuff.ast.Statement;
import com.coolstuff.token.Token;

public record LetStatement(Token token, Identifier name, Expression value) implements Statement {

    @Override
    public String tokenLiteral() {
        return token.token();
    }

    @Override
    public void statementNode() {}
}

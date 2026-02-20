package com.coolstuff.ast.Nodes;

import com.coolstuff.ast.Expression;
import com.coolstuff.ast.Statement;
import com.coolstuff.token.Token;

public record WhileStatement(Token token, Expression condition, BlockStatement body) implements Statement {
    @Override
    public String tokenLiteral() {
        return token.token();
    }

    @Override
    public String string() {
        var builder = new StringBuilder();
        builder.append(tokenLiteral());
        builder.append(" (");
        builder.append(condition.string());
        builder.append(") {");
        builder.append(body.string());
        builder.append('}');
        return builder.toString();
    }

    @Override
    public void statementNode() {

    }
}

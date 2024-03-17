package com.coolstuff.ast.Nodes;

import com.coolstuff.ast.Expression;
import com.coolstuff.ast.Statement;
import com.coolstuff.token.Token;

public record ExpressionStatement(Token token, Expression expression)  implements Statement {

    @Override
    public String tokenLiteral() {
        return token.token();
    }

    @Override
    public void statementNode() {}

    @Override
    public String string() {
        if (expression != null) {
            return expression.string();
        }

        return "";
    }
}

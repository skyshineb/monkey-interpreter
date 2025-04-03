package com.coolstuff.ast.Nodes;

import com.coolstuff.ast.Statement;
import com.coolstuff.token.Token;

import java.util.Arrays;
import java.util.stream.Collectors;

public record BlockStatement(Token token, Statement[] statements) implements Statement {
    @Override
    public String tokenLiteral() {
        return token.token();
    }

    @Override
    public String string() {
        return Arrays.stream(statements).map(Statement::string).collect(Collectors.joining("\n"));
    }

    @Override
    public void statementNode() {

    }
}

package com.coolstuff.parser;

import com.coolstuff.ast.*;
import com.coolstuff.ast.Nodes.ExpressionStatement;
import com.coolstuff.ast.Nodes.LetStatement;
import com.coolstuff.ast.Nodes.ReturnStatement;
import com.coolstuff.lexer.Lexer;
import com.coolstuff.token.Token;
import com.coolstuff.token.TokenType;

import java.util.ArrayList;
import java.util.List;

import static com.coolstuff.parser.Precedence.LOWEST;

public class Parser {
    final Lexer lexer;
    final List<String> errors;

    Token curToken;
    Token peekToken;

    public Parser(Lexer lexer) {
        this.lexer = lexer;

        nextToken();
        nextToken();
        errors = new ArrayList<>();
    }

    void nextToken() {
        this.curToken = peekToken;
        this.peekToken = lexer.nextToken();
    }

    public Program parseProgram() {
        ArrayList<Statement> statements = new ArrayList<>();
        while (!curTokenIs(TokenType.EOF)) {
            Statement stmt = parseStatement();
            if (stmt != null) {
                statements.add(stmt);
            }

            nextToken();
        }

        return new Program(statements.toArray(Statement[]::new));
    }

    private Statement parseStatement() {
        switch (curToken.type()) {
            case LET -> {
                return parseLetStatement();
            }
            case RETURN -> {
                return parseReturnStatement();
            }
            default -> {
                return parseExpressionStatement();
            }
        }
    }

    private ExpressionStatement parseExpressionStatement() {
        var token = curToken;
        var expression = parseExpression(LOWEST);

        if (peekTokenIs(TokenType.SEMICOLON)) {
            nextToken();
        }

        return new ExpressionStatement(token, expression);
    }

    private Expression parseExpression(Precedence precedence) {
        var prefix = prefixParseFn();
        if (prefix == null) {
            return null;
        }
        var leftExpr = prefix.get();

        return leftExpr;
    }

    private ReturnStatement parseReturnStatement() {
        var token = curToken;

        nextToken();

        // TODO: We're skipping an expression until we encounter a semicolon
        while (!curTokenIs(TokenType.SEMICOLON)) {
            nextToken();
        }

        return new ReturnStatement(token, null);
    }

    private LetStatement parseLetStatement() {
        var token = curToken;

        if (!expectPeek(TokenType.IDENT)) {
            return null;
        }

        var name = new IdentifierExpression(curToken, curToken.token());

        if (!expectPeek(TokenType.ASSIGN)) {
            return null;
        }

        // TODO: We're skipping an expression until we encounter a semicolon
        while (!curTokenIs(TokenType.SEMICOLON)) {
            nextToken();
        }

        return new LetStatement(token, name, null);
    }

    private boolean curTokenIs(TokenType t) {
        return curToken.type() == t;
    }

    private boolean peekTokenIs(TokenType t) {
        return peekToken.type() == t;
    }

    private boolean expectPeek(TokenType t) {
        if (peekTokenIs(t)) {
            nextToken();
            return true;
        } else {
            peekError(t);
            return false;
        }
    }

    private void peekError(TokenType t) {
        var msg = String.format("Expected next token to be %s type, got %s instead", t, peekToken.type());
        errors.add(msg);
    }

    public List<String> getErrors() {
        return errors;
    }

    public ParserSupplier<Expression> prefixParseFn() {
        return switch (curToken.type()) {
            case IDENT -> () -> new IdentifierExpression(curToken, curToken.token());
            case INT -> this::parseIntegerLiteral;
            default -> null;
        };
    }

    private Expression parseIntegerLiteral() {
        try {
            long val = Long.parseLong(curToken.token());
            return new IntegerLiteralExpression(curToken, val);
        } catch (NumberFormatException e) {
            errors.add(String.format("Could not parse %s as integer", curToken.token()));
            return null;
        }
    }

    public ParserFunction<Expression, Expression> infixParseFn(Expression left) {
        return null;
    }
}

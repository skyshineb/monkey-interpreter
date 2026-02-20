package com.coolstuff.parser;

import com.coolstuff.ast.*;
import com.coolstuff.ast.Nodes.BlockStatement;
import com.coolstuff.ast.Nodes.ExpressionStatement;
import com.coolstuff.ast.Nodes.LetStatement;
import com.coolstuff.ast.Nodes.ReturnStatement;
import com.coolstuff.lexer.Lexer;
import com.coolstuff.token.Token;
import com.coolstuff.token.TokenType;

import java.util.ArrayList;
import java.util.List;

import static com.coolstuff.parser.Precedence.LOWEST;
import static com.coolstuff.parser.Precedence.PREFIX;

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
            noPrefixParseFnError(curToken.token());
            return null;
        }
        var leftExpr = prefix.get();

        while (!peekTokenIs(TokenType.SEMICOLON) && precedence.ordinal() < peekPrecedence().ordinal()) {
            var infix = infixParseFn(peekToken.type());
            if (infix == null) {
                return leftExpr;
            }

            nextToken();

            leftExpr = infix.apply(leftExpr);
        }

        return leftExpr;
    }

    private void noPrefixParseFnError(String tokenType) {
        errors.add(String.format("no prefix parse function for %s found", tokenType));
    }

    private ReturnStatement parseReturnStatement() {
        var token = curToken;

        nextToken();

        var returnValue = parseExpression(LOWEST);

        if (peekTokenIs(TokenType.SEMICOLON)) {
            nextToken();
        }

        return new ReturnStatement(token, returnValue);
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

        nextToken();

        var value = parseExpression(LOWEST);

        if (peekTokenIs(TokenType.SEMICOLON)) {
            nextToken();
        }

        return new LetStatement(token, name, value);
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
            case BANG, MINUS -> this::parsePrefixExpression;
            case TRUE, FALSE ->  this::parseBooleanExpression;
            case LPAREN -> this::parseGroupedExpression;
            case IF -> this::parseIfExpression;
            case FUNCTION -> this::parseFunctionLiteral;
            case STRING -> this::parseStringLiteral;
            case LBRACKET -> this::parseArrayLiteral;
            case LBRACE -> this::parseHashLiteral;
            default -> null;
        };
    }

    private Expression parseHashLiteral() {
        Token token = curToken;
        var expressions = new ArrayList<KVPair>();

        while (!peekTokenIs(TokenType.RBRACE)) {
            nextToken();

            var key = parseExpression(LOWEST);
            if (!expectPeek(TokenType.COLON)) {
                return null;
            }
            nextToken();

            var value = parseExpression(LOWEST);

            if (!peekTokenIs(TokenType.RBRACE) && !expectPeek(TokenType.COMMA)) {
                return null;
            }
            expressions.add(new KVPair(key, value));
        }

        if (!expectPeek(TokenType.RBRACE)) {
            return null;
        }

        return new HashLiteral(token, expressions);
    }

    private Expression parseArrayLiteral() {
        Token token = curToken;
        var elements = parseExpressionList(TokenType.RBRACKET);
        return new ArrayLiteral(token, elements);
    }

    private Expression[] parseExpressionList(TokenType end) {
        ArrayList<Expression> expressionList = new ArrayList<>();

        if (peekTokenIs(end)) {
            nextToken();
            return new Expression[0];
        }

        nextToken();
        expressionList.add(parseExpression(LOWEST));

        while (peekTokenIs(TokenType.COMMA)) {
            nextToken();
            nextToken();
            expressionList.add(parseExpression(LOWEST));
        }

        if (!expectPeek(end)) {
            return null;
        }

        return expressionList.toArray(new Expression[0]);
    }

    private Expression parseStringLiteral() {
        return new StringLiteralExpression(curToken, curToken.token());
    }

    private Expression parseFunctionLiteral() {
        Token token = curToken;

        if (!expectPeek(TokenType.LPAREN)) {
            return null;
        }

        var parameters = parseFunctionParameters();

        if (!expectPeek(TokenType.LBRACE)) {
            return null;
        }

        var body = parseBlockStatement();

        return new FunctionLiteral(token, parameters, body);
    }

    private IdentifierExpression[] parseFunctionParameters() {
        ArrayList<IdentifierExpression> identifiers = new ArrayList<>();
        if (peekTokenIs(TokenType.RPAREN)) {
            nextToken();
            return new IdentifierExpression[]{};
        }

        nextToken();
        identifiers.add(new IdentifierExpression(curToken, curToken.token()));

        while (peekTokenIs(TokenType.COMMA)) {
            nextToken();
            nextToken();
            identifiers.add(new IdentifierExpression(curToken, curToken.token()));
        }

        if (!expectPeek(TokenType.RPAREN)) {
            return null;
        }

        return identifiers.toArray(IdentifierExpression[]::new);
    }

    private Expression parseIfExpression() {
        Token token = curToken;

        if (!expectPeek(TokenType.LPAREN)) {
            return null;
        }

        nextToken();

        var condition = parseExpression(LOWEST);

        if (!expectPeek(TokenType.RPAREN)) {
            return null;
        }

        if (!expectPeek(TokenType.LBRACE)) {
            return null;
        }

        var consequence = parseBlockStatement();

        BlockStatement alternative = null;
        if (peekTokenIs(TokenType.ELSE)) {
            nextToken();
            if (!expectPeek(TokenType.LBRACE)) {
                return null;
            }

            alternative = parseBlockStatement();
        }

        return new IfExpression(token, condition, consequence, alternative);
    }

    private BlockStatement parseBlockStatement() {
        Token token = curToken;
        var statements = new ArrayList<Statement>();
        nextToken();

        while (!curTokenIs(TokenType.RBRACE) && !curTokenIs(TokenType.EOF)) {
            var stmt = parseStatement();
            if (stmt != null) {
                statements.add(stmt);
            }
            nextToken();
        }

        return new BlockStatement(token, statements.toArray(Statement[]::new));
    }

    private Expression parseGroupedExpression() {
        nextToken();
        var expr = parseExpression(LOWEST);

        if (!expectPeek(TokenType.RPAREN)) {
            return null;
        }

        return expr;
    }

    private Expression parseBooleanExpression() {
        return new BooleanExpression(curToken, curTokenIs(TokenType.TRUE));
    }

    private Expression parsePrefixExpression() {
        Token token = curToken;
        String operator = curToken.token();

        nextToken();

        Expression right = parseExpression(PREFIX);
        return new PrefixExpression(token, operator, right);
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

    private ParserFunction<Expression, Expression> infixParseFn(TokenType type) {
        return switch (type) {
            case PLUS, MINUS, SLASH, ASTERISK, EQ, NOT_EQ, LT, GT, LTE, GTE -> this::parseInfixExpression;
            case LPAREN -> this::parseCallExpression;
            case LBRACKET -> this::parseIndexExpression;
            default -> null;
        };
    }

    private Expression parseIndexExpression(Expression left) {
        Token token = curToken;

        nextToken();
        var index = parseExpression(LOWEST);

        if (!expectPeek(TokenType.RBRACKET)) {
            return null;
        }

        return new IndexExpression(token, left, index);
    }

    private Expression parseCallExpression(Expression function) {
        return new CallExpression(curToken, function, parseCallArguments());
    }

    private Expression[] parseCallArguments() {
        var args = new ArrayList<Expression>();

        if (peekTokenIs(TokenType.RPAREN)) {
            nextToken();
            return args.toArray(Expression[]::new);
        }

        nextToken();
        args.add(parseExpression(LOWEST));

        while (peekTokenIs(TokenType.COMMA)) {
            nextToken();
            nextToken();
            args.add(parseExpression(LOWEST));
        }

        if (!expectPeek(TokenType.RPAREN)) {
            return null;
        }

        return args.toArray(Expression[]::new);
    }

    private Expression parseInfixExpression(Expression left) {
        Token token = curToken;
        String operator = curToken.token();

        var precedence = curPrecedence();
        nextToken();
        var right = parseExpression(precedence);

        return new InfixExpression(token, left, operator, right);
    }

    private Precedence curPrecedence() {
        return Precedence.precedenceForToken(curToken.type());
    }

    private Precedence peekPrecedence() {
        return Precedence.precedenceForToken(peekToken.type());
    }
}

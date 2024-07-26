package com.coolstuff;

import com.coolstuff.ast.IdentifierExpression;
import com.coolstuff.ast.Nodes.LetStatement;
import com.coolstuff.ast.Program;
import com.coolstuff.ast.Statement;
import com.coolstuff.token.Token;
import com.coolstuff.token.TokenType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ASTTest {

    @Test
    public void testString() {
        var program = new Program(new Statement[]{
                new LetStatement(
                        new Token(TokenType.LET, "let"),
                        new IdentifierExpression(
                                new Token(TokenType.IDENT, "myVar"),
                                "myVar"
                        ),
                        new IdentifierExpression(
                                new Token(TokenType.IDENT, "anotherVar"),
                                "anotherVar"
                        )
                )
        });

        Assertions.assertEquals("let myVar = anotherVar;", program.string());
    }
}

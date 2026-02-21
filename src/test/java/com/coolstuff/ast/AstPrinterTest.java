package com.coolstuff.ast;

import com.coolstuff.cli.MonkeyPipeline;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AstPrinterTest {

    @Test
    public void printsLetAndReturnStatements() {
        var parseResult = new MonkeyPipeline().parseProgram("let answer = 42; return answer;");

        Assertions.assertTrue(parseResult.errors().isEmpty());
        Assertions.assertEquals("""
                Program
                  LetStatement
                    Name
                      Identifier(answer)
                    Value
                      IntegerLiteral(42)
                  ReturnStatement
                    Value
                      Identifier(answer)
                """, AstPrinter.print(parseResult.program()));
    }

    @Test
    public void printsConditionalFunctionsAndCalls() {
        var parseResult = new MonkeyPipeline().parseProgram("if (x < y) { fn(a) { a + 1; }(y); } else { return x; }");

        Assertions.assertTrue(parseResult.errors().isEmpty());
        Assertions.assertEquals("""
                Program
                  ExpressionStatement
                    Expression
                      IfExpression
                        Condition
                          InfixExpression(<)
                            Left
                              Identifier(x)
                            Right
                              Identifier(y)
                        Consequence
                          BlockStatement
                            ExpressionStatement
                              Expression
                                CallExpression
                                  Function
                                    FunctionLiteral
                                      Parameters
                                        Identifier(a)
                                      Body
                                        BlockStatement
                                          ExpressionStatement
                                            Expression
                                              InfixExpression(+)
                                                Left
                                                  Identifier(a)
                                                Right
                                                  IntegerLiteral(1)
                                  Arguments
                                    Identifier(y)
                        Alternative
                          BlockStatement
                            ReturnStatement
                              Value
                                Identifier(x)
                """, AstPrinter.print(parseResult.program()));
    }

    @Test
    public void printsArraysHashesAndIndexes() {
        var parseResult = new MonkeyPipeline().parseProgram("let v = {\"k\": [1, 2, 3][0]};");

        Assertions.assertTrue(parseResult.errors().isEmpty());
        Assertions.assertEquals("""
                Program
                  LetStatement
                    Name
                      Identifier(v)
                    Value
                      HashLiteral
                        Pair[0]
                          Key
                            StringLiteral(\"k\")
                          Value
                            IndexExpression
                              Left
                                ArrayLiteral
                                  IntegerLiteral(1)
                                  IntegerLiteral(2)
                                  IntegerLiteral(3)
                              Index
                                IntegerLiteral(0)
                """, AstPrinter.print(parseResult.program()));
    }
}

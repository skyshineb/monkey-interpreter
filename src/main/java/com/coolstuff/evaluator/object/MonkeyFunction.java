package com.coolstuff.evaluator.object;

import com.coolstuff.ast.IdentifierExpression;
import com.coolstuff.ast.Nodes.BlockStatement;
import com.coolstuff.evaluator.Environment;

import java.util.Arrays;

public class MonkeyFunction extends MonkeyObject<MonkeyNull> {
    private final IdentifierExpression[] parameters;
    private final BlockStatement body;
    private final Environment environment;

    public MonkeyFunction(IdentifierExpression[] parameters, BlockStatement body, Environment environment) {
        super(ObjectType.FUNCTION_OBJ);
        this.parameters = parameters;
        this.body = body;
        this.environment = environment;
    }

    @Override
    public String inspect() {
        return "fn(%s){\n%s\n}".formatted(Arrays.toString(parameters), body.string());
    }

    public IdentifierExpression[] getParameters() {
        return parameters;
    }

    public BlockStatement getBody() {
        return body;
    }

    public Environment getEnvironment() {
        return environment;
    }
}

package com.coolstuff.evaluator.object;

import com.coolstuff.ast.FunctionLiteral;
import com.coolstuff.evaluator.AbstractMonkeyFunction;
import com.coolstuff.evaluator.Environment;

import java.util.Arrays;

public class MonkeyFunction extends AbstractMonkeyFunction {

    private final FunctionLiteral functionLiteral;

    public MonkeyFunction(Environment creationEnv, FunctionLiteral functionLiteral) {
        super(ObjectType.FUNCTION_OBJ);
        this.functionLiteral = functionLiteral;

        setObject(((callToken, arguments, evaluator) -> {
            var parameters = functionLiteral.parameters();
            var body = functionLiteral.body();

            checkArgumentCount(parameters.length, arguments.size(), callToken, evaluator);

            var environment = new Environment(creationEnv);
            for (int i = 0; i < parameters.length; i++) {
                var parameter = parameters[i];
                environment.set(parameter.value(), arguments.get(i));
            }

            return evaluator.child(environment).eval(body);
        }));
    }

    public FunctionLiteral getFunctionLiteral() {
        return functionLiteral;
    }

    @Override
    public String inspect() {
        return "fn(%s){\n%s\n}".formatted(Arrays.toString(functionLiteral.parameters()), functionLiteral.body().string());
    }
}

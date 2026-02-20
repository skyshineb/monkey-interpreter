package com.coolstuff.evaluator.object;

import com.coolstuff.evaluator.AbstractMonkeyFunction;
import com.coolstuff.evaluator.MonkeyFunctionInterface;

public class BuiltInFunction extends AbstractMonkeyFunction {
    private final String name;

    public BuiltInFunction(String name, MonkeyFunctionInterface functionInterface) {
        super(ObjectType.BUILTIN_OBJ);
        this.name = name;
        setObject(functionInterface);
    }

    public String getName() {
        return name;
    }

    @Override
    public String inspect() {
        return "builtin function";
    }
}

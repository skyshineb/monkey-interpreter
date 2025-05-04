package com.coolstuff.evaluator.object;

import com.coolstuff.evaluator.AbstractMonkeyFunction;
import com.coolstuff.evaluator.MonkeyFunctionInterface;

public class BuiltInFunction extends AbstractMonkeyFunction {

    public BuiltInFunction(MonkeyFunctionInterface functionInterface) {
        super(ObjectType.BUILTIN_OBJ);
        setObject(functionInterface);
    }

    @Override
    public String inspect() {
        return "builtin function";
    }
}

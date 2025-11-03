package com.coolstuff.evaluator.object;

import com.coolstuff.evaluator.HashKey;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class MonkeyHash extends MonkeyObject<Map<HashKey, MonkeyObject<?>>>{

    public MonkeyHash(Map<HashKey, MonkeyObject<?>> pairs) {
        super(ObjectType.HASH_OBJ);
        setObject(Collections.unmodifiableMap(pairs));
    }

    @Override
    public String inspect() {
        var pairsString = getObject()
                .entrySet()
                .stream()
                .map(pair -> "%s : %s".formatted(pair.getKey().getOriginalObject().inspect(), pair.getValue().inspect()))
                .collect(Collectors.joining(", "));

        return "{%s}".formatted(pairsString);
    }
}

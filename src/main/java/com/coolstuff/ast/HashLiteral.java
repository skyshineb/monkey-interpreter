package com.coolstuff.ast;

import com.coolstuff.token.Token;

import java.util.List;
import java.util.stream.Collectors;

public record HashLiteral(Token token, List<KVPair> pairs) implements Expression{

    @Override
    public void expressionNode() {

    }

    @Override
    public String tokenLiteral() {
        return token.token();
    }

    @Override
    public String string() {
        var pairsString = pairs
                .stream()
                .map(pair -> "%s : %s".formatted(pair.key().string(), pair.value().string()))
                .collect(Collectors.joining(", "));

        return "{%s}".formatted(pairsString);
    }
}

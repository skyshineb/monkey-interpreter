package com.coolstuff.parser;

@FunctionalInterface
public interface ParserFunction<T, R> {
    R apply(T t);
}

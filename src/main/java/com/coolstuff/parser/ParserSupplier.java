package com.coolstuff.parser;


@FunctionalInterface
public interface ParserSupplier<T> {
    T get();
}

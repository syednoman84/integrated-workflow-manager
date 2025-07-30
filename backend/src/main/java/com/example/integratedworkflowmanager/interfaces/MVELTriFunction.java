package com.example.integratedworkflowmanager.interfaces;

@FunctionalInterface
public interface MVELTriFunction<T, U, V, R> {
    R apply(T t, U u, V v);
}

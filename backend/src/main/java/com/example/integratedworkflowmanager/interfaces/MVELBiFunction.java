package com.example.integratedworkflowmanager.interfaces;

@FunctionalInterface
public interface MVELBiFunction<T, U, R> {
    R apply(T t, U u);
}

package com.example.integratedworkflowmanager.interfaces;

@FunctionalInterface
public interface MVELFunction<T, R> {
    R apply(T t);
}
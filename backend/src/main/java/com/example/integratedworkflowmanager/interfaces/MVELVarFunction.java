package com.example.integratedworkflowmanager.interfaces;

@FunctionalInterface
public interface MVELVarFunction<R> {
    R apply(Object... args);
}

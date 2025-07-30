package com.example.integratedworkflowmanager.interfaces;

import java.util.*;
import com.example.integratedworkflowmanager.util.ExpressionUtils;
import com.example.integratedworkflowmanager.util.MathUtils;
import com.example.integratedworkflowmanager.util.StringUtils;

public class FunctionRegistry {

    public static Map<String, Object> getMvelContext(Map<String, Object> inputContext) {
        Map<String, Object> context = new HashMap<>(inputContext);

        // Static utility classes
        context.put("base64", ExpressionUtils.class);
        context.put("math", MathUtils.class);
        context.put("stringUtils", StringUtils.class);
    
        // MVELFunction (Single-arg)
        context.put("toUpper", (MVELFunction<String, String>) String::toUpperCase);
        context.put("trim", (MVELFunction<String, String>) String::trim);
        context.put("square", (MVELFunction<Integer, Integer>) x -> x * x);
        context.put("isEven", (MVELFunction<Integer, Boolean>) x -> x % 2 == 0);
        context.put("doubleIt", (MVELFunction<Number, Number>) x -> x.doubleValue() * 2);

        // MVELBiFunction (Two-arg)
        context.put("add", (MVELBiFunction<Number, Number, Number>) (a, b) -> a.doubleValue() + b.doubleValue());
        context.put("subtract", (MVELBiFunction<Number, Number, Number>) (a, b) -> a.doubleValue() - b.doubleValue());
        context.put("concat", (MVELBiFunction<String, String, String>) (a, b) -> a + b);
        context.put("min", (MVELBiFunction<Number, Number, Number>) (a, b) -> Math.min(a.doubleValue(), b.doubleValue()));
        context.put("max", (MVELBiFunction<Number, Number, Number>) (a, b) -> Math.max(a.doubleValue(), b.doubleValue()));

        // MVELTriFunction (Three-arg)
        context.put("between", (MVELTriFunction<Number, Number, Number, Boolean>)
                (val, min, max) -> {
                    double v = val.doubleValue();
                    return v >= min.doubleValue() && v <= max.doubleValue();
                });

        context.put("padString", (MVELTriFunction<String, String, Integer, String>)
                (str, padChar, length) -> {
                    StringBuilder sb = new StringBuilder(str);
                    while (sb.length() < length) {
                        sb.append(padChar);
                    }
                    return sb.toString();
                });

        // MVELVarFunction (Variadic)
        context.put("sum", (MVELVarFunction<Number>) args -> Arrays.stream(args)
                .mapToDouble(arg -> ((Number) arg).doubleValue())
                .sum());

        context.put("avg", (MVELVarFunction<Number>) args -> {
            return Arrays.stream(args)
                    .mapToDouble(arg -> ((Number) arg).doubleValue())
                    .average()
                    .orElse(0);
        });

        return context;
    }
}

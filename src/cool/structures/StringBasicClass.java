package cool.structures;

import java.util.HashMap;
import java.util.List;

public class StringBasicClass extends ObjectBasicClass{
    public StringBasicClass(Scope globalScope) {
        super(globalScope, "String");
        HashMap<String, List<String>> methods = new HashMap<>();
        methods.put("length", List.of("Int"));
        methods.put("concat", List.of("String", "s", "String"));
        methods.put("substr", List.of("String", "i", "Int", "l", "Int"));
        addMethod(globalScope, methods);
    }
}

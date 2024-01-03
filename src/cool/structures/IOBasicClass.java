package cool.structures;

import java.util.HashMap;
import java.util.List;

public class IOBasicClass extends ObjectBasicClass{
    public IOBasicClass(Scope globalScope) {
        super(globalScope, "IO");
        HashMap<String, List<String>> methods = new HashMap<>();
        methods.put("out_string", List.of("SELF_TYPE", "x", "String"));
        methods.put("out_int", List.of("SELF_TYPE", "x", "Int"));
        methods.put("in_string", List.of("String"));
        methods.put("in_int", List.of("Int"));
        addMethod(globalScope, methods);
    }
}

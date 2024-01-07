package cool.structures;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ObjectBasicClass extends IdSymbol {
    public ObjectBasicClass(Scope globalScope, String name) {
        super(name);
        this.type = new ClassSymbol(globalScope, name);
        HashMap<String, List<String>> methods = new HashMap<>();;
        methods.put("abort", List.of("Object"));
        methods.put("type_name", List.of("String"));
        methods.put("copy", List.of("SELF_TYPE"));
        addMethod(globalScope, methods);
    }

    protected void addMethod(Scope globalScope, HashMap<String, List<String>> methods) {
        for (var method : methods.entrySet()) {
            var methodSymbol = new MethodSymbol(globalScope, method.getKey());
            methodSymbol.setReturnType(new ClassSymbol(globalScope, method.getValue().get(0)));
            for (int i = 1; i < method.getValue().size(); i+=2) {
                var idSymbol = new IdSymbol(method.getValue().get(i));
                idSymbol.setType(new ClassSymbol(globalScope, method.getValue().get(i+1)));
                methodSymbol.getFormals().put(idSymbol.getName(), idSymbol);
            }
            this.type.add(methodSymbol);
        }
    }
}

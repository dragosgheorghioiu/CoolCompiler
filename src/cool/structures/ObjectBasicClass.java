package cool.structures;

public class ObjectBasicClass extends IdSymbol {
    public ObjectBasicClass(Scope globalScope, String name) {
        super(name);
        this.type = new ClassSymbol(globalScope, name);
        type.methodSymbols.put("abort", new MethodSymbol(globalScope, "abort"));
        type.methodSymbols.put("type_name", new MethodSymbol(globalScope, "type_name"));
        type.methodSymbols.put("copy", new MethodSymbol(globalScope, "copy"));
    }
}

package cool.structures;

public class IOBasicClass extends ObjectBasicClass{
    public IOBasicClass(Scope globalScope) {
        super(globalScope, "IO");
        type.methodSymbols.put("out_string", new MethodSymbol(globalScope, "out_string"));
        type.methodSymbols.put("out_int", new MethodSymbol(globalScope, "out_int"));
        type.methodSymbols.put("in_string", new MethodSymbol(globalScope, "in_string"));
        type.methodSymbols.put("in_int", new MethodSymbol(globalScope, "in_int"));
        this.getType().setParentClass("Object");
    }
}

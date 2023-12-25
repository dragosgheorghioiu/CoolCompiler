package cool.structures;

public class StringBasicClass extends ObjectBasicClass{
    public StringBasicClass(Scope globalScope) {
        super(globalScope, "String");
        type.methodSymbols.put("length", new MethodSymbol(globalScope, "length"));
        type.methodSymbols.put("concat", new MethodSymbol(globalScope, "concat"));
        type.methodSymbols.put("substr", new MethodSymbol(globalScope, "substr"));
    }
}

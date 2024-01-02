package cool.structures;

public class IOBasicClass extends ObjectBasicClass{
    public IOBasicClass(Scope globalScope) {
        super(globalScope, "IO");
        var out_stringMethod = new MethodSymbol(globalScope, "out_string");
        out_stringMethod.setReturnType(new ClassSymbol(globalScope, "SELF_TYPE"));
        out_stringMethod.getFormals().put("x", new ClassSymbol(globalScope, "String"));
        var out_intMethod = new MethodSymbol(globalScope, "out_int");
        out_intMethod.getFormals().put("x", new ClassSymbol(globalScope, "Int"));
        out_intMethod.setReturnType(new ClassSymbol(globalScope, "SELF_TYPE"));
        var in_stringMethod = new MethodSymbol(globalScope, "in_string");
        in_stringMethod.setReturnType(new ClassSymbol(globalScope, "String"));
        var in_intMethod = new MethodSymbol(globalScope, "in_int");
        in_intMethod.setReturnType(new ClassSymbol(globalScope, "Int"));
        this.getType().add(out_stringMethod);
        this.getType().add(out_intMethod);
        this.getType().add(in_stringMethod);
        this.getType().add(in_intMethod);
        this.getType().setParentClass("Object");
    }
}

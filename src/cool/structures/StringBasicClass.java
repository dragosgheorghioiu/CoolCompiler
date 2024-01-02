package cool.structures;

public class StringBasicClass extends ObjectBasicClass{
    public StringBasicClass(Scope globalScope) {
        super(globalScope, "String");
        var lengthMethod = new MethodSymbol(globalScope, "length");
        lengthMethod.setReturnType(new ClassSymbol(globalScope, "Int"));
        var concatMethod = new MethodSymbol(globalScope, "concat");
        concatMethod.setReturnType(new ClassSymbol(globalScope, "String"));
        concatMethod.getFormals().put("s", new ClassSymbol(globalScope, "String"));
        var substrMethod = new MethodSymbol(globalScope, "substr");
        substrMethod.setReturnType(new ClassSymbol(globalScope, "String"));
        substrMethod.getFormals().put("i", new ClassSymbol(globalScope, "Int"));
        substrMethod.getFormals().put("l", new ClassSymbol(globalScope, "Int"));
        this.getType().add(lengthMethod);
        this.getType().add(concatMethod);
        this.getType().add(substrMethod);
        this.getType().setParentClass("Object");
    }
}

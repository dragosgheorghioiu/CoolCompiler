package cool.structures;

public class ObjectBasicClass extends IdSymbol {
    public ObjectBasicClass(Scope globalScope, String name) {
        super(name);
        this.type = new ClassSymbol(globalScope, name);
        var abortMethod = new MethodSymbol(globalScope, "abort");
        abortMethod.setReturnType(new ClassSymbol(globalScope, "Object"));
        var type_nameMethod = new MethodSymbol(globalScope, "type_name");
        type_nameMethod.setReturnType(new ClassSymbol(globalScope, "String"));
        var copyMethod = new MethodSymbol(globalScope, "copy");
        copyMethod.setReturnType(new ClassSymbol(globalScope, "SELF_TYPE"));
        this.type.add(abortMethod);
        this.type.add(type_nameMethod);
        this.type.add(copyMethod);
    }
}

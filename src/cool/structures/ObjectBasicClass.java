package cool.structures;

public class ObjectBasicClass extends IdSymbol {
    public ObjectBasicClass(Scope globalScope, String name) {
        super(name);
        this.type = new ClassSymbol(globalScope, name);
    }
}

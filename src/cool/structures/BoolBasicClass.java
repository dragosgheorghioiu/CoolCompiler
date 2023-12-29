package cool.structures;

public class BoolBasicClass extends ObjectBasicClass{
    public BoolBasicClass(Scope globalScope) {
        super(globalScope, "Bool");
        this.getType().setParentClass("Object");
    }
}

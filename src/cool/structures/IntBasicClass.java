package cool.structures;

public class IntBasicClass extends ObjectBasicClass{
    public IntBasicClass(Scope globalScope) {
        super(globalScope, "Int");
        this.getType().setParentClass("Object");
    }
}

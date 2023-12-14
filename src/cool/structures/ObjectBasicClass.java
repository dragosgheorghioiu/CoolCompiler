package cool.structures;

public class ObjectBasicClass extends IdSymbol {
    public ObjectBasicClass(Scope globalScope, String name) {
        super(name);
        this.type = new ClassSymbol(globalScope, name);
        type.symbols.put("abort", new FeatureSymbol("abort"));
        type.symbols.put("type_name", new FeatureSymbol("type_name"));
        type.symbols.put("copy", new FeatureSymbol("copy"));
    }
}

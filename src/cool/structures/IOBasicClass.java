package cool.structures;

public class IOBasicClass extends ObjectBasicClass{
    public IOBasicClass(Scope globalScope) {
        super(globalScope, "IO");
        type.symbols.put("out_string", new FeatureSymbol("out_string"));
        type.symbols.put("out_int", new FeatureSymbol("out_int"));
        type.symbols.put("in_string", new FeatureSymbol("in_string"));
        type.symbols.put("in_int", new FeatureSymbol("in_int"));
    }
}

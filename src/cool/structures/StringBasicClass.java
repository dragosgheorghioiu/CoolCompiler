package cool.structures;

public class StringBasicClass extends ObjectBasicClass{
    public StringBasicClass(Scope globalScope) {
        super(globalScope, "String");
        type.symbols.put("length", new FeatureSymbol("length"));
        type.symbols.put("concat", new FeatureSymbol("concat"));
        type.symbols.put("substr", new FeatureSymbol("substr"));
    }
}

package cool.structures;

import java.util.LinkedHashMap;
import java.util.Map;

public class ClassSymbol extends IdSymbol implements Scope{
    Map<String, FeatureSymbol> symbols = new LinkedHashMap<>();
    protected Scope parent;
    protected String parentClass;

    public ClassSymbol(Scope parent, String name) {
        super(name);
        this.parent = parent;
    }

    @Override
    public boolean add(Symbol sym) {
        if (symbols.containsKey(sym.getName()))
            return false;

        symbols.put(sym.getName(), (FeatureSymbol) sym);

        return true;
    }

    @Override
    public Symbol lookup(String s) {
        var sym = symbols.get(s);

        if (sym != null)
            return sym;

        if (parent != null)
            return parent.lookup(s);

        return null;
    }

    @Override
    public Scope getParent() {
        return parent;
    }
    public String getParentClass() {
        return parentClass;
    }
    public void setParentClass(String parentClass) {
        if (this.parentClass != null) {
            throw new RuntimeException("Parent class already set");
        }
        this.parentClass = parentClass;
    }
    public Map<String, FeatureSymbol> getFeatures() {
        return symbols;
    }
}
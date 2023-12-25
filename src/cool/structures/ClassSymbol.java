package cool.structures;

import java.util.LinkedHashMap;
import java.util.Map;

public class ClassSymbol extends IdSymbol implements Scope {
    Map<String, IdSymbol> attributeSymbols = new LinkedHashMap<>();
    Map<String, MethodSymbol> methodSymbols = new LinkedHashMap<>();
    protected Scope parent;
    protected String parentClass;

    public ClassSymbol(Scope parent, String name) {
        super(name);
        this.parent = parent;
    }

    @Override
    public boolean add(Symbol sym) {
        if (sym instanceof IdSymbol) {
            var id = (IdSymbol) sym;
            if (attributeSymbols.containsKey(id.getName())) {
                return false;
            }
            attributeSymbols.put(id.getName(), id);
            return true;
        }
        if (sym instanceof MethodSymbol) {
            var method = (MethodSymbol) sym;
            if (methodSymbols.containsKey(method.getName())) {
                return false;
            }
            methodSymbols.put(method.getName(), method);
            return true;
        }

        return true;
    }

    @Override
    public Symbol lookup(String s) {
        var sym = attributeSymbols.get(s);

        if (sym != null)
            return sym;

        var symMethod = methodSymbols.get(s);

        if (symMethod != null)
            return symMethod;

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

    public ClassSymbol getParentClassSymbol() {
        var parentSymbol = (IdSymbol) parent.lookup(parentClass);
        if (parentSymbol == null) {
            return null;
        }
        return parentSymbol.getType();
    }

    public void setParentClass(String parentClass) {
        if (this.parentClass != null) {
            throw new RuntimeException("Parent class already set");
        }
        this.parentClass = parentClass;
    }
    public Map<String, FeatureSymbol> getFeatures() {
        var features = new LinkedHashMap<String, FeatureSymbol>();
        for (var entry : attributeSymbols.entrySet()) {
            features.put(entry.getKey(), entry.getValue());
        }
        for (var entry : methodSymbols.entrySet()) {
            features.put(entry.getKey(), entry.getValue());
        }
        return features;
    }
}
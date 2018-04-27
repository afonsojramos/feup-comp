import java.util.LinkedHashMap;

public class SymbolTable {
    private LinkedHashMap<String, Symbol> parameters;
    private LinkedHashMap<String, Symbol> variables;
    private Symbol returnSymbol = null;

    public SymbolTable() {
        this.parameters = new LinkedHashMap<String, Symbol>();
        this.variables = new LinkedHashMap<String, Symbol>();
    }

    public LinkedHashMap<String, Symbol> getParameters() {
        return this.parameters;
    }

    public LinkedHashMap<String, Symbol> getVariables() {
        return this.variables;
    }

    public Symbol getReturnSymbol() {
        return this.returnSymbol;
    }

    public void setReturnSymbol(String name, String type, int register) {
        Symbol s = new Symbol(name, type, register);
        this.returnSymbol = s;
    }

    public boolean addParameter(String name, String type, int register) {

        if(this.parameters.containsKey(name)){
            return false;
        }
        else{
            Symbol s = new Symbol(name, type, register);
            this.parameters.put(name, s);
            return true;
        }
       
    }

    public boolean addVariable(String name, String type, int register) {

        if(this.variables.containsKey(name)){
            return false;
        }
        else{
            Symbol s = new Symbol(name, type, register);
            this.variables.put(name, s);
            return true;
        }
       

    }

    public String getVariableType (String name){
        Symbol s = this.variables.get(name);

        if (s != null)
            return s.getType();

        return null;
    }
}
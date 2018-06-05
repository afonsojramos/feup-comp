package AST;

import java.util.LinkedHashMap;
import java.util.Map;

public class SymbolTable implements Cloneable {
    private LinkedHashMap<String, Symbol> parameters;
    private LinkedHashMap<String, Symbol> variables;
    private Symbol returnSymbol = null;
    private boolean returned = false;
    private int lastRegister;
    private int stack = 1;
    private int locals = 0;
    private int loopCounter = -1;

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
        if (this.returnSymbol != null)
            return this.returnSymbol;
        return null;
    }

    public Boolean getReturned() {
        return this.returned;
    }

    public void setReturnSymbol(String name, String type) {
        Symbol s = new Symbol(name, type);
        this.returnSymbol = s;
    }

    public void setReturned(Boolean returned) {
        this.returned=returned;
    }

    public boolean addParameter(String name, String type) {

        if (this.parameters.containsKey(name)) {
            return false;
        } else {
            Symbol s = new Symbol(name, type, true);
            this.parameters.put(name, s);
            return true;
        }

    }

    public boolean addVariable(String name, String type) {

        if (this.variables.containsKey(name)) {
            return false;
        } else {
            Symbol s = new Symbol(name, type, true);
            this.variables.put(name, s);
            return true;
        }

    }

    public void removeVariable(String name) {

        if (!this.variables.containsKey(name)) {
            return;
        } else {
            System.out.println("REMOVED");
            this.variables.remove(name);
        }

    }

    public Symbol getFromAll(String name) {

        if (this.returnSymbol != null && name.equals(this.returnSymbol.getName()) && this.returnSymbol.getInit())
            return this.returnSymbol;
        else if (this.variables.get(name) != null)
            return this.variables.get(name);
        else if (this.parameters.get(name) != null)
            return this.parameters.get(name);
        else
            return null;
    }

    public String getAcessType(String name) {

        if (returnSymbol != null) {
            if (returnSymbol.getName().equals(name)) {
                return returnSymbol.getType();
            }
        }

        if (variables.containsKey(name)) {
            return variables.get(name).getType();
        }

        if (parameters.containsKey(name)) {
            return parameters.get(name).getType();
        }

        return "";
    }

    public void setRegisters(String functionName){
        int registerCounter;

        if(functionName.equals("main"))
            registerCounter = 1;
        else 
            registerCounter = 0;

        //System.out.println("FUNCTION: " + functionName);

        //System.out.println("PARAMETERS:");
        for (Map.Entry<String, Symbol> entry : this.parameters.entrySet()) {
            Symbol symbol = entry.getValue();
            symbol.setRegister(registerCounter);
            //System.out.println(symbol.getName() + " - " + symbol.getRegister());
            registerCounter ++;
        }

        //System.out.println("VARIABLES:");
        for (Map.Entry<String, Symbol> entry : this.variables.entrySet()) {
            Symbol symbol = entry.getValue();
            symbol.setRegister(registerCounter);
            //System.out.println(symbol.getName() + " - " + symbol.getRegister());
            registerCounter ++;
        }

        if(this.returnSymbol != null){
            //System.out.println("RETURN:");
            this.returnSymbol.setRegister(registerCounter);
            //System.out.println(this.returnSymbol.getName() + " - " + this.returnSymbol.getRegister());
            registerCounter ++;
            
        }

        this.lastRegister=registerCounter;

        this.locals = registerCounter;
           
    }

    public int getLastRegister() {
        return lastRegister;
    }

    public void setLastRegister(int lastRegister) {
        this.lastRegister = lastRegister;
    }

    public int getStack(){
        return stack;
    }

    public void setMaxStack(int stack){
        int stack_1 =  Math.max(1, stack);
        this.stack = Math.max(this.stack, stack_1);
    }

    public int getLocals() {
        return locals;
    }

    public void setLocals(int locals) {
        this.locals = locals;
    }

    public int getLoopCounter() {
        return loopCounter;
    }

    public void incLoopCounter() {
        this.loopCounter ++;
    }

    @SuppressWarnings("unchecked")
	@Override
    public SymbolTable clone() throws CloneNotSupportedException{
        SymbolTable newTable = (SymbolTable)super.clone();
        newTable.variables = (LinkedHashMap<String, Symbol>)this.variables.clone();
        return newTable;
    }
}

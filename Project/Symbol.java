public class Symbol {
    private String name = null;
    private String type;
    private int register; //if it is -1, means it's a global variable

    public Symbol(String name, String type) {
        this.name = name;
        this.type = type; //"array" or "int"
    }

    public String getName() {
        return this.name;
    }

    public String getType() {
        return this.type;
    }

    public int getRegister() {
        return this.register;
    }

    public void setRegister(int register) {
        this.register = register;
    }

    public boolean equals(Object symbol) {
        Symbol s = (Symbol) symbol;
        return this.name.equals(s.getName());
    }

    public int hashCode() {
        return name.hashCode();
    }
}
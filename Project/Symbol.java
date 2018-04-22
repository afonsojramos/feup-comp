public class Symbol {
    private String name;
    private String type;

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

    public boolean equals(Object symbol){
        Symbol s=(Symbol) symbol;
        return this.name.equals(s.getName());
    }

    public int hashCode(){
        return name.hashCode();
    }
}
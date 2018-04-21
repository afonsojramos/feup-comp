public class Symbol {
    private String name;
    private String type;
    private int size;

    public Symbol(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return this.name;
    }

    public String getType() {
        return this.type;
    }

    public int getSize() {
        return this.size;
    }

    public void setSize(int n) {
        this.size = n;
    }
}
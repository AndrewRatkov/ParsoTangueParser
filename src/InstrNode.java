package src;

public class InstrNode {
    public Expr type;
    public String var_name;
    public Node expression;

    public InstrNode(Expr _type, String _var_name, Node _expression) {
        this.type = _type;
        this.var_name = _var_name;
        this.expression = _expression;
    }
}

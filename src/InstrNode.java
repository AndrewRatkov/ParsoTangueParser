package src;

public class InstrNode {
    public Expr type;
    public String var_name;
    public ExprNode expression;

    public InstrNode(Expr _type, String _var_name, ExprNode _expression) {
        this.type = _type;
        this.var_name = _var_name;
        this.expression = _expression;
    }

    public String buildTree() {
        String res = "[DEF " + (type == Expr.IntExpr ? "int" : "str") + " " + var_name + "]--->";
        String white_string = " ".repeat(res.length());
        String[] node_tree = expression.buildTree().split("\n");
        res += node_tree[0] + '\n';
        for (int i = 1; i < node_tree.length; ++i) res += white_string + node_tree[i] + "\n";
        return res;
    }
}

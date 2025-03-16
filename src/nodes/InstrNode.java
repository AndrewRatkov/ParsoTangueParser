package src.nodes;

import src.consts.Expr;

public class InstrNode implements Node {
    public Expr type;
    public String var_name;
    public ExprNode expression;
    private String tree;

    public InstrNode(Expr _type, String _var_name, ExprNode _expression) {
        this.type = _type;
        this.var_name = _var_name;
        this.expression = _expression;
    }

    public void buildTree() {
        if (this.type == Expr.ErrorExpr) {
            this.tree = "[ERR " + var_name + "]\n";
            return;
        }
        String res = "[DEF " + (type == Expr.IntExpr ? "int" : "str") + " " + var_name + "]--->";
        String white_string = " ".repeat(res.length());
        String[] node_tree = expression.getTree().split("\n");
        res += node_tree[0] + '\n';
        for (int i = 1; i < node_tree.length; ++i) res += white_string + node_tree[i] + "\n";
        this.tree = res;
    }

    public String getTree() {
        if (this.tree == null) buildTree();
        return this.tree;
    }
}
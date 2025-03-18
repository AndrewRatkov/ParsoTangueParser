package src.nodes;

import src.consts.Expr;

public class AssnNode implements InstrNode {
    private Expr type;
    private String var_name;
    private ExprOrCallNode expression;
    private String tree;

    public Expr getType() {
        return this.type;
    }

    public ExprOrCallNode getExpression() {
        return this.expression;
    }

    public String getVarname() {
        return this.var_name;
    }

    public AssnNode(Expr _type, String _var_name, ExprOrCallNode _expression) {
        this.type = _type;
        this.var_name = _var_name;
        this.expression = _expression;
    }

    public void buildTree() {
        if (this.type == Expr.ErrorExpr) {
            this.tree = "[ERR " + var_name + "]\n";
            return;
        }
        this.tree = "[DEF " + (type == Expr.IntExpr ? "int" : "str") + " " + var_name + "]--->";
        String white_string = " ".repeat(this.tree.length());
        String[] node_tree = this.expression.getTree().split("\n");
        this.tree += node_tree[0] + '\n';
        for (int i = 1; i < node_tree.length; ++i) this.tree += white_string + node_tree[i] + "\n";
    }

    public String getTree() {
        if (this.tree == null) buildTree();
        return this.tree;
    }
}
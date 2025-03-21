package src.nodes;

import src.consts.FunctionReturnType;
import src.consts.Constants;

public class RetNode implements InstrNode {
    private ExprOrCallNode expression;
    private String tree;

    public RetNode(ExprOrCallNode _expression) {
        this.expression = _expression;
    }

    public ExprOrCallNode getExpression() {
        return this.expression;
    }

    public FunctionReturnType returnType() {
        if (expression instanceof FunNode) return ((FunNode)expression).getFunc().output_type;
        return Constants.get_function_return_type_from_expr(expression.getType());
    }
    
    public void buildTree() {
        if (expression == null) {
            this.tree = "[RETURN]\n";
            return;
        }
        this.tree = "[RETURN]--->";
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

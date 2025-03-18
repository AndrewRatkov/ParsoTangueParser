package src.nodes;

public class RetNode implements Node {
    private ExprOrCallNode expression;
    private String tree;

    public RetNode(ExprOrCallNode _expression) {
        this.expression = _expression;
    }
    
    public void buildTree() {
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

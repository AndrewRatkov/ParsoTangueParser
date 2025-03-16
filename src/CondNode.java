package src;

import java.util.List;

public class CondNode implements Node {
    private String tree;
    private ExprNode stmt;
    private List<Node> then_nodes;
    private List<Node> else_nodes;
    
    public CondNode(ExprNode _stmt, List<Node> _then_nodes, List<Node> _else_nodes) {
        this.stmt = _stmt;
        this.then_nodes = _then_nodes;
        this.else_nodes = _else_nodes;
    }

    public void buildTree() {
        String shift = "+--->";
        String skip = "|" + " ".repeat(shift.length() - 1);
        
        tree = "IF\n|\n" + shift;
        String[] stmt_tree = stmt.getTree().split("\n");
        tree += stmt_tree[0] + '\n';
        for (int i = 1; i < stmt_tree.length; ++i) {
            tree += skip + stmt_tree[i] + '\n';
        }

        tree += "|\nTHEN\n|\n";
        for (Node node : then_nodes) {
            String[] node_tree = node.getTree().split("\n");
            tree += shift + node_tree[0] + '\n';
            for (int i = 1; i < node_tree.length; ++i) {
                tree += skip + node_tree[i] + '\n';
            }
            tree += "|\n";
        }

        if (else_nodes != null) {
            tree += "ELSE\n|\n";
            for (Node node : else_nodes) {
                String[] node_tree = node.getTree().split("\n");
                tree += shift + node_tree[0] + '\n';
                for (int i = 1; i < node_tree.length; ++i) {
                    tree += skip + node_tree[i] + '\n';
                }
                tree += "|\n";
            }
        }

        tree += "FI\n";
    }

    public String getTree() {
        if (this.tree == null) buildTree();
        return this.tree;
    }

}

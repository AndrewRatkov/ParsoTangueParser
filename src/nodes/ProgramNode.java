package src.nodes;

import java.util.List;

public class ProgramNode implements Node {
    private String tree;
    private List<Node> nodes;

    public ProgramNode(List<Node> _nodes) {
        this.nodes = _nodes;
    }

    public void buildTree() {
        tree = "[PROGRAM]\n";
        String SHIFT1 = "  |     ";
        String SHIFT2 = "  +---->";
        String SHIFT3 = "        ";
        String SHIFT4 = "  |\n";

        for (int node_idx = 0; node_idx < nodes.size(); ++node_idx) {
            tree += SHIFT4 + SHIFT2;
            String[]strs = nodes.get(node_idx).getTree().split("\n");
            tree += strs[0] + '\n';
            for (int j = 1; j < strs.length; ++j) {
                if (node_idx < nodes.size() - 1) tree += SHIFT1;
                else tree += SHIFT3;
                tree += strs[j] + '\n';
            }
        }
    }

    public String getTree() {
        if (this.tree == null) buildTree();
        return this.tree;
    }
}

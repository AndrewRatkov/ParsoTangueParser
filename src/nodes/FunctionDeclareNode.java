package src.nodes;

import java.util.List;

import src.consts.FunctionInfo;

public class FunctionDeclareNode implements Node {
    private FunctionInfo fun_info;
    private List<Node> instructions;
    private List<String> locals;
    private String tree;
    private boolean valid = true;
    private String message;

    public FunctionDeclareNode(FunctionInfo _fun_info, List<Node> _instructions, List<String> _locals) {
        this.fun_info = _fun_info;
        this.instructions = _instructions;
        this.locals = _locals;
        assert locals.size() == fun_info.input_types.size();
    }

    public FunctionDeclareNode(String _message) {
        this.valid = false;
        this.message = _message;
    }

    public void buildTree() {
        if (!valid) {
            tree = "INVALID FUN DECLARATION: " + message + '\n';
            return;
        }
        tree = "[DEF FUN " + fun_info.name + "(";
        for (int i = 0; i < locals.size(); ++i) {
            tree += fun_info.input_types.get(i).toString() + ' ' + locals.get(i);
            if (i != locals.size() - 1) tree += ", ";
        }
        tree += ")=>" + fun_info.output_type + "]\n";

        String SHIFT1 = "|   \n";
        String SHIFT2 = "+-->";
        String SHIFT3 = "|   ";
        String SHIFT4 = "    ";

        for (int i = 0; i < this.instructions.size(); ++i) {
            String[] n_tree_strings = instructions.get(i).getTree().split("\n");
            tree += SHIFT1 + SHIFT2 + n_tree_strings[0] + '\n';
            for (int j = 1; j < n_tree_strings.length; ++j) {
                if (i == this.instructions.size() - 1) tree += SHIFT4;
                else tree += SHIFT3;
                tree += n_tree_strings[j] + '\n';
            }
        }
    }

    public String getTree() {
        if (this.tree == null) buildTree();
        return this.tree;
    }
}

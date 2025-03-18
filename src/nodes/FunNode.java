package src.nodes;

import java.util.List;

import src.consts.Constants;
import src.consts.Expr;
import src.consts.FunctionInfo;
import src.consts.FunctionReturnType;

public class FunNode implements ExprOrCallNode {
    private FunctionInfo func;
    private List<ExprOrCallNode> params;
    private String tree;
    private boolean valid;
    private String message;

    public FunctionInfo getFunc() {
        return this.func;
    }

    public FunNode(String error) {
        valid = false;
        message = error;
    }

    public Expr getType() {
        switch (func.output_type) {
            case FunctionReturnType.INT:
                return Expr.IntExpr;
            case FunctionReturnType.STR:
                return Expr.StringExpr;
            case FunctionReturnType.VOID:
                return Expr.ErrorExpr;
            default:
                return Expr.ErrorExpr;
        }
    }

    public FunNode(FunctionInfo _func, List<ExprOrCallNode> _params) {
        func = _func;
        params = _params;
        valid = true;
        if (func.input_types.size() != params.size()) {
            valid = false;
            message = "incorrect number of parameters: expected " + func.input_types.size() + ",  got " + params.size();
            return;
        }
        for (int i = 0; i < params.size(); ++i) {
            Expr expected = Constants.get_expr_from_type(func.input_types.get(i));
            Node got = params.get(i);
            if (got instanceof ExprNode) {
                Expr expr_got = ((ExprNode)got).getType();
                if (expr_got != expected) {
                    valid = false;
                    message = "incorrect argument " + i + ": expected " + expected + ", got " + expr_got;
                    break;
                }
            } else if (got instanceof FunNode) {
                FunctionReturnType ret =((FunNode)got).func.output_type; 
                if (ret == FunctionReturnType.VOID) {
                    valid = false;
                    message = "cannot use VOID as a parameter";
                    break;
                } else if (Constants.get_expr_from_function_return_type(ret) != expected) {
                    valid = false;
                    message = "incorrect argument " + i + ": expected " + expected + ", got " + Constants.get_expr_from_function_return_type(ret);
                    break;
                }
            }
        }
    }

    public void buildTree() {
        if (func != null) tree = "[CALL " + func.output_type + ' ' + func.name + "]";
        else tree = "[ERROR FUNCTION NOT DEFINED]";
        String GO_LEFT = "------->";
        if (!valid) {
            tree += GO_LEFT + "{" + message + "}\n";
            return;
        }
        if (params.size() == 0) {
            tree += '\n'; return;
        }

        int ROOT_LENGTH = tree.length();
        String SHIFT1 = " ".repeat(ROOT_LENGTH / 2) + '|' + " ".repeat(GO_LEFT.length() + ROOT_LENGTH - ROOT_LENGTH / 2 - 1);
        String SHIFT2 = " ".repeat(ROOT_LENGTH + GO_LEFT.length());
        String SHIFT3 = " ".repeat(ROOT_LENGTH / 2) + '+' + "-".repeat(ROOT_LENGTH - ROOT_LENGTH / 2 - 1) + GO_LEFT;
        String GO_NEXT = " ".repeat(ROOT_LENGTH / 2) + "|\n";

        for (int idx_param = 0; idx_param < params.size(); ++idx_param) {
            String[] param_tree = params.get(idx_param).getTree().split("\n");
            if (idx_param == 0) tree += GO_LEFT;
            else {
                tree += GO_NEXT;
                tree += SHIFT3;
            }
            tree += param_tree[0] + '\n';
            for (int i = 1; i < param_tree.length; ++i) {
                if (idx_param < params.size() - 1) tree += SHIFT1;
                else tree += SHIFT2;
                tree += param_tree[i] + '\n';
            }
        }
    }

    public String getTree() {
        if (tree == null) buildTree();
        return tree;
    }
}

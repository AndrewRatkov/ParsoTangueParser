package src.nodes;

import src.consts.Binop;
import src.consts.BinopConstants;
import src.consts.Constants;
import src.consts.Expr;
import src.consts.FunctionReturnType;

/*
 * Вершина в дереве разбора выражения (expression).
 * Имеет тип type, который показывает, что в
 * соответствующем поддереве задана строка,
 * целое число, или что-то ошибочное.
 * (например, если в поддереве написано
 * "123" + 456, то это ошибка, так как нельзя
 * складывать числа со строками)
 */

public class ExprNode implements Node {
    public Expr type;
    public Node left;
    public Node right;
    public Binop binop;
    public String value;
    private String tree;


    public ExprNode(Node left, Node right, Binop binop) {
        this.value = null;
        Expr left_type;
        if (left instanceof ExprNode) {
            left_type = ((ExprNode)left).type;
        } else if (left instanceof FunNode && ((FunNode)left).func.output_type != FunctionReturnType.VOID) {
            left_type = Constants.get_expr_from_function_return_type(((FunNode)left).func.output_type);
        } else {
            type = Expr.ErrorExpr;
            value = "Cannot apply binop not to non-void functions or other expressions";
            return;
        }

        Expr right_type;
        if (right instanceof ExprNode) {
            right_type = ((ExprNode)right).type;
        } else if (right instanceof FunNode && ((FunNode)right).func.output_type != FunctionReturnType.VOID) {
            right_type = Constants.get_expr_from_function_return_type(((FunNode)right).func.output_type);
        } else {
            type = Expr.ErrorExpr;
            value = "Cannot apply binop not to non-void functions or other expressions";
            return;
        }


        this.left = left;
        this.right = right;
        this.binop = binop;
        this.tree  = null;
        if (left_type == Expr.ErrorExpr || right_type == Expr.ErrorExpr) {
            this.type = Expr.ErrorExpr;
        } else if (left_type == right_type && binop.priority == 0) { // == or != is ok for both strings and integers
            this.type = Expr.IntExpr;
        } else if (left_type == Expr.IntExpr && right_type == Expr.IntExpr) {
            this.type = Expr.IntExpr;
        } else if (left_type == Expr.StringExpr && right_type == Expr.StringExpr) {
            if (binop == BinopConstants.ADD) this.type = Expr.StringExpr; // can only add strings
            else this.type = Expr.ErrorExpr;
        } else {
            if (binop == BinopConstants.MUL) this.type = Expr.StringExpr; // can only multiply a string by an integer
            else this.type = Expr.ErrorExpr;
        }
    }

    public ExprNode(Expr type, String value) {
        this.type = type;
        this.value = value;
        this.left = null;
        this.right = null;
        this.binop = null;
        this.tree = null;
    }

    private String getStr() { // 8-char string representation of Node (GET_STR_LENGTH = 8)
        String res;
        if (this.type == Expr.IntExpr) res = "[INT,";
        else if (this.type == Expr.StringExpr) res = "[STR,";
        else res = "[ERR,";

        if (this.binop != null) {
            res += String.valueOf(this.binop.operator);
            if (res.length() == 6) res += " ";
        } else {
            res += this.value;
        }
        
        res += "]";
        return res;
    }
    
    private static int GET_STR_LENGTH = 8;
    
    private boolean isLeaf() {
        return this.left == null;
    }

    public void buildTree() { // returns a string-formatted tree (see examples) 
        if (this.isLeaf()) {
            this.tree = getStr() + '\n';
            return;
        }        
        String GO_LEFT = "------->";
        String SHIFT1 = " ".repeat(GET_STR_LENGTH / 2) + '|' + " ".repeat(GO_LEFT.length() + GET_STR_LENGTH - GET_STR_LENGTH / 2 - 1);

        String[] left_node_strings = this.left.getTree().split("\n");
        String[] right_node_strings = this.right.getTree().split("\n");
        this.tree = getStr() + GO_LEFT + left_node_strings[0] + '\n';
        for (int i = 1; i < left_node_strings.length; ++i) {
            this.tree += SHIFT1 + left_node_strings[i] + '\n';
        }
        this.tree += " ".repeat(GET_STR_LENGTH / 2) + "|\n";
        this.tree += " ".repeat(GET_STR_LENGTH / 2) + '+' + "-".repeat(GET_STR_LENGTH - GET_STR_LENGTH / 2 - 1) + GO_LEFT;
        this.tree += right_node_strings[0] + '\n';
        for (int i = 1; i < right_node_strings.length; ++i) {
            this.tree += " ".repeat(GET_STR_LENGTH + GO_LEFT.length()) + right_node_strings[i] + '\n';
        }
    }

    public String getTree() {
        if (this.tree == null) buildTree();
        return this.tree;
    }
    
}

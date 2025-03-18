package src.nodes;

import src.consts.Binop;
import src.consts.BinopConstants;
import src.consts.Expr;

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
    public ExprNode left;
    public ExprNode right;
    public Binop binop;
    public String value;
    private String tree;


    public ExprNode(ExprNode left, ExprNode right, Binop binop) {
        this.value = null;
        this.left = left;
        this.right = right;
        this.binop = binop;
        this.tree  = null;
        if (left.type == Expr.ErrorExpr || right.type == Expr.ErrorExpr) {
            this.type = Expr.ErrorExpr;
        } else if (left.type == right.type && binop.priority == 0) { // == or != is ok for both strings and integers
            this.type = Expr.IntExpr;
        } else if (left.type == Expr.IntExpr && right.type == Expr.IntExpr) {
            this.type = Expr.IntExpr;
        } else if (left.type == Expr.StringExpr && right.type == Expr.StringExpr) {
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

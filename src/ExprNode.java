package src;

import java.util.HashMap;
import java.util.Stack;

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
    
    private enum Move{ // used in buildTree method in tree graph traversal by dfs
        LEFT,
        RIGHT
    }

    private boolean isLeaf() {
        return this.left == null;
    }

    public void buildTree() { // returns a string-formatted tree (see examples) 
        if (this.isLeaf()) {
            this.tree = getStr() + '\n';
            return;
        }
        // for dfs:
        Stack<Move> moves = new Stack<>();
        Stack<ExprNode> cur_path_nodes = new Stack<>();
        cur_path_nodes.push(this);
        moves.add(Move.LEFT);

        // for painting nodes I'll use (x, y) coordinates on R^2 for each of them
        HashMap<ExprNode, Integer > heights = new HashMap<>();
        HashMap<ExprNode, Integer > shifts = new HashMap<>();
        heights.put(this, 0);
        shifts.put(this, 0);
        
        int cur_height = 0;
        Stack<ExprNode> nodes_by_depth = new Stack<>(); // put here the nodes in such a way that parent nodes are topper [nodes are "sorted" by depth]
        
        while (!moves.empty()) {
            Move last_move = moves.getLast();
            ExprNode cur_node, prev_node = cur_path_nodes.peek();
            if (last_move == Move.LEFT) cur_node = prev_node.left;
            else {
                cur_node = prev_node.right;
                ++cur_height;
            }
            heights.put(cur_node, cur_height);
            shifts.put(cur_node, shifts.get(prev_node) + 1);
            if (cur_node.isLeaf()) {
                nodes_by_depth.push(cur_node);
                while (!moves.empty() && moves.peek() == Move.RIGHT) {
                    moves.pop();
                    nodes_by_depth.push(cur_path_nodes.pop());
                }
                if (!moves.empty()) {
                    moves.pop();
                    moves.push(Move.RIGHT);
                }
            } else {
                cur_path_nodes.push(cur_node);
                moves.push(Move.LEFT);
            }
        }
        ++cur_height;
        
        String[] strings = new String[cur_height];
        for (int i = 0; i < cur_height; ++i) strings[i] = "";

        String GO_LEFT = "------->";

        while (!nodes_by_depth.empty()) {
            ExprNode painting_node = nodes_by_depth.pop();
            strings[heights.get(painting_node)] += painting_node.getStr();
            if (!painting_node.isLeaf()) {
                strings[heights.get(painting_node)] += GO_LEFT;
                ExprNode right_node = painting_node.right;
                int h = heights.get(painting_node) + 1;
                int cur_shift = shifts.get(painting_node) * (GET_STR_LENGTH + GO_LEFT.length()) + GET_STR_LENGTH / 2;
                while (h < heights.get(right_node)) {
                    while (strings[h].length() < cur_shift) strings[h] += ' ';
                    strings[h] += '|'; 
                    ++h;
                }
                while (strings[h].length() < cur_shift) strings[h] += ' ';
                strings[h] += '+'; 
                while (strings[h].length() < 16 * shifts.get(right_node) - 1) strings[h] += '-';
                strings[h] += '>';
            }
        }

        String res = strings[0] + '\n';
        for (int i = 1; i < strings.length; ++i) {
            String between = "";
            for (char c : strings[i].toCharArray()) {
                if (c == '+') {
                    between += "|\n";
                    break;
                } else between += c;
            }
            res += between + strings[i] + '\n';
        }
        this.tree = res;
    }

    public String getTree() {
        if (this.tree == null) buildTree();
        return this.tree;
    }
    
}

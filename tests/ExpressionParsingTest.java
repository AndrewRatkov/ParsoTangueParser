package tests;


import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


import src.Parser;
import src.consts.Binop;
import src.consts.BinopConstants;
import src.consts.Expr;
import src.consts.ExprReaderResponses;
import src.consts.Types;
import src.nodes.ExprNode;



public class ExpressionParsingTest {
    @Test
    void testRead() {
        Parser p = new Parser();

        assertTrue(p.read("123".toCharArray(), 0).isSame(ExprReaderResponses.READ_INTEGER, 3));
        assertTrue(p.read("12 3".toCharArray(), 0).isSame(ExprReaderResponses.READ_INTEGER, 2));
        assertTrue(p.read("1+2==3".toCharArray(), 2).isSame(ExprReaderResponses.READ_INTEGER, 3));
        assertTrue(p.read("1+2-303030==3".toCharArray(), 4).isSame(ExprReaderResponses.READ_INTEGER, 10));

        assertTrue(p.read("12 3".toCharArray(), 2).isSame(ExprReaderResponses.SKIPPED, 3));
        assertTrue(p.read("12  6".toCharArray(), 2).isSame(ExprReaderResponses.SKIPPED, 4));
        assertTrue(p.read("12   6".toCharArray(), 2).isSame(ExprReaderResponses.SKIPPED, 5));
        assertTrue(p.read("12   6".toCharArray(), 3).isSame(ExprReaderResponses.SKIPPED, 5));

        assertTrue(p.read("dewdewd".toCharArray(), 7).isSame(ExprReaderResponses.FINISHED_READING, -1));
        assertTrue(p.read("1+2=3".toCharArray(), 10).isSame(ExprReaderResponses.FINISHED_READING, -1));
        
        assertTrue(p.read("1+(2==3)==1".toCharArray(), 2).isSame(ExprReaderResponses.OPENNING_BRACKET, 3));
        assertTrue(p.read("1+((2)==3)==1".toCharArray(), 2).isSame(ExprReaderResponses.OPENNING_BRACKET, 3));

        assertTrue(p.read("1+(2==3)==1".toCharArray(), 7).isSame(ExprReaderResponses.CLOSING_BRACKET, 8));
        assertTrue(p.read("1+((2)==(3))==1".toCharArray(), 10).isSame(ExprReaderResponses.CLOSING_BRACKET, 11));

        assertTrue(p.read("43?".toCharArray(), 2).isSame(ExprReaderResponses.ERROR_UNKNOWN_CHAR, 2));
        assertTrue(p.read("4@78".toCharArray(), 1).isSame(ExprReaderResponses.ERROR_UNKNOWN_CHAR, 1));

        assertTrue(p.read("2 + a2hgf == 4".toCharArray(), 4).isSame(ExprReaderResponses.READ_VARIABLE, 9));
        assertTrue(p.read("(bhdeb - abcdef12345ghi)".toCharArray(), 9).isSame(ExprReaderResponses.READ_VARIABLE,23));

        assertTrue(p.read("2 + ab3def(3+3)".toCharArray(), 4).isSame(ExprReaderResponses.ERROR_AFTER_VARIABLE, 10));
        assertTrue(p.read("2 + ab3def~3 + 3".toCharArray(), 4).isSame(ExprReaderResponses.ERROR_AFTER_VARIABLE, 10));

        assertTrue(p.read("9 * \"cucu\"".toCharArray(), 4).isSame(ExprReaderResponses.READ_STRING, 10));
        assertTrue(p.read("9 * \"cucu\" + \"meme\"".toCharArray(), 4).isSame(ExprReaderResponses.READ_STRING, 10));

        assertTrue(p.read("9 * \"abracadabra".toCharArray(), 4).isSame(ExprReaderResponses.ERROR_STRING_DOESNT_END, 16));

        assertTrue(p.read("9cucu + 4 == 13".toCharArray(), 0).isSame(ExprReaderResponses.ERROR_VAR_STARTS_WITH_DIGITS, 1));
        assertTrue(p.read("1 + 2e0 == 3".toCharArray(), 4).isSame(ExprReaderResponses.ERROR_VAR_STARTS_WITH_DIGITS, 5));

        assertTrue(p.read("10%3==1".toCharArray(), 2).isSame(ExprReaderResponses.READ_BINOP, 3));
        assertTrue(p.read("10+3==1".toCharArray(), 2).isSame(ExprReaderResponses.READ_BINOP, 3));
        assertTrue(p.read("10/3==1".toCharArray(), 2).isSame(ExprReaderResponses.READ_BINOP, 3));
        assertTrue(p.read("10==3==1".toCharArray(), 2).isSame(ExprReaderResponses.READ_BINOP, 4));
        assertTrue(p.read("10>=3==1".toCharArray(), 2).isSame(ExprReaderResponses.READ_BINOP, 4));
        assertTrue(p.read("10!=3==1".toCharArray(), 2).isSame(ExprReaderResponses.READ_BINOP, 4));
        
        assertTrue(p.read("10=!3==1".toCharArray(), 2).isSame(ExprReaderResponses.ERROR_UNKNOWN_CHAR, 3));
        assertTrue(p.read("10!3==1".toCharArray(), 2).isSame(ExprReaderResponses.ERROR_UNKNOWN_CHAR, 3));
    }

    private boolean goToChild(ExprNode node, String path, String expected_val) {
        /*for example, path="LLRLRRL" */
        ExprNode cur_node = node;
        for (char c : path.toCharArray()) {
            if (c == 'L') {
                if (cur_node.left == null) return false;
                cur_node = cur_node.left;
            } else if (c == 'R') {
                if (cur_node.right == null) return false;
                cur_node = cur_node.right;
            } else {
                return false;
            }
        }
        return cur_node.value.equals(expected_val);
    }

    private boolean checkBinop(ExprNode node, String path, Binop expected_binop) {
        /* for example, path="LLRLRRL" */

        ExprNode cur_node = node;
        for (char c : path.toCharArray()) {
            if (c == 'L') {
                if (cur_node.left == null) return false;
                cur_node = cur_node.left;
            } else if (c == 'R') {
                if (cur_node.right == null) return false;
                cur_node = cur_node.right;
            } else {
                return false;
            }
        }

        return cur_node.binop == expected_binop;
    }

    private boolean checkStructure(ExprNode node, Map<String, String> expected_vals, Map<String, Binop>expected_binops) {
        for (String path : expected_vals.keySet()) {
            if (!goToChild(node, path, expected_vals.get(path))) return false;
        }
        for (String path : expected_binops.keySet()) {
            if (!checkBinop(node, path, expected_binops.get(path))) return false;
        }
        return true;
    }

    @Test
    void testParsePrimaryExpr1() {
        Stack<ExprNode> st_nodes = new Stack<>();
        st_nodes.push(new ExprNode(Expr.IntExpr, "2"));
        st_nodes.push(new ExprNode(Expr.IntExpr, "3"));
        st_nodes.push(new ExprNode(Expr.IntExpr, "5"));
        st_nodes.push(new ExprNode(Expr.IntExpr, "4"));

        Stack<Binop> st_binops = new Stack<>();
        st_binops.push(BinopConstants.ADD);
        st_binops.push(BinopConstants.MUL);
        st_binops.push(BinopConstants.EQ);
        /*  2 + 3 * 5 == 4
            ^ ^ ^ ^ ^ ^  ^
            | | +-+-+ |  |
            | |   |   |  |
            +-+---+   |  |
              |       |  |
              +-------+--+
                      |
        */
        Parser p = new Parser();
        ExprNode root = p.parsePrimaryExpr(st_nodes, st_binops, st_nodes.size());

        Map<String, String> expected_vals = Map.of(
            "LL", "2",
            "LRL", "3",
            "LRR", "5",
            "R", "4"
        );
        Map<String, Binop> expected_binops = Map.of(
            "", BinopConstants.EQ,
            "L", BinopConstants.ADD,
            "LR", BinopConstants.MUL
        );

        assertTrue(checkStructure(root, expected_vals, expected_binops));
    }

    @Test
    void testParsePrimaryExpr2() {
        Stack<ExprNode> st_nodes = new Stack<>();
        st_nodes.push(new ExprNode(Expr.IntExpr, "2"));
        st_nodes.push(new ExprNode(Expr.IntExpr, "2"));
        st_nodes.push(new ExprNode(Expr.IntExpr, "2"));
        st_nodes.push(new ExprNode(Expr.IntExpr, "5"));

        Stack<Binop> st_binops = new Stack<>();
        st_binops.push(BinopConstants.DIV);
        st_binops.push(BinopConstants.PER);
        st_binops.push(BinopConstants.LE);
        /*  2 / 2 % 2 <= 5
            ^ ^ ^ ^ ^ ^  ^
            +-+-+ + + |  |
              |   | | |  |
              +---+-+ |  |
                  |   |  |
                  +---+--+
                      |
        */
        Parser p = new Parser();
        ExprNode root = p.parsePrimaryExpr(st_nodes, st_binops, st_nodes.size());

        Map<String, String> expected_vals = Map.of(
            "LLL", "2",
            "LLR", "2",
            "LR", "2",
            "R", "5"
        );
        Map<String, Binop> expected_binops = Map.of(
            "", BinopConstants.LE,
            "L", BinopConstants.PER,
            "LL", BinopConstants.DIV
        );

        assertTrue(checkStructure(root, expected_vals, expected_binops));
    }

    @Test
    void testParseLongPrimaryExpr() {
        Stack<ExprNode> st_nodes = new Stack<>();
        int LEN = 10000;
        for (int i = 0; i < LEN; ++i) {
            st_nodes.push(new ExprNode(Expr.IntExpr, String.valueOf(i)));
        }
        Stack<Binop> st_binops = new Stack<>();
        for (int i = 0; i < LEN - 1; ++i) {
            st_binops.push(BinopConstants.SUB);
        }

        Parser p = new Parser();
        ExprNode root = p.parsePrimaryExpr(st_nodes, st_binops, LEN);
        
        String s = "";
        for (int i = 0; i < LEN - 1; ++i) {
            assertTrue(checkBinop(root, s, BinopConstants.SUB));
            assertTrue(goToChild(root, s + "R", String.valueOf(LEN - 1 - i)));
            s += "L";
        }
        assertTrue(goToChild(root, s, String.valueOf(0)));
    }

    @ParameterizedTest
    @CsvSource({
        "(1==2)< 3",
        "(1==2)<3",
        "( (1) ==   ((2))) < (3)   ",
        "(1 == 2) < 3"
    })
    void testParseExpr(String expr) {
        Parser p = new Parser();
        ExprNode root = p.parseExpr(expr);
        assertTrue(checkBinop(root, "", BinopConstants.L));
        assertTrue(checkBinop(root, "L", BinopConstants.EQ));
        assertTrue(goToChild(root, "R", "3"));
        assertTrue(goToChild(root, "LR", "2"));
        assertTrue(goToChild(root, "LL", "1"));
    }

    @ParameterizedTest
    @CsvSource({
        "(1==2)< \"3\"",
        "(1==2)<\"3\"",
        "( (1) ==   ((2))) < (\"3\")   ",
        "(1 == 2) < \"3\""
    })
    void testParseExpr2(String expr) {
        Parser p = new Parser();
        ExprNode root = p.parseExpr(expr);
        assertTrue(root.type == Expr.ErrorExpr);
        assertTrue(checkBinop(root, "", BinopConstants.L));
        assertTrue(checkBinop(root, "L", BinopConstants.EQ));
        assertTrue(root.right.type == Expr.StringExpr);
        assertTrue(goToChild(root, "R", "\"3\""));
        assertTrue(root.left.type == Expr.IntExpr);
        assertTrue(goToChild(root, "LR", "2"));
        assertTrue(goToChild(root, "LL", "1"));
    }

    @Test
    void testParseExprWithVariables() {

        HashMap<String, Types> vars_before = new HashMap<String, Types>(){{
            put("x", Types.INTEGER);
            put("y", Types.INTEGER);
            put("z", Types.STRING);
            put("t", Types.STRING);
        }};
        Parser p = new Parser(vars_before);

        assertTrue(p.parseExpr("(x+y)>=(z==t)").type == Expr.IntExpr);
        assertTrue(p.parseExpr("x*z+30*t==z*(t==t)").type == Expr.IntExpr);
        assertTrue(p.parseExpr("60*t+(3>2)*z").type == Expr.StringExpr);
        assertTrue(p.parseExpr("60*t==(3>2)*(z!=z)").type == Expr.ErrorExpr);
    }

    @Test
    void testParseEmptyString() {
        Parser p = new Parser();
        ExprNode n = p.parseExpr("");
        assertEquals(n.type, Expr.ErrorExpr);
    }
}

package tests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.assertEquals;

import src.ExprNode;
import src.Expr;
import src.BinopConstants;
import src.Binop;


public class ExprNodeTest {
    @Test
    void defaultConstructorTest() {
        ExprNode n = new ExprNode(Expr.StringExpr, "hrundelair");
        assertEquals(n.type, Expr.StringExpr);
        assertEquals(n.value, "hrundelair");
        assertEquals(n.left, null);
        assertEquals(n.right, null);
        assertEquals(n.binop, null);
    }

    @Test
    void fromTwoConstructorTest() {
        ExprNode a = new ExprNode(Expr.IntExpr, "30");
        ExprNode b = new ExprNode(Expr.IntExpr, "12345");
        ExprNode parent = new ExprNode(a, b, BinopConstants.ADD);

        assertEquals(parent.type, Expr.IntExpr);
        assertEquals(parent.left, a);
        assertEquals(parent.right, b);
        assertEquals(parent.binop, BinopConstants.ADD);
        assertEquals(parent.value, null);
        assertEquals(parent.left.value, "30");
    }

    @ParameterizedTest
    @CsvSource({
        "s,s,+,s",
        "s,s,*,e",
        "s,i,*,s",
        "i,s,*,s",
        "s,e,-,e",
        "i,i,==,i",
        "i,s,+,e",
        "i,i,%,i",
        "s,s,==,i",
        "s,s,>,e",
        "s,s,!=,i",
        "i,i,>,i"
    })
    void fromTwoConstructorTest2(String atype, String btype, String op, String etype) {
        Expr a_expr = Expr.ErrorExpr;
        if (atype.equals("s")) a_expr = Expr.StringExpr;
        else if (atype.equals("i")) a_expr = Expr.IntExpr;

        Expr b_expr = Expr.ErrorExpr;
        if (btype.equals("s")) b_expr = Expr.StringExpr;
        else if (btype.equals("i")) b_expr = Expr.IntExpr;

        Expr expected_expr = Expr.ErrorExpr;
        if (etype.equals("s")) expected_expr = Expr.StringExpr;
        else if (etype.equals("i")) expected_expr = Expr.IntExpr;

        Binop binop = BinopConstants.get_by_string(op);
        ExprNode a = new ExprNode(a_expr, "");
        ExprNode b = new ExprNode(b_expr, "");
        ExprNode c = new ExprNode(a, b, binop);

        assertEquals(c.type, expected_expr);
        assertEquals(c.left, a);
        assertEquals(c.right, b);
        assertEquals(c.binop, binop);
    }
}

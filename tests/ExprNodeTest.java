package tests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.assertEquals;

import src.consts.Binop;
import src.consts.BinopConstants;
import src.consts.Expr;
import src.nodes.ExprNode;


public class ExprNodeTest {
    @Test
    void defaultConstructorTest() {
        ExprNode n = new ExprNode(Expr.StringExpr, "hrundelair");
        assertEquals(n.getType(), Expr.StringExpr);
        assertEquals(n.getValue(), "hrundelair");
        assertEquals(n.getLeft(), null);
        assertEquals(n.getRight(), null);
        assertEquals(n.getBinop(), null);
    }

    @Test
    void fromTwoConstructorTest() {
        ExprNode a = new ExprNode(Expr.IntExpr, "30");
        ExprNode b = new ExprNode(Expr.IntExpr, "12345");
        ExprNode parent = new ExprNode(a, b, BinopConstants.ADD);

        assertEquals(parent.getType(), Expr.IntExpr);
        assertEquals(parent.getLeft(), a);
        assertEquals(parent.getRight(), b);
        assertEquals(parent.getBinop(), BinopConstants.ADD);
        assertEquals(parent.getValue(), null);
        assertEquals(((ExprNode)parent.getLeft()).getValue(), "30");
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

        assertEquals(c.getType(), expected_expr);
        assertEquals(c.getLeft(), a);
        assertEquals(c.getRight(), b);
        assertEquals(c.getBinop(), binop);
    }
}

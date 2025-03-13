package tests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.assertEquals;

import src.Node;
import src.Expr;
import src.BinopConstants;
import src.Binop;


public class NodeTest {
    @Test
    void defaultConstructorTest() {
        Node n = new Node(Expr.StringExpr, "hrundelair");
        assertEquals(n.type, Expr.StringExpr);
        assertEquals(n.value, "hrundelair");
        assertEquals(n.left, null);
        assertEquals(n.right, null);
        assertEquals(n.binop, null);
    }

    @Test
    void fromTwoConstructorTest() {
        Node a = new Node(Expr.IntExpr, "30");
        Node b = new Node(Expr.IntExpr, "12345");
        Node parent = new Node(a, b, BinopConstants.ADD);

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
    void FromTwoConstructorTest2(String atype, String btype, String op, String etype) {
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
        Node a = new Node(a_expr, "");
        Node b = new Node(b_expr, "");
        Node c = new Node(a, b, binop);

        assertEquals(c.type, expected_expr);
        assertEquals(c.left, a);
        assertEquals(c.right, b);
        assertEquals(c.binop, binop);
    } 
}

package tests.node_tree_tests;

import java.util.Arrays;
import java.util.HashMap;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import src.Main;
import src.Parser;
import src.consts.BinopConstants;
import src.consts.FunctionInfo;
import src.consts.FunctionReturnType;
import src.consts.Type;
import src.nodes.ExprNode;
import src.nodes.ExprOrCallNode;
import src.nodes.FunNode;
import src.nodes.RetNode;


public class TreeBuildTests {
    private String PATH = "tests/node_tree_tests/";
    
    @Test
    void retNodeTreeTest() {
        HashMap<String, Type> hm = new HashMap<>(){{put("x", Type.INTEGER);}};
        Parser p = new Parser(hm);
        ExprOrCallNode expr_node = p.parseExpr("x+2-x");
        RetNode n = new RetNode(expr_node);
        assertEquals(n.getTree(), Main.get_str_from_file(PATH + "ret_node_test.txt"));
    }

    @Test
    void funNodeTest1() {
        HashMap<String, Type> hm = new HashMap<>(){{put("x", Type.INTEGER);}};
        Parser p = new Parser(hm);
        ExprOrCallNode expr_node1 = p.parseExpr("x+2-x");
        ExprOrCallNode expr_node2 = p.parseExpr("\"a\"*30");
        ExprOrCallNode expr_node3 = p.parseExpr("1");
        FunctionInfo function = new FunctionInfo("myFunction",
                                                 Arrays.asList(Type.INTEGER, Type.STRING, Type.INTEGER),
                                                 FunctionReturnType.VOID);
        FunNode fun_node = new FunNode(function, Arrays.asList(expr_node1, expr_node2, expr_node3));
        assertEquals(fun_node.getTree(), Main.get_str_from_file(PATH + "fun_node_test1.txt"));
    }

    @Test
    void funNodeTest2() {
        HashMap<String, Type> hm = new HashMap<>(){{put("x", Type.INTEGER);}};
        Parser p = new Parser(hm);
        ExprOrCallNode expr_node1 = p.parseExpr("x-x");
        ExprOrCallNode expr_node2 = p.parseExpr("\"a\"*30");
        FunctionInfo function = new FunctionInfo("aaa", Arrays.asList(Type.INTEGER, Type.STRING), FunctionReturnType.STR);
        FunNode fun_node = new FunNode(function, Arrays.asList(expr_node1, expr_node2));
        assertEquals(fun_node.getTree(), Main.get_str_from_file(PATH + "fun_node_test2.txt"));
    }

    @Test
    void funNodeTest3() {
        Parser p = new Parser();
        ExprOrCallNode expr_node1 = p.parseExpr("\"a\"*30");
        FunctionInfo function = new FunctionInfo("f", Arrays.asList(Type.STRING), FunctionReturnType.INT);
        FunNode fun_node = new FunNode(function, Arrays.asList(expr_node1));
        assertEquals(fun_node.getTree(), Main.get_str_from_file(PATH + "fun_node_test3.txt"));
    }

    @Test
    void funNodeTypeMismatchingTest() {
        Parser p = new Parser();
        ExprOrCallNode expr_node1 = p.parseExpr("\"a\"*30");
        FunctionInfo function = new FunctionInfo("func", Arrays.asList(Type.INTEGER), FunctionReturnType.INT);
        FunNode fun_node = new FunNode(function, Arrays.asList(expr_node1));
        assertEquals(fun_node.getTree(), Main.get_str_from_file(PATH + "fun_node_test4.txt"));
    }

    @Test
    void funNodeTypeTooManyArgsTest() {
        Parser p = new Parser();
        ExprOrCallNode expr_node1 = p.parseExpr("\"a\"*30");
        ExprOrCallNode expr_node2 = p.parseExpr("\"aboba\"");
        FunctionInfo function = new FunctionInfo("smth", Arrays.asList(Type.STRING), FunctionReturnType.INT);
        FunNode fun_node = new FunNode(function, Arrays.asList(expr_node1, expr_node2));
        assertEquals(fun_node.getTree(), Main.get_str_from_file(PATH + "fun_node_test5.txt"));
    }

    @Test
    void funNoArgs() {
        FunctionInfo function = new FunctionInfo("justFun", Arrays.asList(), FunctionReturnType.VOID);
        FunNode fun_node = new FunNode(function, Arrays.asList());
        assertEquals(fun_node.getTree(), Main.get_str_from_file(PATH + "fun_node_test6.txt"));
    }

    @Test
    void funInFun() {
        FunctionInfo function_in = new FunctionInfo("inFun", Arrays.asList(), FunctionReturnType.INT);
        FunNode fun_in_node = new FunNode(function_in, Arrays.asList());

        FunctionInfo function_out = new FunctionInfo("outFun", Arrays.asList(Type.INTEGER), FunctionReturnType.VOID);
        FunNode fun_out_node = new FunNode(function_out, Arrays.asList(fun_in_node));
        assertEquals(fun_out_node.getTree(), Main.get_str_from_file(PATH + "fun_node_test7.txt"));
    }

    @Test
    void funNodeTypeMismatchingTest2() {
        FunctionInfo function_in = new FunctionInfo("inFun", Arrays.asList(), FunctionReturnType.INT);
        FunNode fun_in_node = new FunNode(function_in, Arrays.asList());

        FunctionInfo function_out = new FunctionInfo("outFun", Arrays.asList(Type.STRING), FunctionReturnType.VOID);
        FunNode fun_out_node = new FunNode(function_out, Arrays.asList(fun_in_node));
        assertEquals(fun_out_node.getTree(), Main.get_str_from_file(PATH + "fun_node_test8.txt"));
    }

    @Test
    void exprFromFunsTest() {
        FunctionInfo function_in1 = new FunctionInfo("inFun", Arrays.asList(), FunctionReturnType.INT);
        FunNode fun_in_node1 = new FunNode(function_in1, Arrays.asList());

        FunctionInfo function_in21 = new FunctionInfo("inFun", Arrays.asList(), FunctionReturnType.INT);
        FunNode fun_in_node21 = new FunNode(function_in21, Arrays.asList());

        FunctionInfo function_in2 = new FunctionInfo("outFun", Arrays.asList(Type.INTEGER), FunctionReturnType.STR);
        FunNode fun_in_node2 = new FunNode(function_in2, Arrays.asList(fun_in_node21));

        ExprNode expr = new ExprNode(fun_in_node1, fun_in_node2, BinopConstants.MUL);
        assertEquals(expr.getTree(), Main.get_str_from_file(PATH + "expr_node_test1.txt"));
    }

}

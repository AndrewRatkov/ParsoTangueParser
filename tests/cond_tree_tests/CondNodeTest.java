package tests.cond_tree_tests;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import src.Main;
import src.Parser;
import src.consts.Type;
import src.nodes.CondNode;
import src.nodes.ExprNode;
import src.nodes.Node;


public class CondNodeTest {
    private String PATH = "tests/cond_tree_tests/";


    @Test
    void getTreeTest1() { // basic if-then-else-fi expression
        Parser p = new Parser();
        ExprNode expr_node = p.parseExpr("1+2*(\"aba\"==\"a\"+\"ba\")");
        
        HashMap<String, Type> vars_before = new HashMap<>();
        vars_before.put("s", Type.STRING);

        Parser ip = new Parser(vars_before);
        
        List<Node> then_nodes = new ArrayList<>(), else_nodes = new ArrayList<>();

        then_nodes.add(ip.parseInstr("int x := 3+3;"));
        then_nodes.add(ip.parseInstr("str y := s * 2;"));
        else_nodes.add(ip.parseInstr("s := \"Andrew Ratkov\";"));

        CondNode cond_node = new CondNode(expr_node, then_nodes, else_nodes);
        String expected = Main.get_str_from_file(PATH + "cond_node_test1.txt");
        assertEquals(cond_node.getTree(), expected);
    }

    @Test
    void getTreeTest2() { // basic if-then-fi expression
        Parser p = new Parser();
        ExprNode expr_node = p.parseExpr("2+2==4");

        List<Node> then_nodes = new ArrayList<>();
        Parser ip = new Parser();
        then_nodes.add(ip.parseInstr("int x := (1+2)*3;"));
        then_nodes.add(ip.parseInstr("x := x - x;"));

        CondNode cond_node = new CondNode(expr_node, then_nodes, null);
        String expected = Main.get_str_from_file(PATH + "cond_node_test2.txt");
        assertEquals(cond_node.getTree(), expected);
    }

    @Test
    void getTreeTest3() { // nested if expressions
        Parser p = new Parser();
        ExprNode expr_node_out = p.parseExpr("1+(2>1)==5");
        ExprNode expr_node_in = p.parseExpr("1==\"1\"");

        List<Node> then_in_nodes = new ArrayList<>(), else_in_nodes = new ArrayList<>();
        HashMap<String, Type> vars_before = new HashMap<>();
        vars_before.put("x", Type.INTEGER);
        vars_before.put("y", Type.INTEGER);

        Parser ip = new Parser(vars_before);

        then_in_nodes.add(ip.parseInstr("x := 1+2;"));
        then_in_nodes.add(ip.parseInstr("x := x - 3;"));
        else_in_nodes.add(ip.parseInstr("y := 5;"));

        List<Node> then_out_nodes = new ArrayList<>(), else_out_nodes = new ArrayList<>();
        else_out_nodes.add(ip.parseInstr("y := 2;"));
        then_out_nodes.add(ip.parseInstr("y := x + 2;"));

        CondNode cond_node_in = new CondNode(expr_node_in, then_in_nodes, else_in_nodes);
        then_out_nodes.add(cond_node_in);
        CondNode cond_node_out = new CondNode(expr_node_out, then_out_nodes, else_out_nodes);

        String expected = Main.get_str_from_file(PATH + "cond_node_test3.txt");
        assertEquals(cond_node_out.getTree(), expected);
    }
}

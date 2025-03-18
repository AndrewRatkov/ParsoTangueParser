package tests.instructions_parsing_tests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.HashMap;

import src.Main;
import src.Parser;
import src.consts.Expr;
import src.consts.FunctionInfo;
import src.consts.FunctionReturnType;
import src.consts.Type;
import src.nodes.ExprNode;
import src.nodes.InstrNode;
import src.nodes.AssnNode;


public class InstructionParsingTest {
    private String PATH = "tests/instructions_parsing_tests/";
    private HashMap<String, FunctionInfo> hm = new HashMap<>(){{
        put("f", new FunctionInfo("f", Arrays.asList(Type.INTEGER), FunctionReturnType.INT));
        put("g", new FunctionInfo("g", Arrays.asList(), FunctionReturnType.STR));
        put("h", new FunctionInfo("h", Arrays.asList(Type.INTEGER, Type.STRING), FunctionReturnType.VOID));
    }};

    @ParameterizedTest
    @CsvSource({
        "int x := 123", // missing ; at the end
        "12 := 12;", // starts with digit
        "x := 123;", // type not declared
        "int 2e5 := 1000;", // varname starts with digit
        "int str := 90;", // varname is type
        "int x = 10;", // := expected, but here is =
        "int x := \"abacaba\";", // type error
        "str x := 30;", // type error
        "int x;" // must be initialized immediately
    })
    void testErrors(String expr) {
        Parser p = new Parser();
        assertEquals(((AssnNode)p.parseInstr(expr)).getType(), Expr.ErrorExpr);
    }

    @Test
    void testErrors2() {
        Parser p = new Parser();
        assertEquals(p.Variables.get("x"), null);
        AssnNode n1 = (AssnNode)p.parseInstr("int x:=30;");
        assertEquals(n1.getType(), Expr.IntExpr);
        assertEquals(n1.getVarname(), "x");
        assertEquals(((ExprNode)n1.getExpression()).getType(), Expr.IntExpr);

        assertEquals(((AssnNode)p.parseInstr("int x := 23;")).getType(), Expr.ErrorExpr); // dont have to declare the type
        assertEquals(((AssnNode)p.parseInstr("str x := 23;")).getType(), Expr.ErrorExpr); // dont have to declare the type
        assertEquals(((AssnNode)p.parseInstr("x := \"23\";")).getType(), Expr.ErrorExpr); // type must be int
        assertEquals(((AssnNode)p.parseInstr("x := x * \"abacaba\";")).getType(), Expr.ErrorExpr); // type must be int
        assertEquals(((AssnNode)p.parseInstr("x := x + x * x;")).getType(), Expr.IntExpr); // OK
        assertEquals(p.Variables.get("x"), Type.INTEGER);
    }

    @Test
    void testReturnParsing() {
        Parser p = new Parser();
        p.enableReturnInstructions(FunctionReturnType.INT);
        System.out.println(p.parseInstr("return 12;").getTree());
    }

    @ParameterizedTest
    @CsvSource({
        "test1,INT",
        "test2,INT",
        "test3,STR",
        "test4,VOID",
        "test5,VOID",
        "test6,INT",
        "test7,INT",
        "test8,INT",
    })
    void instrTest(String test_name, FunctionReturnType frt) {
        Parser p = new Parser(null, hm);
        p.enableReturnInstructions(frt);

        String instr = Main.get_str_from_file(PATH + test_name + ".in");
        while (instr.endsWith("\n")) instr = instr.substring(0, instr.length() - 1);
        InstrNode n = p.parseInstr(instr);
        System.err.println(n.getTree());
        assertEquals(n.getTree(), Main.get_str_from_file(PATH + test_name + ".out"));
    }
}

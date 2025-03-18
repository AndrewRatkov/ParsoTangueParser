package tests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import src.Parser;
import src.consts.Expr;
import src.consts.Type;
import src.nodes.ExprNode;
import src.nodes.InstrNode;


public class InstructionParsingTest {
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
        assertEquals(p.parseInstr(expr).getType(), Expr.ErrorExpr);
    }

    @Test
    void testErrors2() {
        Parser p = new Parser();
        assertEquals(p.Variables.get("x"), null);
        InstrNode n1 = p.parseInstr("int x:=30;");
        assertEquals(n1.getType(), Expr.IntExpr);
        assertEquals(n1.getVarname(), "x");
        assertEquals(((ExprNode)n1.getExpression()).getType(), Expr.IntExpr);

        assertEquals(p.parseInstr("int x := 23;").getType(), Expr.ErrorExpr); // dont have to declare the type
        assertEquals(p.parseInstr("str x := 23;").getType(), Expr.ErrorExpr); // dont have to declare the type
        assertEquals(p.parseInstr("x := \"23\";").getType(), Expr.ErrorExpr); // type must be int
        assertEquals(p.parseInstr("x := x * \"abacaba\";").getType(), Expr.ErrorExpr); // type must be int
        assertEquals(p.parseInstr("x := x + x * x;").getType(), Expr.IntExpr); // OK
        assertEquals(p.Variables.get("x"), Type.INTEGER);
    }
}

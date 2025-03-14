package tests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import src.InstrParser;
import src.Expr;
import src.InstrNode;


public class InstrParserTest {
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
        InstrParser p = new InstrParser();
        assertEquals(p.parseInstr(expr).type, Expr.ErrorExpr);
    }

    @Test
    void testErrors2() {
        InstrParser p = new InstrParser();
        InstrNode n1 = p.parseInstr("int x:=30;");
        assertEquals(n1.type, Expr.IntExpr);
        assertEquals(n1.var_name, "x");
        assertEquals(n1.expression.type, Expr.IntExpr);

        assertEquals(p.parseInstr("int x := 23;").type, Expr.ErrorExpr); // dont have to declare the type
        assertEquals(p.parseInstr("str x := 23;").type, Expr.ErrorExpr); // dont have to declare the type
        assertEquals(p.parseInstr("x := \"23\";").type, Expr.ErrorExpr); // type must be int
        assertEquals(p.parseInstr("x := x * \"abacaba\";").type, Expr.ErrorExpr); // type must be int
        
        assertEquals(p.parseInstr("x := x + x * x;").type, Expr.IntExpr); // OK
    }
}

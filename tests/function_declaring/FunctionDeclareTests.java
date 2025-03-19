package tests.function_declaring;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import src.Parser;
import src.nodes.FunctionDeclareNode;
import src.Main;

public class FunctionDeclareTests {
    private String PATH = "tests/function_declaring/";

    @ParameterizedTest
    @CsvSource({
        "test1",
        "test2",
        "test3",
        "test4",
        "test5",
        "test6",
        "test7",
        "test8",
        "test9",
        "test10",
    })
    void test(String test_name) {
        String f_decl = Main.get_str_from_file(PATH + test_name + ".in").trim();
        Parser cp = new Parser();
        FunctionDeclareNode n = cp.parseFunDefinition(f_decl);
        assertEquals(n.getTree(), Main.get_str_from_file(PATH + test_name + ".out"));
    }
}

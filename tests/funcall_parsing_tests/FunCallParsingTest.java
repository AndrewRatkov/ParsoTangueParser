package tests.funcall_parsing_tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.HashMap;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import src.Main;
import src.Parser;
import src.consts.FunctionInfo;
import src.consts.FunctionReturnType;
import src.consts.Type;
import src.nodes.FunNode;


public class FunCallParsingTest {
    private String PATH = "tests/funcall_parsing_tests/";
    private HashMap<String, FunctionInfo> hm = new HashMap<>(){{
        put("f", new FunctionInfo("f", Arrays.asList(Type.INTEGER), FunctionReturnType.INT));
        put("g", new FunctionInfo("g", Arrays.asList(), FunctionReturnType.STR));
        put("h", new FunctionInfo("h", Arrays.asList(Type.INTEGER, Type.STRING), FunctionReturnType.VOID));
    }};

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
        "test11",
    })
    void test(String test_name) {
        Parser p = new Parser(null, hm);
        String call = Main.get_str_from_file(PATH + test_name + ".in");
        FunNode n = p.parseFunCall(call);
        assertEquals(n.getTree(), Main.get_str_from_file(PATH + test_name + ".out"));
    }
}

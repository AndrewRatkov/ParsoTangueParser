package tests.commands_trees_tests;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.Assert.assertEquals;


import src.Parser;
import src.Main;
import src.consts.Expr;
import src.consts.TextReaderResponses;
import src.nodes.CondNode;
import src.nodes.Node;
import src.structs.Pair;


public class CommandsParsingTest {
    private String PATH = "tests/commands_trees_tests/";

    @Test
    public void noStatementTest() { // условия нет, но всё остальное есть, и поэтому случится частичный разбор
        Parser cp = new Parser();

        Pair<TextReaderResponses, List<Node>> nodes = cp.parseCondition("if then int x := 1; fi");
        assertEquals(nodes.first(), TextReaderResponses.OK);
        assertEquals(nodes.second().size(), 1);

        CondNode n = (CondNode) nodes.second().getFirst();
        assertEquals(n.exprStmt(), Expr.ErrorExpr);
        assertEquals(n.getTree(), Main.get_str_from_file(PATH + "no_statement_test.txt"));
    }


    @ParameterizedTest
    @CsvSource({
        "test_only_if",
        "test_if_with_else",
        "test_example",
        "bad_brackets", // с неправильной скобочной последовательностью
        "test_scopes", // на области видимости: в непересекающихся областях объявляется переменная с одним и тем же именем, но разным типом
        "test_scopes_and_nested_ifs", // то же самое
        "test_empty_then" // после then до else (или до fi) 0  инструкций -- это ок
    })
    void commandsParserOKTests(String test_name) {
        String cmds = Main.get_str_from_file(PATH + test_name + ".in");
        
        Parser cp = new Parser();
        Pair<TextReaderResponses, List<Node>> nodes_info = cp.parseCondition(cmds);

        assertEquals(nodes_info.first(), TextReaderResponses.OK);
        String answer = "";
        for (Node n : nodes_info.second()) {
            answer += n.getTree();
        }

        assertEquals(answer, Main.get_str_from_file(PATH + test_name + ".out"));
    }

}

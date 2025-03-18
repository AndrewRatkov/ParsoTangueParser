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
import src.nodes.ExprNode;
import src.nodes.Node;
import src.structs.Pair;


public class CommandsParsingTest {
    private String PATH = "tests/commands_trees_tests/";

    @Test
    public void noStatementTest() { // условия нет, но всё остальное есть, и поэтому случится частичный разбор
        Parser cp = new Parser();

        Pair<TextReaderResponses, List<Node>> nodes = cp.parseCommands("if then int x := 1; fi");
        assertEquals(nodes.first(), TextReaderResponses.OK);
        assertEquals(nodes.second().size(), 1);

        CondNode n = (CondNode) nodes.second().getFirst();
        assertEquals(((ExprNode)n.getStmt()).getType(), Expr.ErrorExpr);
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
        "test_empty_then", // после then до else (или до fi) 0  инструкций -- это ок
        "test_file_with_errors", // файл с пустым statement'ом и инвалидной инструкцией. но парсер всё равно постоит дерево разбора с пометками ошибочных вершин
    })
    void commandsParserOKTests(String test_name) {
        String cmds = Main.get_str_from_file(PATH + test_name + ".in");
        
        Parser cp = new Parser();
        Pair<TextReaderResponses, List<Node>> nodes_info = cp.parseCommands(cmds);

        assertEquals(nodes_info.first(), TextReaderResponses.OK);
        String answer = "";
        for (Node n : nodes_info.second()) {
            answer += n.getTree();
        }
        assertEquals(answer, Main.get_str_from_file(PATH + test_name + ".out"));
    }

    @ParameterizedTest
    @CsvSource({
        "test_err_1,NOT_CLOSED_IF",
        "test_err_2,NOTHING_AFTER_THEN",
        "test_err_3,DOUBLE_ELSE",
        "test_err_4,EXTRA_FI",
    })
    void commandsParsingErrorsTest(String test_name, TextReaderResponses response) {
        String cmds = Main.get_str_from_file(PATH + test_name + ".in");
        Parser cp = new Parser();
        Pair<TextReaderResponses, List<Node>> nodes_info = cp.parseCommands(cmds);
        assertEquals(nodes_info.first(), response);
    }



}

package tests.commands_trees_tests;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import src.CommandsParser;
import src.CondNode;
import src.Expr;
import src.Main;
import src.Node;
import src.TextReaderResponses;
import src.structs.Pair;


public class CommandsParserTest {
    private String PATH = "tests/commands_trees_tests/";

    @Test
    public void noStatementTest() { // условия нет, но всё остальное есть, и поэтому случится частичный разбор
        CommandsParser cp = new CommandsParser();

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
    })
    void commandsParserOKTests(String test_name) {
        String cmds = Main.get_str_from_file(PATH + test_name + ".in");
        
        CommandsParser cp = new CommandsParser();
        Pair<TextReaderResponses, List<Node>> nodes_info = cp.parseCondition(cmds);

        assertEquals(nodes_info.first(), TextReaderResponses.OK);
        String answer = "";
        for (Node n : nodes_info.second()) {
            answer += n.getTree();
        }

        assertEquals(answer, Main.get_str_from_file(PATH + test_name + ".out"));
    }

}

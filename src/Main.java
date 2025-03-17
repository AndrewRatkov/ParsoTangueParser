package src;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import src.consts.TextReaderResponses;
import src.nodes.Node;
import src.structs.Pair;

public class Main {

    public static String readFile(String filename) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line + '\n');
            }
        }
        return content.toString();
    }

    public static String get_str_from_file(String filename) {
        try {
            return Main.readFile(filename);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
            return "";
        }
    }

    public static void writeFile(String filename, String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(content);
        }
        
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java Main <input_file> <output_file>");
            System.exit(1);
        }

        String input = args[0], output = args[1];
        Parser parser = new Parser();

        String cmds = get_str_from_file(input);
        Pair<TextReaderResponses, List<Node>> nodes_info = parser.parseCondition(cmds);
        
        //assertEquals(nodes_info.first(), TextReaderResponses.OK);
        String answer = "";
        for (Node n : nodes_info.second()) {
            answer += n.getTree();
        }
        
        try {
            Main.writeFile(output, answer);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
}

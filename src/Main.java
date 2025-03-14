package src;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Main {

    private static String readFile(String filename) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
        }
        return content.toString();
    }

    private static void writeFile(String filename, String content) throws IOException {
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
        InstrParser parser = new InstrParser();

        try {
            String text = readFile(input);
            List<String> instructions = InstructionSplitter.splitText(text);
            String tree = "";
            for (String instr : instructions) {
                InstrNode root = parser.parseInstr(instr);
                tree += root.buildTree() + '\n';    
            }
            writeFile(output, tree);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }

    }
}

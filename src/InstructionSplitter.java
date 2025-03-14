package src;

import java.util.ArrayList;
import java.util.List;

/*
 * Разделяет текст на инструкции. Пока что это просто куча инструкций, осле каждой из которых должна стоять ; 
 */
public class InstructionSplitter {
    public static List<String> splitText(String text) {
        List<String> instructions = new ArrayList<>();
        int used_quotes = 0; // we dont want to split by signs ; if they are in strings (for example, this is one instruction: str x := "a;b;c;d"; )
        String cur_instruction = "";
        boolean skipping_spaces = true;
        for (char c : text.toCharArray()) {
            if (skipping_spaces) {
                if (Character.isWhitespace(c)) continue;
                else skipping_spaces = false;
            }

            cur_instruction += c;
            if (c == ';' && used_quotes % 2 == 0) {
                instructions.add(cur_instruction);
                cur_instruction = "";
                skipping_spaces = true;
            } else if (c == '\"') {
                ++used_quotes;
            }
        }
        if (cur_instruction != "") return null; // if there is something else in text after last ';', it is an error 

        return instructions;
    }
}

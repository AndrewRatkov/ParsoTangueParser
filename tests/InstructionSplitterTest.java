package tests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import src.InstructionSplitter;


public class InstructionSplitterTest {
    @ParameterizedTest
    @CsvSource({
        "abracadabra",
        "a;b;c;d",
        "str x := \"abacaba;"
    })
    void testErrors(String text) {
        assertEquals(InstructionSplitter.splitText(text), null);      
    }

    @ParameterizedTest
    @CsvSource({
        "int x:= 1; int y:= x * x + x; str me := \"hrundelair\";, 3",
        "int x:= 1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1;, 1",
        "\"bad string but ends \" correctly;, 1",
        ";, 1",
    })
    void testOk(String text, int expected_length) {
        assertEquals(InstructionSplitter.splitText(text).size(), expected_length);
    }

    @Test
    void testEmpty() {
        assertEquals(InstructionSplitter.splitText("").size(), 0);
    }

    @Test
    void testWithLineskips() {
        String text = "int x := 1+2+3+4+(\"a\"!=\"b\"*4)!=7;  \t \n\n";
        assertEquals(InstructionSplitter.splitText(text).size(), 1);
    }
}

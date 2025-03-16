package src;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import src.structs.Pair;

public class CommandsParser {
    private InstrParser instr_parser;
    
    public CommandsParser() {
        this.instr_parser = new InstrParser();
    }

    public CommandsParser(InstrParser ip) {
        this.instr_parser = ip;
    }

    private int skipWord(char[] char_array, int idx, String keyword) {
        for (int i = idx; i < idx + keyword.length(); ++i) {
            if (i >= char_array.length || char_array[i] != keyword.charAt(i - idx)) return -1;
        }
        return idx + keyword.length();
    }

    public Pair<TextReaderResponses, List<Node>> parseCondition(String str) {
        char[] char_array = str.trim().toCharArray();
        int len = char_array.length;
        if (len == 0) return new Pair<>(TextReaderResponses.OK, new ArrayList<>()); // empty 
        
        List<Node> result = new ArrayList<>();
        
        Stack<InstrParser> parsers = new Stack<>();
        Stack<ExprNode> stmts = new Stack<>();
        Stack<List<Node> > then_node_lists = new Stack<>(), else_node_lists = new Stack<>();
        
        int idx = 0;
        while (idx < len) {
            System.err.println("idx: " + idx);
            if (Character.isWhitespace(char_array[idx])) {
                ++idx; continue;
            }


            int skip_if = skipWord(char_array, idx, "if");
            if (skip_if != -1) {
                if (len == skip_if) return new Pair<>(TextReaderResponses.STATEMENT_NOT_FOUND, result); // nothing after if
                else if (Character.isWhitespace(char_array[skip_if])) {
                    int used_quotes = 0;
                    idx = skip_if;
                    while (idx < len) {
                        if (char_array[idx] == '\"') {
                            ++used_quotes;
                            ++idx;
                            continue;
                        } else if (used_quotes % 2 == 1) {
                            ++idx;
                            continue;
                        }

                        int skip_then = skipWord(char_array, idx, "then");
                        System.err.println("idx: " + idx + ", skip then: " + skip_then);
                        if (skip_then != -1 && Character.isWhitespace(char_array[idx - 1])) {
                            if (len == skip_then) {
                                return new Pair<>(TextReaderResponses.NOTHING_AFTER_THEN, result); // nothing after then
                            } else if (Character.isWhitespace(char_array[skip_then])) { // it is really keyword "then"
                                ExprParser expr_parser;
                                if (parsers.empty()) expr_parser = instr_parser.getExprParser();
                                else expr_parser = parsers.peek().getExprParser();
                                
                                ExprNode expr_node = expr_parser.parseExpr(String.copyValueOf(char_array, skip_if, idx - skip_if));
                                stmts.push(expr_node);
                                if (parsers.empty()) parsers.push((InstrParser)instr_parser.clone());
                                else parsers.push((InstrParser)parsers.peek().clone());
                                then_node_lists.push(new ArrayList<>());
                                else_node_lists.push(null);
                                idx = skip_then;
                                break;
                            }
                        }
                        ++idx;
                    }
                    if (idx == len) return new Pair<>(TextReaderResponses.THEN_NOT_FOUND, result);
                    continue;
                }
            }

            int skip_else = skipWord(char_array, idx, "else");
            if (skip_else != -1) {
                if (len == skip_else) return new Pair<>(TextReaderResponses.NOTHING_AFTER_ELSE, result);
                else if (Character.isWhitespace(char_array[skip_else])) {
                    idx = skip_else;
                    if (else_node_lists.peek() != null) {
                        return new Pair<>(TextReaderResponses.DOUBLE_ELSE, result);
                    } else {
                        else_node_lists.pop();
                        else_node_lists.push(new ArrayList<>());
                        parsers.pop();
                        if (parsers.empty()) parsers.push((InstrParser)instr_parser.clone());
                        else parsers.push((InstrParser)parsers.peek().clone()); 
                    }
                    continue;
                }
            }

            int skip_fi = skipWord(char_array, idx, "fi");
            if (skip_fi != -1 && (skip_fi == len || Character.isWhitespace(char_array[skip_fi]))) {
                if (stmts.empty()) {
                    return new Pair<>(TextReaderResponses.EXTRA_FI, result);
                } else {
                    idx = skip_fi;
                    CondNode cond = new CondNode(stmts.pop(), then_node_lists.pop(), else_node_lists.pop());
                    if (stmts.empty()) result.add(cond);
                    else if (else_node_lists.peek() == null) then_node_lists.peek().add(cond);
                    else else_node_lists.peek().add(cond);
                    continue;
                }
            }

            int used_quotes = 0;
            int i = idx;
            while (i < len) {
                if (char_array[i] == '\"') ++used_quotes;
                else if (used_quotes % 2 == 0 && char_array[i] == ';') {++i; break;}
                ++i;
            }
            if (i == len) {
                return new Pair<>(TextReaderResponses.ENDING_NOT_PARSED, result);
            } else {
                InstrParser ip;
                if (stmts.empty()) ip = this.instr_parser;
                else ip = parsers.peek();

                System.out.println("parsing: " + String.copyValueOf(char_array, idx, i - idx));
                InstrNode instr = ip.parseInstr(String.copyValueOf(char_array, idx, i - idx));
                System.err.println(instr.getTree());
                if (stmts.empty()) result.add(instr);
                else if (else_node_lists.peek() == null) then_node_lists.peek().add(instr);
                else else_node_lists.peek().add(instr);
                idx = i;
            }
        }


        return new Pair<TextReaderResponses,List<Node>>(TextReaderResponses.OK, result);
    }
}

package src;
import java.util.HashSet;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

/*
 * Объект этого класса умеет парсить выражения --
 * то есть получать на вход строку из арифметических операций над числами/строками,
 * и строить для неё дерево разбора.
 * Затем это дерево разбора можно напечатать в файл, например
*/
public class Parser{
    private HashSet<String> Integers;
    private HashSet<String> Strings;

    public Parser() {
        this.Integers = new HashSet<>();
        this.Strings = new HashSet<>();
    }

    public Parser(HashSet<String> _Integers, HashSet<String> _Strings) {
        this.Integers = _Integers;
        this.Strings = _Strings;
    }


    public Pair<ReaderResponses, Integer> read(char[] str, int idx) {
        if (idx >= str.length) {
            return new Pair<>(ReaderResponses.FINISHED_READING, -1);
        } else if (Character.isWhitespace(str[idx])) {
            while (idx < str.length && Character.isWhitespace(str[idx])) ++idx;
            return new Pair<>(ReaderResponses.SKIPPED, idx);
        } else if (str[idx] == '(') {
            return new Pair<>(ReaderResponses.OPENNING_BRACKET, idx + 1);
        } else if (str[idx] == ')') {
            return new Pair<>(ReaderResponses.CLOSING_BRACKET, idx + 1);
        } else if (BinopConstants.BINOP_FIRST_CHARS.indexOf(str[idx]) != -1) {
            if (idx + 1 == str.length || BinopConstants.BINOP_FIRST_CHARS.indexOf(str[idx + 1]) == -1) {
                if (BinopConstants.SHORT_BINOPS.indexOf(str[idx]) != -1) {
                    return new Pair<>(ReaderResponses.READ_BINOP, idx + 1);
                } else {
                    return new Pair<>(ReaderResponses.ERROR_UNKNOWN_CHAR, idx + 1);
                }
            } else {
                if (BinopConstants.LONG_BINOPS.contains("" + str[idx] + str[idx + 1])) {
                    return new Pair<>(ReaderResponses.READ_BINOP, idx + 2);
                } else {
                    return new Pair<>(ReaderResponses.ERROR_UNKNOWN_CHAR, idx + 1);
                }
            }
        } else if (Character.isDigit(str[idx])) { // reading an integer
            while (idx < str.length && Character.isDigit(str[idx])) ++idx;
            if (idx == str.length || Character.isWhitespace(str[idx]) || BinopConstants.BINOP_FIRST_CHARS.indexOf(str[idx]) != -1 || str[idx] == ')') {
                return new Pair<>(ReaderResponses.READ_INTEGER, idx);
            } else {
                return new Pair<>(ReaderResponses.ERROR_VAR_STARTS_WITH_DIGITS, idx);
            }
        } else if (str[idx] == '\"') {
            ++idx;
            while (idx < str.length && str[idx] != '\"') ++idx;
            if (idx == str.length) return new Pair<>(ReaderResponses.ERROR_STRING_DOESNT_END, idx);
            return new Pair<>(ReaderResponses.READ_STRING, idx + 1);
        } else if (Character.isLetter(str[idx])){ // reading a variable
            while (Character.isLetterOrDigit(str[idx])) ++idx;
            if (idx == str.length || Character.isWhitespace(str[idx]) || BinopConstants.BINOP_FIRST_CHARS.indexOf(str[idx]) != -1 || str[idx] == ')') {
                return new Pair<>(ReaderResponses.READ_VARIABLE, idx);
            } else {
                return new Pair<>(ReaderResponses.ERROR_AFTER_VARIABLE, idx);
            }
        } else {
            return new Pair<>(ReaderResponses.ERROR_UNKNOWN_CHAR, idx);
        }
    }

    public Node parsePrimaryExpr(Stack<Node> nodes, Stack<Binop> binops, int last_nodes) {
        // pop last (last_nodes) elements from nodes and apply their binops to them
        assert last_nodes > 0;
        Node t = nodes.pop();
        if (last_nodes == 1) return t;

        // repack last (last_nodes) nodes and last (last_nodes - 1) binops into new arraylists
        Stack<Node> temp_stack_nodes = new Stack<>();
        temp_stack_nodes.push(t);
        Stack<Binop> temp_stack_binops = new Stack<>();
        for (int k = 0; k < last_nodes - 1; ++k) {
            temp_stack_nodes.push(nodes.pop());
            temp_stack_binops.push(binops.pop());
        }
        
        List<Node> expr_nodes = new ArrayList<>();
        while (!temp_stack_nodes.isEmpty()) expr_nodes.add(temp_stack_nodes.pop());

        List<Binop> expr_binops = new ArrayList<>();
        while (!temp_stack_binops.isEmpty()) expr_binops.add(temp_stack_binops.pop());

        // simplify the expression (firstly, resolve binops with maximum priority, then the second ones, etc)
        for (int pr = BinopConstants.MAX_PROPRITY; pr >= BinopConstants.MIN_PROPRITY; --pr) {
            assert expr_binops.size() + 1 == expr_nodes.size();

            List<Node> new_nodes = new ArrayList<>();
            new_nodes.add(expr_nodes.get(0));
            List<Binop> new_binops = new ArrayList<>();

            for (int k = 0; k < expr_binops.size(); ++k) {
                if (expr_binops.get(k).priority < pr) {
                    new_nodes.add(expr_nodes.get(k + 1));
                    new_binops.add(expr_binops.get(k));
                } else {
                    Node lst = new_nodes.removeLast();
                    new_nodes.add(new Node(lst, expr_nodes.get(k + 1), expr_binops.get(k)));
                }
            }

            expr_nodes = new_nodes;
            expr_binops = new_binops;
        }

        assert expr_nodes.size() == 1;
        return expr_nodes.get(0);
    }

    public Node parseExpr(String str) {
        int opened_brackets = 0;
        int used_quotes = 0;
        boolean in_string = false;
        boolean correct_brackets_sequence = true;

        char[] char_array = str.toCharArray();

        for (char c : char_array) {
            if (c == '\"') {
                ++used_quotes;
                in_string = !in_string;
            }
            else if (c == '(' && in_string) ++opened_brackets;
            else if (c == ')' && in_string) {
                if (opened_brackets == 0) {
                    correct_brackets_sequence = false;
                    break;
                } else --opened_brackets;
            }
        }
        if (opened_brackets > 0) return new Node(Expr.ErrorExpr, "Too many opened brackets");
        else if (used_quotes % 2 == 1) return new Node(Expr.ErrorExpr, "Not finished string");
        else if (!correct_brackets_sequence) return new Node(Expr.ErrorExpr, "Incorrect brackets sequence");

        Integer idx = 0;
        Boolean expected_value = true;
        Stack<Integer> openning_brackets = new Stack<>();
        Stack<Node> nodes = new Stack<>();
        Stack<Binop> binops = new Stack<>();

        Pair<ReaderResponses, Integer> p = read(char_array, idx);
        while (p.first() != ReaderResponses.FINISHED_READING) {
            // System.out.println(p);
            switch (p.first()) {
                case ERROR_AFTER_VARIABLE:
                    return new Node(Expr.ErrorExpr, "Character " + char_array[p.second()] + " after variable name");
                case ERROR_UNKNOWN_CHAR:
                    return new Node(Expr.ErrorExpr, "Unknown character " + char_array[idx]);
                case ERROR_VAR_STARTS_WITH_DIGITS:
                    return new Node(Expr.ErrorExpr, "Variable starts with digits");
                case OPENNING_BRACKET:
                    openning_brackets.push(nodes.size());
                    break;
                case SKIPPED:
                    break;
                case READ_BINOP:
                    if (expected_value) {
                        return new Node(Expr.ErrorExpr, "Expected value, got binop");
                    } else {
                        Binop new_binop = BinopConstants.get_by_string(String.copyValueOf(char_array, idx, p.second() - idx));
                        binops.push(new_binop);
                        expected_value = true;
                        break;
                    }
                case READ_INTEGER:
                    if (!expected_value) {
                        return new Node(Expr.ErrorExpr, "Expected binop, got value");
                    } else {
                        Node new_node = new Node(Expr.IntExpr, String.copyValueOf(char_array, idx, p.second() - idx));
                        nodes.push(new_node);
                        expected_value = false;
                        break;
                    }
                case READ_STRING:
                    if (!expected_value) {
                        return new Node(Expr.ErrorExpr, "Expected binop, got value");
                    } else {
                        Node new_node = new Node(Expr.StringExpr, String.copyValueOf(char_array, idx, p.second() - idx));
                        nodes.push(new_node);
                        expected_value = false;
                        break;
                    }
                case READ_VARIABLE:
                    if (!expected_value) {
                        return new Node(Expr.ErrorExpr, "Expected binop, got value");
                    } else {
                        String var_name = String.copyValueOf(char_array, idx, p.second() - idx);
                        Node new_node;
                        if (Integers.contains(var_name)) {
                            new_node = new Node(Expr.IntExpr, var_name);
                        } else if (Strings.contains(var_name)) {
                            new_node = new Node(Expr.StringExpr, var_name);
                        } else {
                            return new Node(Expr.ErrorExpr, "Unknown variable");
                        }
                        nodes.push(new_node);
                        expected_value = false;
                        break;
                    }
                case CLOSING_BRACKET:
                    if (expected_value) {
                        return new Node(Expr.ErrorExpr, "Got ')' after a binary operator, expected a value");
                    } 
                    Integer last_openning_bracket = openning_brackets.pop();
                    Node new_node = parsePrimaryExpr(nodes, binops, nodes.size() - last_openning_bracket);
                    nodes.push(new_node);
                    break;
                case FINISHED_READING:
                    break;
                case ERROR_STRING_DOESNT_END:
                    break;
                default:
                    break;
            }

            idx = p.second();
            p = read(char_array, idx);
        }
        //System.out.println(p);
        return parsePrimaryExpr(nodes, binops, nodes.size());
    } 
}

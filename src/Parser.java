package src;
import java.util.HashSet;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

import src.consts.Binop;
import src.consts.BinopConstants;
import src.consts.Constants;
import src.consts.Expr;
import src.consts.ReaderResponses;
import src.consts.TextReaderResponses;
import src.nodes.CondNode;
import src.nodes.ExprNode;
import src.nodes.InstrNode;
import src.nodes.Node;
import src.structs.Pair;

/*
 * Объект этого класса умеет парсить разные штуки:
 * арифметические выражения, инструкции присваивания, условные выражения --
 * и возвращать соответствующие Nod'ы
*/
public class Parser implements Cloneable {
    public HashSet<String> Integers;
    public HashSet<String> Strings;

    public Parser() {
        this.Integers = new HashSet<>();
        this.Strings = new HashSet<>();
    }

    public Parser(HashSet<String> _Integers, HashSet<String> _Strings) {
        this.Integers = _Integers;
        this.Strings = _Strings;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Parser clone() {
        Parser new_parser = new Parser();
        if (Integers != null) new_parser.Integers = (HashSet<String>)Integers.clone();
        if (Strings != null) new_parser.Strings = (HashSet<String>)Strings.clone();
        return new_parser; 
    } 

    /* читает следующий токен в арифметическом выражении */
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
            while (idx < str.length && Character.isLetterOrDigit(str[idx])) ++idx;
            if (idx == str.length || Character.isWhitespace(str[idx]) || BinopConstants.BINOP_FIRST_CHARS.indexOf(str[idx]) != -1 || str[idx] == ')') {
                return new Pair<>(ReaderResponses.READ_VARIABLE, idx);
            } else {
                return new Pair<>(ReaderResponses.ERROR_AFTER_VARIABLE, idx);
            }
        } else {
            return new Pair<>(ReaderResponses.ERROR_UNKNOWN_CHAR, idx);
        }
    }

    public ExprNode parsePrimaryExpr(Stack<ExprNode> nodes, Stack<Binop> binops, int last_nodes) {
        // pop last (last_nodes) elements from nodes and apply their binops to them
        assert last_nodes > 0;
        ExprNode t = nodes.pop();
        if (last_nodes == 1) return t;

        // repack last (last_nodes) nodes and last (last_nodes - 1) binops into new arraylists
        Stack<ExprNode> temp_stack_nodes = new Stack<>();
        temp_stack_nodes.push(t);
        Stack<Binop> temp_stack_binops = new Stack<>();
        for (int k = 0; k < last_nodes - 1; ++k) {
            temp_stack_nodes.push(nodes.pop());
            temp_stack_binops.push(binops.pop());
        }
        
        List<ExprNode> expr_nodes = new ArrayList<>();
        while (!temp_stack_nodes.isEmpty()) expr_nodes.add(temp_stack_nodes.pop());

        List<Binop> expr_binops = new ArrayList<>();
        while (!temp_stack_binops.isEmpty()) expr_binops.add(temp_stack_binops.pop());

        // simplify the expression (firstly, resolve binops with maximum priority, then the second ones, etc)
        for (int pr = BinopConstants.MAX_PROPRITY; pr >= BinopConstants.MIN_PROPRITY; --pr) {
            assert expr_binops.size() + 1 == expr_nodes.size();

            List<ExprNode> new_nodes = new ArrayList<>();
            new_nodes.add(expr_nodes.get(0));
            List<Binop> new_binops = new ArrayList<>();

            for (int k = 0; k < expr_binops.size(); ++k) {
                if (expr_binops.get(k).priority < pr) {
                    new_nodes.add(expr_nodes.get(k + 1));
                    new_binops.add(expr_binops.get(k));
                } else {
                    ExprNode lst = new_nodes.removeLast();
                    new_nodes.add(new ExprNode(lst, expr_nodes.get(k + 1), expr_binops.get(k)));
                }
            }

            expr_nodes = new_nodes;
            expr_binops = new_binops;
        }

        assert expr_nodes.size() == 1;
        return expr_nodes.get(0);
    }

    /*
     * Строит синтаксический разбор арифметического выражения
     */
    public ExprNode parseExpr(String str) {
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
            else if (c == '(' && !in_string) ++opened_brackets;
            else if (c == ')' && !in_string) {
                if (opened_brackets == 0) {
                    correct_brackets_sequence = false;
                    break;
                } else --opened_brackets;
            }
        }
        if (opened_brackets > 0) return new ExprNode(Expr.ErrorExpr, "Too many opened brackets");
        else if (used_quotes % 2 == 1) return new ExprNode(Expr.ErrorExpr, "Not finished string");
        else if (!correct_brackets_sequence) return new ExprNode(Expr.ErrorExpr, "Incorrect brackets sequence");

        int idx = 0;
        boolean expected_a_value = true;
        Stack<Integer> openning_brackets = new Stack<>();
        Stack<ExprNode> nodes = new Stack<>();
        Stack<Binop> binops = new Stack<>();

        Pair<ReaderResponses, Integer> p = read(char_array, idx);
        while (p.first() != ReaderResponses.FINISHED_READING) {
            switch (p.first()) {
                case ERROR_AFTER_VARIABLE:
                    return new ExprNode(Expr.ErrorExpr, "Character " + char_array[p.second()] + " after variable name");
                case ERROR_UNKNOWN_CHAR:
                    return new ExprNode(Expr.ErrorExpr, "Unknown character " + char_array[idx]);
                case ERROR_VAR_STARTS_WITH_DIGITS:
                    return new ExprNode(Expr.ErrorExpr, "Variable starts with digits");
                case OPENNING_BRACKET:
                    openning_brackets.push(nodes.size());
                    break;
                case SKIPPED:
                    break;
                case READ_BINOP:
                    if (expected_a_value) {
                        return new ExprNode(Expr.ErrorExpr, "Expected value, got binop");
                    } else {
                        Binop new_binop = BinopConstants.get_by_string(String.copyValueOf(char_array, idx, p.second() - idx));
                        binops.push(new_binop);
                        expected_a_value = true;
                        break;
                    }
                case READ_INTEGER:
                    if (!expected_a_value) {
                        return new ExprNode(Expr.ErrorExpr, "Expected binop, got value");
                    } else {
                        ExprNode new_node = new ExprNode(Expr.IntExpr, String.copyValueOf(char_array, idx, p.second() - idx));
                        nodes.push(new_node);
                        expected_a_value = false;
                        break;
                    }
                case READ_STRING:
                    if (!expected_a_value) {
                        return new ExprNode(Expr.ErrorExpr, "Expected binop, got value");
                    } else {
                        ExprNode new_node = new ExprNode(Expr.StringExpr, String.copyValueOf(char_array, idx, p.second() - idx));
                        nodes.push(new_node);
                        expected_a_value = false;
                        break;
                    }
                case READ_VARIABLE:
                    if (!expected_a_value) {
                        return new ExprNode(Expr.ErrorExpr, "Expected binop, got value");
                    } else {
                        String var_name = String.copyValueOf(char_array, idx, p.second() - idx);
                        ExprNode new_node;
                        if (Integers.contains(var_name)) {
                            new_node = new ExprNode(Expr.IntExpr, var_name);
                        } else if (Strings.contains(var_name)) {
                            new_node = new ExprNode(Expr.StringExpr, var_name);
                        } else {
                            return new ExprNode(Expr.ErrorExpr, "Unknown variable");
                        }
                        nodes.push(new_node);
                        expected_a_value = false;
                        break;
                    }
                case CLOSING_BRACKET:
                    if (expected_a_value) {
                        return new ExprNode(Expr.ErrorExpr, "Got ')' after a binary operator, expected a value");
                    } 
                    Integer last_openning_bracket = openning_brackets.pop();
                    ExprNode new_node = parsePrimaryExpr(nodes, binops, nodes.size() - last_openning_bracket);
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
        if (nodes.empty()) {
            return new ExprNode(Expr.ErrorExpr, "Empty expression");
        }
        return parsePrimaryExpr(nodes, binops, nodes.size());
    } 

    /*
    * Строит синтаксический разбор инструкции присваивания
    * Инструкции присваивания могут быть двух видов
    * 1) Type x := Expression;
    * -- где Expression -- выражение,
    * задаваемое Node и распознаваемое с помощью Parser, при этом переменная x еще не была объявлена
    * Вместо Type может быть int либо str (целочисленный тип либо строковый)
    * При этом проверяем, что Expression задаёт тот же тип, что и Type
    * 2) x := Expression;
    * -- то же самое, но переменная x была уже объявлена.
    * При этом запрещаем переменной x менять тип (то есть если она была типа str,
    * а Expression задает целое число, то это ошибка)
    */
    public InstrNode parseInstr(String str) {
        if (!str.endsWith(";")) { // instruction must end with ;
            return new InstrNode(Expr.ErrorExpr, "Instruction must end with \";\"", null);
        }

        int idx = 0;
        char[] char_array = str.toCharArray();
        while (Character.isWhitespace(char_array[idx])) ++idx;
        if (!Character.isLetter(char_array[idx])) {
            return new InstrNode(Expr.ErrorExpr, "Instruction cannot start with not a letter", null);
        }

        String first_word = "";
        while (Character.isLetterOrDigit(char_array[idx])) {
            first_word += char_array[idx];
            ++idx;
        }

        boolean type_declared = Constants.TYPE_NAMES.contains(first_word);
        String var_name = "";
        if (!type_declared) { // 2nd case: x := Expression;
            var_name = first_word;
            if ((!Integers.contains(var_name)) && (!Strings.contains(var_name))) {
                return new InstrNode(Expr.ErrorExpr, "Variable should have been initialized before", null);
            }
        } else { // 1st case: Type x := expression;
            while (Character.isWhitespace(char_array[idx])) ++idx;
            if (!Character.isLetter(char_array[idx])) {
                return new InstrNode(Expr.ErrorExpr, "Variable name cannot start with not a letter", null);
            }
            while (Character.isLetterOrDigit(char_array[idx])) {
                var_name += char_array[idx];
                ++idx;
            }

            if (Constants.TYPE_NAMES.contains(var_name)) {
                return new InstrNode(Expr.ErrorExpr, "Variable name cannot be a type name", null);
            }

            if (Integers.contains(var_name) || Strings.contains(var_name)) {
                return new InstrNode(Expr.ErrorExpr, "Variable was initialized before", null);
            }
        }

        while (Character.isWhitespace(char_array[idx])) ++idx;
        if (char_array[idx] != ':' || char_array[idx + 1] != '=') {
            return new InstrNode(Expr.ErrorExpr, "Expected :=, but not found", null);
        }
        idx += 2;

        String expression = new String(char_array, idx, char_array.length - idx - 1);
        ExprNode expr = parseExpr(expression);

        Expr type_of_expr_expected;
        if (type_declared) type_of_expr_expected = Constants.get_type(first_word);
        else type_of_expr_expected = (Integers.contains(var_name) ? Expr.IntExpr : Expr.StringExpr);

        if (expr.type != type_of_expr_expected) {
            return new InstrNode(Expr.ErrorExpr, "Expression expected to be " + type_of_expr_expected + " but was " + expr.type, null);
        }

        if (type_declared) {
            if (type_of_expr_expected == Expr.IntExpr) Integers.add(var_name);
            else Strings.add(var_name);
        }
        
        return new InstrNode(type_of_expr_expected, var_name, expr);
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
        
        Stack<Parser> parsers = new Stack<>();
        Stack<ExprNode> stmts = new Stack<>();
        Stack<List<Node> > then_node_lists = new Stack<>(), else_node_lists = new Stack<>();
        
        int idx = 0;
        while (idx < len) {
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
                        if (skip_then != -1 && Character.isWhitespace(char_array[idx - 1])) {
                            if (len == skip_then) {
                                return new Pair<>(TextReaderResponses.NOTHING_AFTER_THEN, result); // nothing after then
                            } else if (Character.isWhitespace(char_array[skip_then])) { // it is really keyword "then"
                                Parser expr_parser;
                                if (parsers.empty()) expr_parser = this;
                                else expr_parser = parsers.peek();
                                
                                ExprNode expr_node = expr_parser.parseExpr(String.copyValueOf(char_array, skip_if, idx - skip_if));
                                stmts.push(expr_node);
                                if (parsers.empty()) parsers.push(clone());
                                else parsers.push(parsers.peek().clone());
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
                        if (parsers.empty()) parsers.push(clone());
                        else parsers.push(parsers.peek().clone()); 
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
                else if (used_quotes % 2 == 0 && char_array[i] == ';') break;
                ++i;
            }
            if (i == len) {
                return new Pair<>(TextReaderResponses.ENDING_NOT_PARSED, result);
            } else {
                ++i; // shift after ';' symbol
                InstrNode instr;
                if (stmts.empty()) instr = parseInstr(String.copyValueOf(char_array, idx, i - idx));
                else instr = parsers.peek().parseInstr(String.copyValueOf(char_array, idx, i - idx));

                if (stmts.empty()) result.add(instr);
                else if (else_node_lists.peek() == null) then_node_lists.peek().add(instr);
                else else_node_lists.peek().add(instr);
                idx = i;
            }
        }


        return new Pair<TextReaderResponses,List<Node>>(TextReaderResponses.OK, result);
    }

}

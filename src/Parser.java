package src;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import src.consts.Binop;
import src.consts.BinopConstants;
import src.consts.Constants;
import src.consts.Expr;
import src.consts.ExprReaderResponses;
import src.consts.FunctionInfo;
import src.consts.FunctionReturnType;
import src.consts.TextReaderResponses;
import src.consts.Type;
import src.nodes.CondNode;
import src.nodes.ExprNode;
import src.nodes.ExprOrCallNode;
import src.nodes.FunNode;
import src.nodes.FunctionDeclareNode;
import src.nodes.InstrFunCallNode;
import src.nodes.InstrNode;
import src.nodes.AssnNode;
import src.nodes.Node;
import src.nodes.RetNode;
import src.structs.Pair;

/*
 * Объект этого класса умеет парсить разные штуки:
 * арифметические выражения, инструкции присваивания, условные выражения, функции --
 * и возвращать соответствующие Nod'ы
*/
public class Parser implements Cloneable {
    // todo private boolean return_instructions_allowed; // если парсим внутренность функций -- добавляется возможность парсить инструкции вида: return Expr;

    public HashMap<String, Type> Variables;
    public HashMap<String, FunctionInfo> Functions;
    private boolean accepts_return_instructions = false;
    private FunctionReturnType ret_type;

    public Parser() {
        this.Variables = new HashMap<>();
    }

    public Parser(HashMap<String, Type> _Variables) {
        this.Variables = _Variables;
    }

    public Parser(HashMap<String, Type> _Variables, HashMap<String, FunctionInfo> _Functions) {
        this.Variables = _Variables;
        this.Functions = _Functions;
    }

    public void enableReturnInstructions(FunctionReturnType _ret_type) {
        accepts_return_instructions = true;
        ret_type = _ret_type;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Parser clone() {
        Parser new_parser = new Parser();
        if (Variables != null) new_parser.Variables = (HashMap<String, Type>)Variables.clone();
        if (Functions != null) new_parser.Functions = (HashMap<String, FunctionInfo>)Functions.clone();
        new_parser.accepts_return_instructions = this.accepts_return_instructions;
        new_parser.ret_type = this.ret_type;
        return new_parser; 
    } 

    /* читает следующий токен в арифметическом выражении */
    public Pair<ExprReaderResponses, Integer> read(char[] str, int idx) {
        if (idx >= str.length) {
            return new Pair<>(ExprReaderResponses.FINISHED_READING, -1);
        } else if (Character.isWhitespace(str[idx])) {
            while (idx < str.length && Character.isWhitespace(str[idx])) ++idx;
            return new Pair<>(ExprReaderResponses.SKIPPED, idx);
        } else if (str[idx] == '(') {
            return new Pair<>(ExprReaderResponses.OPENNING_BRACKET, idx + 1);
        } else if (str[idx] == ')') {
            return new Pair<>(ExprReaderResponses.CLOSING_BRACKET, idx + 1);
        } else if (BinopConstants.BINOP_FIRST_CHARS.indexOf(str[idx]) != -1) {
            if (idx + 1 == str.length || BinopConstants.BINOP_FIRST_CHARS.indexOf(str[idx + 1]) == -1) {
                if (BinopConstants.SHORT_BINOPS.indexOf(str[idx]) != -1) {
                    return new Pair<>(ExprReaderResponses.READ_BINOP, idx + 1);
                } else {
                    return new Pair<>(ExprReaderResponses.ERROR_UNKNOWN_CHAR, idx + 1);
                }
            } else {
                if (BinopConstants.LONG_BINOPS.contains("" + str[idx] + str[idx + 1])) {
                    return new Pair<>(ExprReaderResponses.READ_BINOP, idx + 2);
                } else {
                    return new Pair<>(ExprReaderResponses.ERROR_UNKNOWN_CHAR, idx + 1);
                }
            }
        } else if (Character.isDigit(str[idx])) { // reading an integer
            while (idx < str.length && Character.isDigit(str[idx])) ++idx;
            if (idx == str.length || Character.isWhitespace(str[idx]) || BinopConstants.BINOP_FIRST_CHARS.indexOf(str[idx]) != -1 || str[idx] == ')') {
                return new Pair<>(ExprReaderResponses.READ_INTEGER, idx);
            } else {
                return new Pair<>(ExprReaderResponses.ERROR_VAR_STARTS_WITH_DIGITS, idx);
            }
        } else if (str[idx] == '\"') {
            ++idx;
            while (idx < str.length && str[idx] != '\"') ++idx;
            if (idx == str.length) return new Pair<>(ExprReaderResponses.ERROR_STRING_DOESNT_END, idx);
            return new Pair<>(ExprReaderResponses.READ_STRING, idx + 1);
        } else if (Character.isLetter(str[idx])){ // reading a variable or a function call
            while (idx < str.length && Character.isLetterOrDigit(str[idx])) ++idx;
            if (idx == str.length || Character.isWhitespace(str[idx]) || BinopConstants.BINOP_FIRST_CHARS.indexOf(str[idx]) != -1 || str[idx] == ')') {
                return new Pair<>(ExprReaderResponses.READ_VARIABLE, idx);
            } else if (str[idx] == '(') {
                int opened_brackets = 1, used_quotes = 0;
                ++idx;
                while (opened_brackets > 0) {
                    if (str[idx] == '\"') ++used_quotes;
                    if (used_quotes % 2 == 0) {
                        if (str[idx] == '(') ++opened_brackets;
                        else if (str[idx] == ')') --opened_brackets;
                    }
                    ++idx;
                }
                return new Pair<>(ExprReaderResponses.READ_FUN_CALL, idx);
            } else {
                return new Pair<>(ExprReaderResponses.ERROR_AFTER_VARIABLE, idx);
            }
        } else {
            return new Pair<>(ExprReaderResponses.ERROR_UNKNOWN_CHAR, idx);
        }
    }

    public ExprOrCallNode parsePrimaryExpr(Stack<ExprOrCallNode> nodes, Stack<Binop> binops, int last_nodes) {
        // pop last (last_nodes) elements from nodes and apply their binops to them
        assert last_nodes > 0;
        ExprOrCallNode t = nodes.pop();
        if (last_nodes == 1) return t;

        // repack last (last_nodes) nodes and last (last_nodes - 1) binops into new arraylists
        Stack<ExprOrCallNode> temp_stack_nodes = new Stack<>();
        temp_stack_nodes.push(t);
        Stack<Binop> temp_stack_binops = new Stack<>();
        for (int k = 0; k < last_nodes - 1; ++k) {
            temp_stack_nodes.push(nodes.pop());
            temp_stack_binops.push(binops.pop());
        }
        
        List<ExprOrCallNode> expr_nodes = new ArrayList<>();
        while (!temp_stack_nodes.isEmpty()) expr_nodes.add(temp_stack_nodes.pop());

        List<Binop> expr_binops = new ArrayList<>();
        while (!temp_stack_binops.isEmpty()) expr_binops.add(temp_stack_binops.pop());

        // simplify the expression (firstly, resolve binops with maximum priority, then the second ones, etc)
        for (int pr = BinopConstants.MAX_PROPRITY; pr >= BinopConstants.MIN_PROPRITY; --pr) {
            assert expr_binops.size() + 1 == expr_nodes.size();

            List<ExprOrCallNode> new_nodes = new ArrayList<>();
            new_nodes.add(expr_nodes.get(0));
            List<Binop> new_binops = new ArrayList<>();

            for (int k = 0; k < expr_binops.size(); ++k) {
                if (expr_binops.get(k).priority < pr) {
                    new_nodes.add(expr_nodes.get(k + 1));
                    new_binops.add(expr_binops.get(k));
                } else {
                    ExprOrCallNode lst = new_nodes.removeLast();
                    new_nodes.add(new ExprNode(lst, expr_nodes.get(k + 1), expr_binops.get(k)));
                }
            }

            expr_nodes = new_nodes;
            expr_binops = new_binops;
        }

        assert expr_nodes.size() == 1;
        assert expr_nodes.get(0) instanceof ExprNode;
        return expr_nodes.get(0);
    }

    /*
     * Строит синтаксический разбор арифметического выражения
     */
    public ExprOrCallNode parseExpr(String str) {
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
        Stack<ExprOrCallNode> nodes = new Stack<>();
        Stack<Binop> binops = new Stack<>();

        Pair<ExprReaderResponses, Integer> p = read(char_array, idx);
        while (p.first() != ExprReaderResponses.FINISHED_READING) {
            switch (p.first()) {
                case ExprReaderResponses.ERROR_AFTER_VARIABLE:
                    return new ExprNode(Expr.ErrorExpr, "Character " + char_array[p.second()] + " after variable name");
                case ExprReaderResponses.ERROR_UNKNOWN_CHAR:
                    return new ExprNode(Expr.ErrorExpr, "Unknown character " + char_array[idx]);
                case ExprReaderResponses.ERROR_VAR_STARTS_WITH_DIGITS:
                    return new ExprNode(Expr.ErrorExpr, "Variable starts with digits");
                case ExprReaderResponses.OPENNING_BRACKET:
                    openning_brackets.push(nodes.size());
                    break;
                case ExprReaderResponses.SKIPPED:
                    break;
                case ExprReaderResponses.READ_BINOP:
                    if (expected_a_value) {
                        return new ExprNode(Expr.ErrorExpr, "Expected value, got binop");
                    } else {
                        Binop new_binop = BinopConstants.get_by_string(String.copyValueOf(char_array, idx, p.second() - idx));
                        binops.push(new_binop);
                        expected_a_value = true;
                        break;
                    }
                case ExprReaderResponses.READ_INTEGER:
                    if (!expected_a_value) {
                        return new ExprNode(Expr.ErrorExpr, "Expected binop, got value");
                    } else {
                        ExprNode new_node = new ExprNode(Expr.IntExpr, String.copyValueOf(char_array, idx, p.second() - idx));
                        nodes.push(new_node);
                        expected_a_value = false;
                        break;
                    }
                case ExprReaderResponses.READ_STRING:
                    if (!expected_a_value) {
                        return new ExprNode(Expr.ErrorExpr, "Expected binop, got value");
                    } else {
                        ExprNode new_node = new ExprNode(Expr.StringExpr, String.copyValueOf(char_array, idx, p.second() - idx));
                        nodes.push(new_node);
                        expected_a_value = false;
                        break;
                    }
                case ExprReaderResponses.READ_VARIABLE:
                    if (!expected_a_value) {
                        return new ExprNode(Expr.ErrorExpr, "Expected binop, got value");
                    } else {
                        String var_name = String.copyValueOf(char_array, idx, p.second() - idx);
                        ExprNode new_node;
                        if (Variables.get(var_name) == Type.INTEGER) {
                            new_node = new ExprNode(Expr.IntExpr, var_name);
                        } else if (Variables.get(var_name) == Type.STRING) {
                            new_node = new ExprNode(Expr.StringExpr, var_name);
                        } else {
                            return new ExprNode(Expr.ErrorExpr, "Unknown variable");
                        }
                        nodes.push(new_node);
                        expected_a_value = false;
                        break;
                    }
                case ExprReaderResponses.READ_FUN_CALL:
                    if (!expected_a_value) {
                        return new ExprNode(Expr.ErrorExpr, "Expected binop, got value");
                    } else {
                        FunNode new_node = parseFunCall(String.copyValueOf(char_array, idx, p.second() - idx));
                        nodes.push(new_node);
                        expected_a_value = false;
                        break;
                    }
                case ExprReaderResponses.CLOSING_BRACKET:
                    if (expected_a_value) {
                        return new ExprNode(Expr.ErrorExpr, "Got ')' after a binary operator, expected a value");
                    } 
                    Integer last_openning_bracket = openning_brackets.pop();
                    ExprOrCallNode new_node = parsePrimaryExpr(nodes, binops, nodes.size() - last_openning_bracket);
                    nodes.push(new_node);
                    break;
                case ExprReaderResponses.FINISHED_READING:
                    break;
                case ExprReaderResponses.ERROR_STRING_DOESNT_END:
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

    // если строка имеет вид funName(...), то выводит длину fun_name, иначе -1
    private int funcPrefSkip(char[] char_array) {
        int len = char_array.length;
        if (len == 0 || char_array[len - 1] != ')') return -1;
        int idx = 0;
        if (!Character.isLetter(char_array[0])) return -1;
        while (Character.isLetterOrDigit(char_array[idx])) ++idx;
        if (char_array[idx] != '(') return -1;
        int res = idx;

        // еще надо проверить "правильность" правильной скобочной последовательности, то есть что у нас последняя скобка действительно соответствует именно первой, а не какой-то ещё"
        ++idx;
        int used_quotes = 0;
        int opened = 1;
        while (idx < len) {
            if (char_array[idx] == '\"') ++used_quotes; // внутри строк может происходить что угодно
            else if (used_quotes % 2 == 0 && char_array[idx] == '(') ++opened;
            else if (used_quotes % 2 == 0 && char_array[idx] == ')') --opened;
            ++idx;
            if (idx < len && opened == 0) return -1;
        }
        if (opened != 0) return -1;
        return res;
    }

    public FunNode parseFunCall(String str) { // smth like: funName(...);
        char[] char_array = str.trim().toCharArray();
        int res = funcPrefSkip(char_array);
        if (res == -1) return new FunNode("incorrect function call");

        String fun_name = String.valueOf(char_array, 0, res);
        if (Functions == null || Functions.get(fun_name) == null) return new FunNode("function not found");
        
        FunctionInfo f_info = Functions.get(fun_name);


        int idx = res + 1;
        int cur = idx;
        int opened_quotes = 0, opened_brackets = 0; // dont count the outer brackets now
        List<ExprOrCallNode> params = new ArrayList<>();
        while (idx < char_array.length) {
            char c  = char_array[idx];
            if (c == '\"') ++opened_quotes;
            else if (opened_quotes % 2 == 0) {
                if (c == '(') ++opened_brackets;
                else if (c == ')' && opened_brackets > 0) --opened_brackets;
                else if ((c == ',' || c == ')') && opened_brackets == 0) {
                    if (idx == cur) {
                        if (cur == res + 1) return new FunNode(f_info, params); // no params
                        else return new FunNode("incorrect function call"); // ",)" construction
                    } 
                    String between = String.valueOf(char_array, cur, idx - cur);
                    if (funcPrefSkip(between.trim().toCharArray()) != -1) {
                        FunNode fn = parseFunCall(between);
                        params.add(fn);
                    } else {
                        ExprOrCallNode en = parseExpr(between);
                        params.add(en);
                    }
                    cur = idx + 1;
                } 
            }
            ++idx;
        } 
        
        return new FunNode(f_info, params);
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
            return new AssnNode(Expr.ErrorExpr, "Instruction must end with \";\"", null);
        }

        String ret = "return";
        if (ret_type == FunctionReturnType.VOID) {
            boolean only_spaces = true;
            for (int idx = ret.length(); idx < str.length() - 1; ++idx) {
                if (!Character.isWhitespace(str.charAt(idx))) { only_spaces = false; break; }
            }
            if (only_spaces) return new RetNode(null);
        }

        if (str.startsWith(ret) && Character.isWhitespace(str.charAt(ret.length()))) {
            if (!accepts_return_instructions) {
                return new RetNode(new ExprNode(Expr.ErrorExpr, "cannot return"));
            }   

            ExprOrCallNode inner_node = parseExpr(str.substring(ret.length() + 1, str.length() - 1));
            RetNode ret_node = new RetNode(inner_node);
            if (ret_node.getExpression() instanceof FunNode && ((FunNode)ret_node.getExpression()).getFunc() == null) {
                return new RetNode(new ExprNode(Expr.ErrorExpr, "function not found"));
            }
            if (ret_node.returnType() != ret_type) {
                return new RetNode(new ExprNode(Expr.ErrorExpr, "return type mismatch"));
            }

            return ret_node;
        }

        if (funcPrefSkip(str.substring(0, str.length() - 1).toCharArray()) != -1) {
            return new InstrFunCallNode(parseFunCall(str.substring(0, str.length() - 1)));
        }

        int idx = 0;
        char[] char_array = str.toCharArray();
        while (Character.isWhitespace(char_array[idx])) ++idx;
        if (!Character.isLetter(char_array[idx])) {
            return new AssnNode(Expr.ErrorExpr, "Instruction cannot start with not a letter", null);
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
            if (Variables.get(var_name) == null) {
                return new AssnNode(Expr.ErrorExpr, "Variable should have been initialized before", null);
            }
        } else { // 1st case: Type x := expression;
            while (Character.isWhitespace(char_array[idx])) ++idx;
            if (!Character.isLetter(char_array[idx])) {
                return new AssnNode(Expr.ErrorExpr, "Variable name cannot start with not a letter", null);
            }
            while (Character.isLetterOrDigit(char_array[idx])) {
                var_name += char_array[idx];
                ++idx;
            }

            if (Constants.TYPE_NAMES.contains(var_name)) {
                return new AssnNode(Expr.ErrorExpr, "Variable name cannot be a type name", null);
            }

            if (Variables.get(var_name) != null) {
                return new AssnNode(Expr.ErrorExpr, "Variable was initialized before", null);
            }
        }

        while (Character.isWhitespace(char_array[idx])) ++idx;
        if (char_array[idx] != ':' || char_array[idx + 1] != '=') {
            return new AssnNode(Expr.ErrorExpr, "Expected :=, but not found", null);
        }
        idx += 2;

        String expression = new String(char_array, idx, char_array.length - idx - 1);
        ExprOrCallNode expr;
        if (funcPrefSkip(expression.toCharArray()) != -1) {
            expr = parseFunCall(expression);
        } else {
            expr = parseExpr(expression);
        }
        Expr type_of_expr_expected;
        if (type_declared) type_of_expr_expected = Constants.get_expr(first_word);
        else type_of_expr_expected = (Variables.get(var_name) == Type.INTEGER ? Expr.IntExpr : Expr.StringExpr);

        if (expr.getType() != type_of_expr_expected) {
            return new AssnNode(Expr.ErrorExpr, "Expression expected to be " + type_of_expr_expected + " but was " + expr.getType(), null);
        }

        if (type_declared) {
            if (Functions != null && Functions.get(var_name) != null) {
                return new AssnNode(Expr.ErrorExpr, var_name + " is a function", null);
            }
            if (Variables == null) Variables = new HashMap<>();
            if (type_of_expr_expected == Expr.IntExpr) Variables.put(var_name, Type.INTEGER);
            else Variables.put(var_name, Type.STRING);
        }
        
        return new AssnNode(type_of_expr_expected, var_name, expr);
    }

    private int skipWord(char[] char_array, int idx, String keyword) {
        for (int i = idx; i < idx + keyword.length(); ++i) {
            if (i >= char_array.length || char_array[i] != keyword.charAt(i - idx)) return -1;
        }
        return idx + keyword.length();
    }

    public Pair<TextReaderResponses, List<Node>> parseCommands(String str) {
        char[] char_array = str.trim().toCharArray();
        int len = char_array.length;
        if (len == 0) return new Pair<>(TextReaderResponses.OK, new ArrayList<>()); // empty 
        
        List<Node> result = new ArrayList<>();
        
        Stack<Parser> parsers = new Stack<>();
        Stack<ExprOrCallNode> stmts = new Stack<>();
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
                                
                                ExprOrCallNode expr_node = expr_parser.parseExpr(String.copyValueOf(char_array, skip_if, idx - skip_if));
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
                        else_node_lists.pop(); // pop the null
                        else_node_lists.push(new ArrayList<>()); // add new array list instead of it
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
                    parsers.pop();
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

        if (!stmts.empty()) return new Pair<>(TextReaderResponses.NOT_CLOSED_IF, result); 

        return new Pair<>(TextReaderResponses.OK, result);
    }

    public FunctionDeclareNode parseFunDefinition(String str) {
        assert str.startsWith("def");
        assert Character.isWhitespace(str.charAt(3));
        assert str.endsWith("enddef");
        assert Character.isWhitespace(str.charAt(str.length() - 7));

        int idx = 4;
        while (Character.isWhitespace(str.charAt(idx))) ++idx;
        
        FunctionReturnType frt;
        if (str.substring(idx, idx + 3).equals("int")) {frt = FunctionReturnType.INT;idx+=3;}
        else if (str.substring(idx, idx + 3).equals("str")) {frt = FunctionReturnType.STR;idx+=3;}
        else if (str.substring(idx, idx + 4).equals("void")) {frt = FunctionReturnType.VOID;idx+=4;}
        else return new FunctionDeclareNode("unknown return type");
        if (!Character.isWhitespace(str.charAt(idx))) return new FunctionDeclareNode("unknown return type"); // например intv funName(...), то есть после названия типа без побела дальше что-то
        while (Character.isWhitespace(str.charAt(idx))) ++idx; 
        
        int start_name_idx = idx;
        if (!Character.isLetter(str.charAt(start_name_idx))) return new FunctionDeclareNode("function name starts with not a letter");
        
        while (idx < str.length() && Character.isLetterOrDigit(str.charAt(idx))) ++idx;
        if (idx == str.length() || str.charAt(idx) != '(') return new FunctionDeclareNode("'(' should be after function name");
        
        String fun_name = str.substring(start_name_idx, idx);
        if (Constants.KEYWORDS.contains(fun_name)) return new FunctionDeclareNode("invalid function name");

        ++idx;
        while (Character.isWhitespace(str.charAt(idx))) ++idx;
        int params_start_idx = idx;
        while (idx < str.length() && str.charAt(idx) != ')') ++idx;
        if (idx == str.length()) return new FunctionDeclareNode("')' not found");
        List<Type> types = new ArrayList<>();
        List<String> varnames = new ArrayList<>();
        if (params_start_idx < idx) {
            String[] declarations = str.substring(params_start_idx, idx).split(",");
            for (int d = 0; d < declarations.length; ++d) {
                String[] decl = declarations[d].trim().split(" ");
                if (decl.length != 2) return new FunctionDeclareNode("incorrect declaration of varnames");
                else if (!Constants.TYPE_NAMES.contains(decl[0])) return new FunctionDeclareNode("type " + decl[0] + "does not exist");
                types.add(Constants.type_from_str(decl[0]));
                if (Constants.KEYWORDS.contains(decl[1])) return new FunctionDeclareNode("varname cannot be a keyword");
                else if (Variables != null && Variables.containsKey(decl[1])) return new FunctionDeclareNode("varname already used");
                else if (Functions != null && Functions.containsKey(decl[1])) return new FunctionDeclareNode("varname already used as a function");
                varnames.add(decl[1]);
            }
        }

        FunctionInfo f_info = new FunctionInfo(fun_name, types, frt);

        Parser in_function_parser = clone();
        in_function_parser.enableReturnInstructions(frt);

        if (in_function_parser.Functions == null) in_function_parser.Functions = new HashMap<>();
        in_function_parser.Functions.put(fun_name, f_info);

        if (in_function_parser.Variables == null) in_function_parser.Variables = new HashMap<>();
        for (int i = 0; i < types.size(); ++i) {
            if (in_function_parser.Variables.containsKey(varnames.get(i))) return new FunctionDeclareNode("repeating varnames");
            in_function_parser.Variables.put(varnames.get(i), types.get(i));
        }
        

        ++idx; // shift after ')'

        Pair<TextReaderResponses, List<Node>> inner_parse_res = in_function_parser.parseCommands(str.substring(idx, str.length() - 7));
        if (inner_parse_res.first() != TextReaderResponses.OK) {
            return new FunctionDeclareNode("inner parsing ended with error " + inner_parse_res.first());
        }

        
        if (this.Functions == null) this.Functions = new HashMap<>();
        this.Functions.put(fun_name, f_info);
        
        return new FunctionDeclareNode(f_info, inner_parse_res.second(), varnames);
    }
}

package src;
import java.util.HashSet;

/*
 * Строит синтаксический разбор инструкции присваивания
 * Инструкции присваивания могут быть двух видов
 * 1) Type x := Expression;
 * -- где Expression -- выражение,
 * задаваемое Node и распознаваемое с помощью Parser, при этом переменная x еще не была объявлена
 * Вместо Type может быть int либо str (целочисленный тип либо строковый)
 * При этом проверяем, что Expression звдвёт тот же тип, что и Type
 * 2) x := Expression;
 * -- то же самое, но переменная x была уже объявлена.
 * При этом запрещаем переменной x менять тип (то есть если она была типа str,
 * а Expression задает целое число, то это ошибка)
 */
public class InstrParser{
    private HashSet<String> Integers;
    private HashSet<String> Strings;

    public InstrParser() {
        this.Integers = new HashSet<>();
        this.Strings = new HashSet<>();
    }

    public InstrParser(HashSet<String> _Integers, HashSet<String> _Strings) {
        this.Integers = _Integers;
        this.Strings = _Strings;
    }

    public InstrNode parseInstr(String str) {
        if (!str.endsWith(";")) { // instruction must end with 
            return new InstrNode(Expr.ErrorExpr, "Instruction must end with \";\"", new Node(Expr.ErrorExpr, ""));
        }

        int idx = 0;
        char[] char_array = str.toCharArray();
        while (Character.isWhitespace(char_array[idx])) ++idx;
        if (!Character.isLetter(char_array[idx])) {
            return new InstrNode(Expr.ErrorExpr, "Instruction cannot start with not a letter", new Node(Expr.ErrorExpr, ""));
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

        /* TODO: get the expression */

        if (type_declared) {
            Expr type_of_expr_expected = Constants.get_type(first_word);
            /* TODO: check types */
            if (type_of_expr_expected == Expr.IntExpr) Integers.add(var_name);
            else Strings.add(var_name);
        }
        

        return new InstrNode(null, str, null);
    }
    
}

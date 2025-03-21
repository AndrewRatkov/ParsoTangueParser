package src.consts;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Constants{
    public static final List<String> TYPE_NAMES = Arrays.asList("int", "str");
    public static final List<String> KEYWORDS = Arrays.asList("int", "str", "fi", "if", "then", "else", "return", "def", "enddef", "void");
    public static final String WHITESPACES = " \t\r\n";
    
    private static final Map<String, Expr> type_by_str = new HashMap<>();
    static {
        type_by_str.put("int", Expr.IntExpr);
        type_by_str.put("str", Expr.StringExpr);
    }

    public static Expr get_expr(String s) {
        return type_by_str.get(s);
    }

    public static Type type_from_str(String s) {
        if (s.equals("int")) return Type.INTEGER;
        else return Type.STRING;
    }

    public static Expr get_expr_from_type(Type t) {
        if (t == Type.INTEGER) return Expr.IntExpr;
        else return Expr.StringExpr;
    }

    public static Expr get_expr_from_function_return_type(FunctionReturnType t) {
        if (t == FunctionReturnType.INT) return Expr.IntExpr;
        else if ((t == FunctionReturnType.STR)) return Expr.StringExpr;
        return Expr.ErrorExpr;
    }

    public static FunctionReturnType get_function_return_type_from_expr(Expr e) {
        if (e == Expr.IntExpr) return FunctionReturnType.INT;
        else return FunctionReturnType.STR;
    }
};

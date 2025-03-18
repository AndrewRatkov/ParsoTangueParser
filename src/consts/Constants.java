package src.consts;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Constants{
    public static final List<String> TYPE_NAMES = Arrays.asList("int", "str");
    public static final List<String> KEYWORDS = Arrays.asList("int", "str", "fi", "if", "then", "else");
    public static final String WHITESPACES = " \t\r\n";
    
    private static final Map<String, Expr> type_by_str = new HashMap<>();
    static {
        type_by_str.put("int", Expr.IntExpr);
        type_by_str.put("str", Expr.StringExpr);
    }

    public static Expr get_expr(String s) {
        return type_by_str.get(s);
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
};

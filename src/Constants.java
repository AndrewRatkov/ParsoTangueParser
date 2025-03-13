package src;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Constants{
    public static final List<String> TYPE_NAMES = Arrays.asList("int", "str");
    public static final List<String> KEYWORDS = Arrays.asList("int", "str");
    public static final String WHITESPACES = " \t\r\n";
    
    private static final Map<String, Expr> type_by_str = new HashMap<>();
    static {
        type_by_str.put("int", Expr.IntExpr);
        type_by_str.put("str", Expr.StringExpr);
    }

    public static Expr get_type(String s) {
        return type_by_str.get(s);
    }
};

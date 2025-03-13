package src;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/*
 * Определение всех используемых бинарных операторов с их приоритетами
 */
public class BinopConstants {
    public static final Binop EQ = new Binop("==", 0);
    public static final Binop NEQ = new Binop("!=", 0);

    public static final Binop G = new Binop('>', 1);
    public static final Binop L = new Binop('<', 1);
    public static final Binop GE = new Binop(">=", 1);
    public static final Binop LE = new Binop("<=", 1);

    public static final Binop ADD = new Binop('+', 2);
    public static final Binop SUB = new Binop('-', 2);

    public static final Binop MUL = new Binop('*', 3);
    public static final Binop DIV = new Binop('/', 3);
    public static final Binop PER = new Binop('%', 3);

    public static final String BINOP_FIRST_CHARS = "=!<>+-*/%";
    public static final String SHORT_BINOPS = "<>+-*/%";
    public static final List<String> LONG_BINOPS = Arrays.asList("==", ">=", "<=", "!=");

    public static final int MAX_PROPRITY = 3;
    public static final int MIN_PROPRITY = 0;
    
    private static final Map<String, Binop> binop_by_operator = new HashMap<>();

    static  {
        binop_by_operator.put("==", EQ);
        binop_by_operator.put("!=", NEQ);
        
        binop_by_operator.put(">", G);
        binop_by_operator.put("<", L);
        binop_by_operator.put(">=", GE);
        binop_by_operator.put("<=", LE);
        
        binop_by_operator.put("+", ADD);
        binop_by_operator.put("-", SUB);
        
        binop_by_operator.put("*", MUL);
        binop_by_operator.put("/", DIV);
        binop_by_operator.put("%", PER);
    }

    public static Binop get_by_string(String s) {
        return binop_by_operator.get(s);
    }
}

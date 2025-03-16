package src.consts;


/*
 * Бинарный оператор. Состоит из массива из одного или двух char'ов и целого числа -- приоритета оператора
 */
public class Binop {
        public char[] operator;
        public int priority;

        public Binop(char[] operator, int priority) {
            this.operator = operator;
            this.priority = priority;
        }

        public Binop(char operator, int priority) {
            this.operator = String.valueOf(operator).toCharArray();
            this.priority = priority;
        }

        public Binop(String operator, int priority) {
            this.operator = operator.toCharArray();
            this.priority = priority;
        }

        @Override
        public String toString() {
            return "Binop{ operator=\"" + String.valueOf(operator) + "\", priority=" + priority + " }";
        }
}

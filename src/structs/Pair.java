package src.structs;
import java.util.Objects;

/* Класс, чтобы можно было заводить пары */
public class Pair<K, V> {
    private final K first;
    private final V second;

    public Pair(K key, V value) {
        this.first = key;
        this.second = value;
    }

    public K first() {
        return first;
    }

    public V second() {
        return second;
    }

    @Override
    public String toString() {
        return first.toString() + " " + second.toString(); 
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
    }

    public boolean isSame(K key, V val) {
        return first.equals(key) && second.equals(val);
    }
}

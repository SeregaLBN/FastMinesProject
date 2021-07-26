package fmg.common;

import java.util.Objects;

public class Pair<A, B> {

    public final A first;
    public final B second;

    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public String toString() {
        return "Pair[" + first + "," + second + "]";
    }

    @Override
    public int hashCode() {
        int result = 31 + ((first == null) ? 0 : first.hashCode());
        return 31 * result + ((second == null) ? 0 : second.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Pair<?, ?> other = (Pair<?, ?>)obj;
        return Objects.equals(first, other.first) &&
               Objects.equals(second, other.second);
    }

}
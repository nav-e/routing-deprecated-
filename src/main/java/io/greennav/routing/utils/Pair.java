package io.greennav.routing.utils;

public class Pair<K, V> {
    public final K first;
    public final V second;

    public Pair(K first, V second) {
        this.first = first;
        this.second = second;
    }

    public K getKey() {
        return this.first;
    }

    public V getValue() {
        return this.second;
    }

    @Override
    public int hashCode() {
        return first.hashCode() + second.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Pair))
            return false;
        return ((Pair) obj).getKey().equals(first)
                && ((Pair) obj).getValue().equals(second);
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ')';
    }
}
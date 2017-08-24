package io.greennav.routing.utils;

import java.util.Comparator;

public class QueueEntry<K extends Number, V> implements Comparator<QueueEntry<K, V>>, Comparable<QueueEntry<K, V>> {
    public K key;
    public V value;

    public QueueEntry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public int compare(QueueEntry<K, V> lhs, QueueEntry<K, V> rhs) {
        return lhs.key.intValue() - rhs.key.intValue();
    }

    public int compareTo(QueueEntry<K, V> other) {
        return compare(this, other);
    }
}

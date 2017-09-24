package edu.berkeley.nlp.assignments.assign1.student;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache<K, V> extends LinkedHashMap<K, V> {
	private int cacheSize;

	public LRUCache(int cacheSize) {
		super(16,0.75f,true);
		this.cacheSize = cacheSize;
	}

	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return size() >= cacheSize;
	}
}

// public class LRUCache {
// class Node {
// long key;
// double value;
// Node pre;
// Node next;
//
// public Node(long key, double value) {
// this.key = key;
// this.value = value;
// }
// }
//
// int capacity;
// HashMap<Long, Node> map = new HashMap<Long, Node>();
// Node head = null;
// Node end = null;
//
// public LRUCache(int capacity) {
// this.capacity = capacity;
// }
//
// public double get(long key) {
// if (map.containsKey(key)) {
// Node n = map.get(key);
// remove(n);
// setHead(n);
// return n.value;
// }
//
// return -1;
// }
//
// public void remove(Node n) {
// if (n.pre != null) {
// n.pre.next = n.next;
// } else {
// head = n.next;
// }
//
// if (n.next != null) {
// n.next.pre = n.pre;
// } else {
// end = n.pre;
// }
//
// }
//
// public void setHead(Node n) {
// n.next = head;
// n.pre = null;
//
// if (head != null)
// head.pre = n;
//
// head = n;
//
// if (end == null)
// end = head;
// }
//
// public void set(long key, double value) {
// if (map.containsKey(key)) {
// Node old = map.get(key);
// old.value = value;
// remove(old);
// setHead(old);
// } else {
// Node created = new Node(key, value);
// if (map.size() >= capacity) {
// map.remove(end.key);
// remove(end);
// setHead(created);
//
// } else {
// setHead(created);
// }
//
// map.put(key, created);
// }
// }
// }

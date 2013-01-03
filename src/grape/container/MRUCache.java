package grape.container;

import java.util.HashMap;
import java.util.Map;

/**
 * most recently used cache
 *
 * @author jingqi
 *
 * @param <K>
 * @param <V>
 */
public class MRUCache <K,V> {

	private class Node {
		K key;
		V value;
		Node pre, next;

		Node(K k, V v) {
			key = k;
			value = v;
		}
	}

	private static final int DEFAULT_CAPACITY = 50;

	private final int capacity;
	private final Map<K,Node> map;
	private final Node list;

	public MRUCache(int cap) {
		assert cap > 0 : cap;
		capacity = cap;
		map = new HashMap<K,Node>();
		list = new Node(null,null); // 哨兵节点
		list.pre = list;
		list.next = list;
	}

	public MRUCache() {
		this(DEFAULT_CAPACITY);
	}

	public synchronized V put(K k, V v) {
		assert k != null;
		if (v == null)
			return remove(k);

		Node n = map.get(k);
		V ret = null;
		if (n == null) {
			n = new Node(k,v);
			map.put(k, n);
			while (map.size() > capacity)
				remove(list.pre.key);
		} else {
			ret = n.value;
			n.value = v;
			removeListNode(n);
		}
		pushListHead(n);
		return ret;
	}

	public synchronized V remove(K k) {
		assert k != null;
		Node n = map.remove(k);
		if (n == null)
			return null;
		removeListNode(n);
		return n.value;
	}

	public synchronized V get(K k) {
		assert k != null;
		Node n = map.get(k);
		if (n == null)
			return null;
		return n.value;
	}

	public synchronized void clear() {
		map.clear();
		list.pre = list;
		list.next = list;
	}

	private void removeListNode(Node n) {
		assert n != null && n != list;
		n.pre.next = n.next;
		n.next.pre = n.pre;
	}

	private void pushListHead(Node n) {
		assert n != null && n != list;
		n.next = list.next;
		n.pre = list;
		list.next.pre = n;
		list.next = n;
	}
}

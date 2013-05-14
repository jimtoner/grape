package grape.util;

import java.util.HashMap;
import java.util.Map;

/**
 * most recently used cache
 *
 * @author jingqi
 *
 * @param <K> Key type
 * @param <V> Value type
 */
public class MRUCache <K,V> {

	private static class Node <K,V> {
		K key;
		V value;
		Node<K,V> pre, next;

		Node(K k, V v) {
			key = k;
			value = v;
		}
	}

	// 默认容量
	private static final int DEFAULT_CAPACITY = 50;

	private final Map<K, Node<K, V> > map;
	private final Node<K, V> list; // 所有node组成环形链表，head为MRU，tail为LRU
	private int capacity;

	public MRUCache(int cap) {
		if (cap < 1)
			throw new IllegalArgumentException();

		capacity = cap;
		map = new HashMap<K, Node<K,V> >();
		list = new Node<K,V>(null, null); // 哨兵节点 NIL
		list.pre = list;
		list.next = list;
	}

	public MRUCache() {
		this(DEFAULT_CAPACITY);
	}

	/**
	 * 从缓存中获取值
	 *
	 * @return null if miss
	 * 		Non-null if hit
	 */
	public synchronized V get(K k) {
		Node<K, V> n = map.get(k);
		if (n == null)
			return null;

		// hit, then move the node to head
		removeNode(n);
		pushHead(n);

		return n.value;
	}

	/**
	 * 添加
	 *
	 * @return 被替换或者丢弃的数据
	 */
	public synchronized V put(K k, V v) {
		if (v == null)
			return remove(k);

		// 更新 cache
		Node<K, V> n = map.get(k);
		V ret = null;
		if (n == null) {
			n = new Node<K,V>(k,v);
			map.put(k, n);
			for (int i = map.size(); i > capacity; --i)
				ret = remove(list.pre.key); // remove tail
		} else {
			ret = n.value;
			n.value = v;
			removeNode(n);
		}
		pushHead(n);
		return ret;
	}

	public synchronized V remove(K k) {
		Node<K, V> n = map.remove(k);
		if (n == null)
			return null;
		removeNode(n);
		return n.value;
	}

	public synchronized void clear() {
		map.clear();
		list.pre = list;
		list.next = list;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int cap) {
		if (cap < 1)
			throw new IllegalArgumentException();
		capacity = cap;
	}

	// 摘除节点
	private void removeNode(Node<K,V> n) {
		n.pre.next = n.next;
		n.next.pre = n.pre;
	}

	// 插入到链首
	private void pushHead(Node<K,V> n) {
		n.next = list.next;
		n.pre = list;
		list.next.pre = n;
		list.next = n;
	}
}

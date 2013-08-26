package grape.container.skiplist;

import java.util.*;

/**
 * 跳表
 *
 * 跳表查询、插入、删除操作的时间复杂度为 O(log(n))，空间复杂度为O(n)
 * 平衡二叉树查询、插入、删除操作的时间复杂度为 O(log(n))，空间复杂度为O(n)
 * 但跳表相比于平衡二叉树，其插入删除较容易，也容易实现lock-free
 */
@SuppressWarnings({ "rawtypes", "unchecked"} )
public class SkipListSet <K extends Comparable<K> > extends AbstractSet<K> implements Set <K> {

	private static class Node <K> {
		K key;
		Node<K>[] next;

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof Node))
				return false;
			Node n = (Node) o;
			if (n.key == key || (n.key != null && n.key.equals(key)))
				return true;
			return false;
		}

		@Override
		public int hashCode() {
			return (key == null ? 17 : key.hashCode());
		}
	}

	private Node<K>[] head = new Node[1];
	private int size = 0;

	@Override
	public int size() {
		return size;
	}

	/**
	 * @return >=1
	 */
	public int level() {
		int ret = head.length - 1;
		while (ret > 0 && head[ret] == null)
			--ret;
		return ret + 1;
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public Iterator<K> iterator() {
		return new Iterator<K>() {

			Node<K> current, next = head[0];

			@Override
			public boolean hasNext() {
				return next != null;
			}

			@Override
			public K next() {
				current = next;
				next = next.next[0];
				return current.key;
			}

			@Override
			public void remove() {
				SkipListSet.this.remove(current.key);
			}
		};
	}

	public void putAll(Set<? extends K> s) {
        for (K e : this)
            add(e);
	}

	@Override
	public void clear() {
		head = new Node[1];
		size = 0;
	}

	@Override
	public boolean contains(Object key) {
		return searchNode((Comparable<K>) key, null) != null;
	}

	/**
	 * 查找key所在的节点
	 *
	 * @param key 要查找的key
	 * @param pre_lv 可以为null，前向节点数组(长度与{@link #head}相同)
	 * @return 未找到则返回null
	 */
	private Node<K> searchNode(Comparable<K> key, Node<K>[] pre_lv) {
		Node<K> pre = null, ret = null;
		int lv = head.length - 1;
		do {
			while (true) {
				Node<K> n = (pre == null ? head[lv] : pre.next[lv]);
				if (n == null) {
					if (pre_lv != null)
						pre_lv[lv] = pre;
					break;
				}

				int cmp = key.compareTo(n.key);
				if (cmp < 0) {
					if (pre_lv != null)
						pre_lv[lv] = pre;
					break;
				} else if (cmp == 0) {
					if (pre_lv == null)
						return n;
					pre_lv[lv] = pre;
					ret = n;
					break; // 即使找到了节点，为了填充完前向节点数组，也需要继续查找下去
				} else {
					pre = n;
				}
			}
		} while (--lv >= 0);
		return ret;
	}

	/**
	 * 插入节点
	 *
	 * @param n 如果其 {@link Node#next} 为 NULL，则会随机生成level数。否则使用已有的{@link Node#next}数组
	 * @param pre_lv
	 */
	private void insertNode(Node<K> n, Node<K>[] pre_lv) {
        // random level
		if (n.next == null)
			n.next = new Node[SkipListMap.randomLevel() + 1];

        // adjust low-half level
        for (int i = 0; i < head.length && i < n.next.length; ++i) {
            if (pre_lv[i] == null) {
            	n.next[i] = head[i];
            	head[i] = n;
            } else {
                n.next[i] = pre_lv[i].next[i];
                pre_lv[i].next[i] = n;
            }
        }

        // adjust high-half level
        if (n.next.length > head.length) {
            Node<K>[] nh = new Node[n.next.length];
            System.arraycopy(head, 0, nh, 0, head.length);
            for (int i = head.length; i < n.next.length; ++i) {
                nh[i] = n;
                n.next[i] = null;
            }
            head = nh;
        }
	}

	/**
	 * 删除节点
	 */
	private void removeNode(Node<K> n, Node<K>[] pre_lv) {
		for (int i = 0; i < pre_lv.length && i < n.next.length; ++i) {
			if (pre_lv[i] == null)
				head[i] = n.next[i];
			else
				pre_lv[i].next[i] = n.next[i];
		}
	}

	@Override
	public boolean add(K key) {
		// search
		Node<K>[] pre_lv = new Node[head.length];
		Node<K> n = searchNode(key, pre_lv);
		if (n != null)
			return false;

		// increase total level
		n = new Node<K>();
		n.key = key;
		insertNode(n, pre_lv);
		++size;
		return true;
	}

	@Override
	public boolean remove(Object key) {
		// search
		Node<K>[] pre_lv = new Node[head.length];
		Node<K> n = searchNode((Comparable<K>) key, pre_lv);
		if (n == null)
			return false; // 没有找到元素

		// remove node
		removeNode(n, pre_lv);

		// 没有必要更改 head 的数组大小
		--size;
		return true;
	}

	@Override
	public SkipListSet<K> clone() {
		SkipListSet<K> ret = new SkipListSet<K>();
		int level = level();
		ret.head = new Node[level];
		Node<K>[] pre_lv = new Node[level];
		Node<K> n = head[0];
		while (n != null) {
			Node<K> c = new Node<K>();
			c.key = n.key;
			c.next = new Node[n.next.length];
			for (int i = 0; i < c.next.length; ++i) {
				if (pre_lv[i] == null)
					ret.head[i] = c;
				else
					pre_lv[i].next[i] = c;
				pre_lv[i] = c;
			}
		}
		ret.size = size;
		return ret;
	}

	@Override
	public String toString() {
		return toDebugString();
	}

	public String toDebugString() {
		int level = level();
		StringBuilder sb = new StringBuilder();
		sb.append("level: ").append(Integer.toString(level)).append("\n");

		Node<K> n = null;
		for (int i = 0; i < size; ++i) {
			n = (n == null ? head[0] : n.next[0]);
			for (int j = 0; j < level; ++j) {
				if (j < n.next.length)
					sb.append("○");
				else
					sb.append("|");
			}

			sb.append(" ").append(n.key).append("\n");
		}
		return sb.toString();
	}
}

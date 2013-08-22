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
		return getEntry(key) != null;
	}

	private Node<K> getEntry(Object key) {
		Comparable<K> k = (Comparable<K>) key;
		Node<K> pre = null;
		int lv = head.length - 1;
		do {
			while (true) {
				Node<K> n = (pre == null ? head[lv] : pre.next[lv]);
				if (n == null)
					break;

				int cmp = k.compareTo(n.key);
				if (cmp < 0)
					break;
				else if (cmp == 0)
					return n;
				else
					pre = n;
			}
		} while (--lv >= 0);
		return null;
	}

	@Override
	public boolean add(K key) {
		// search
		Comparable<K> k = (Comparable<K>) key;
		Node<K>[] pre_lv = new Node[head.length];
		Node<K> pre = null;
		int lv = head.length - 1;
		do {
			while (true) {
				Node<K> n = (pre == null ? head[lv] : pre.next[lv]);
				if (n == null) {
					pre_lv[lv] = pre;
					break;
				}

				int cmp = k.compareTo(n.key);
				if (cmp < 0) {
					pre_lv[lv] = pre;
					break;
				} else if (cmp == 0) {
					return false;
				} else {
					pre = n;
				}
			}
		} while (--lv >= 0);

		// increase total level
		Node<K> n = new Node<K>();
		int level = SkipListMap.randomLevel();
		if (level + 1 > head.length) {
			Node<K>[] nh = new Node[level + 1];
			System.arraycopy(head, 0, nh, 0, head.length);
			Arrays.fill(nh, head.length, level + 1, n);
			head = nh;
		}

		// insert node
		n.key = key;
		n.next = new Node[level + 1];
		for (int i = 0, len = pre_lv.length; i < len && i <= level; ++i) {
			Node<K> t;
			if (pre_lv[i] == null) {
				t = head[i];
				head[i] = n;
			} else {
				t = pre_lv[i].next[i];
				pre_lv[i].next[i] = n;
			}
			n.next[i] = t;
		}
		++size;
		return true;
	}

	@Override
	public boolean remove(Object key) {
		// search
		Comparable<K> k = (Comparable<K>) key;
		Node<K>[] pre_lv = new Node[head.length];
		Node<K> pre = null;
		boolean found = false;
		int lv = head.length - 1;
		do {
			while (true) {
				Node<K> n = (pre == null ? head[lv] : pre.next[lv]);
				if (n == null) {
					pre_lv[lv] = pre;
					break;
				}

				int cmp = k.compareTo(n.key);
				if (cmp < 0) {
					pre_lv[lv] = pre;
					break;
				} else if (cmp == 0) {
					pre_lv[lv] = pre;
					found = true;
					break; // 即使找到了节点，也需要继续找下去，以便找到剩余的前趋节点
				} else {
					pre = n;
				}
			}
		} while (--lv >= 0);
		if (!found)
			return false; // 没有找到元素

		// remove node
		Node<K> e = (pre_lv[0] == null ? head[0] : pre_lv[0].next[0]);
		int level = e.next.length;
		for (int i = 0, len = pre_lv.length; i < len && i < level; ++i) {
			if (pre_lv[i] == null)
				head[i] = e.next[i];
			else
				pre_lv[i].next[i] = e.next[i];
		}

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

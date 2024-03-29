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
public class SkipListMap <K extends Comparable<K>, V> extends AbstractMap<K,V> implements Map <K, V> {

	// 最大允许的层数(大于0)
	private static final int MAX_LEVEL = 16;

	// 升级层数的概率阀值
//	private static final double P = 0.5;

	// 随机数发生器
	private static final Random r = new Random();

	private static class Node <K,V> implements Map.Entry<K, V> {
		private K key;
		private V value;
		private Node<K,V>[] next;

		@Override
		public K getKey() {
			return key;
		}

		@Override
		public V getValue() {
			return value;
		}

		@Override
		public V setValue(V value) {
			V ret = this.value;
			this.value = value;
			return ret;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof Map.Entry))
				return false;
			Map.Entry n = (Map.Entry) o;
			Object k1 = getKey(), k2 = n.getKey();
			if (k1 == k2 || (k1 != null && k1.equals(k2))) {
				Object v1 = getValue(), v2 = n.getValue();
				if (v1 == v2 || (v1 != null && v1.equals(v2)))
					return true;
			}
			return false;
		}

		@Override
		public int hashCode() {
			return (key == null ? 0 : key.hashCode()) ^
					(value == null ? 0 : value.hashCode());
		}
	}

	private Node<K,V>[] head = new Node[1];
	private int size = 0;

	/**
	 * @return 0-based random level
	 */
	static int randomLevel() {
		/**
		 * 下面两种实现，概率上是相同的，50%几率返回0，25%返回1, 12.5%返回2...
		 *
		int lvl = (int)(Math.log(1.0 - Math.random()) / Math.log(1.0 - P));
	    return Math.min(lvl, MAX_LEVEL);
	     */

		int k = 0;
		while (k < MAX_LEVEL && r.nextBoolean()) // r = new Random();
			++k;
		return k;
	}

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
	public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet())
            put(e.getKey(), e.getValue());
	}

	@Override
	public void clear() {
		head = new Node[1];
		size = 0;
	}

	@Override
	public Set<K> keySet() {
		return new AbstractSet<K>() {

			@Override
			public Iterator<K> iterator() {
				return new Iterator<K>() {

					Node<K,V> current, next = head[0];

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
						SkipListMap.this.remove(current.key);
					}
				};
			}

			@Override
			public int size() {
				return SkipListMap.this.size();
			}

			@Override
			public boolean contains(Object o) {
				return SkipListMap.this.containsKey(o);
			}

			@Override
			public boolean remove(Object o) {
				return SkipListMap.this.remove(o) != null;
			}

			@Override
			public void clear() {
				SkipListMap.this.clear();
			}
		};
	}

	@Override
	public Collection<V> values() {
		return new AbstractCollection<V>() {

			@Override
			public Iterator<V> iterator() {
				return new Iterator<V>() {

					Node<K,V> current, next = head[0];

					@Override
					public boolean hasNext() {
						return next != null;
					}

					@Override
					public V next() {
						current = next;
						next = next.next[0];
						return current.value;
					}

					@Override
					public void remove() {
						SkipListMap.this.remove(current.key);
					}
				};
			}

			@Override
			public int size() {
				return SkipListMap.this.size();
			}

			@Override
			public boolean contains(Object o) {
				return SkipListMap.this.containsValue(o);
			}

			@Override
			public void clear() {
				SkipListMap.this.clear();
			}
		};
	}

	@Override
	public Set<Map.Entry<K,V> > entrySet() {
		return new AbstractSet<Map.Entry<K, V> >() {

			@Override
			public Iterator<Map.Entry<K, V> > iterator() {
				return new Iterator<Map.Entry<K,V> >() {

					Node<K,V> current, next = head[0];

					@Override
					public boolean hasNext() {
						return next != null;
					}

					@Override
					public Map.Entry<K, V> next() {
						current = next;
						next = next.next[0];
						return current;
					}

					@Override
					public void remove() {
						SkipListMap.this.remove(current.key);
					}
				};
			}

			@Override
			public int size() {
				return SkipListMap.this.size();
			}

			@Override
			public boolean contains(Object o) {
				if (!(o instanceof Map.Entry))
					return false;
				Map.Entry e = (Map.Entry) o;
				Map.Entry candidate = SkipListMap.this.searchNode((Comparable<K>) e.getKey(), null);
				return candidate != null && candidate.equals(e);
			}

			@Override
			public boolean remove(Object o) {
				return SkipListMap.this.removeMapping(o) != null;
			}

			@Override
			public void clear() {
				SkipListMap.this.clear();
			}
		};
	}

	@Override
	public boolean containsKey(Object key) {
		return get(key) != null;
	}

	@Override
	public boolean containsValue(Object value) {
		Node<K,V> n = head[0];
		while (n != null) {
			if (value == n.value || (value != null && value.equals(n.value)))
				return true;
			n = n.next[0];
		}
		return false;
	}

	@Override
	public V get(Object key) {
		Node<K,V> e = searchNode((Comparable<K>) key, null);
		return (e == null ? null : e.value);
	}

	/**
	 * 查找key所在的节点
	 *
	 * @param key 要查找的key
	 * @param pre_lv 可以为null，前向节点数组(长度与{@link #head}相同)
	 * @return 未找到则返回null
	 */
	private Node<K,V> searchNode(Comparable<K> key, Node<K,V>[] pre_lv) {
		Node<K,V> pre = null, ret = null;
		int lv = head.length - 1;
		do {
			while (true) {
				Node<K,V> n = (pre == null ? head[lv] : pre.next[lv]);
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
	private void insertNode(Node<K,V> n, Node<K,V>[] pre_lv) {
        // random level
		if (n.next == null)
			n.next = new Node[randomLevel() + 1];

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
            Node<K,V>[] nh = new Node[n.next.length];
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
	private void removeNode(Node<K,V> n, Node<K,V>[] pre_lv) {
		for (int i = 0; i < pre_lv.length && i < n.next.length; ++i) {
			if (pre_lv[i] == null)
				head[i] = n.next[i];
			else
				pre_lv[i].next[i] = n.next[i];
		}
	}

	@Override
	public V put(K key, V value) {
		// search
		Node<K,V>[] pre_lv = new Node[head.length];
		Node<K,V> n = searchNode(key, pre_lv);
		if (n != null) {
			V ret = n.value;
			n.value = value;
			return ret;
		}

		// insert node
		n = new Node<K,V>();
		n.key = key;
		n.value = value;
		insertNode(n, pre_lv);
		++size;
		return null;
	}

	@Override
	public V remove(Object key) {
		// search
		Node<K,V>[] pre_lv = new Node[head.length];
		Node<K,V> n = searchNode((Comparable<K>) key, pre_lv);
		if (n == null)
			return null;

		// remove node
		removeNode(n, pre_lv);

		// 没有必要更改 {@link #head} 的数组大小
		--size;
		return n.value;
	}

	private Node<K,V> removeMapping(Object o) {
		if (!(o instanceof Map.Entry))
			return null;
		Map.Entry<K, V> td = (Map.Entry<K, V>) o;

		// search
		Node<K,V>[] pre_lv = new Node[head.length];
		Node<K,V> n = searchNode((Comparable<K>) td.getKey(), pre_lv);
		if (n == null || !n.getValue().equals(td.getValue()))
			return null; // key或者value值不同

		// remove node
		removeNode(n, pre_lv);

		// 没有必要更改 head 的数组大小
		--size;
		return n;
	}

	@Override
	public SkipListMap<K, V> clone() {
		SkipListMap<K, V> ret = new SkipListMap<K, V>();
		int level = level();
		ret.head = new Node[level];
		Node<K,V>[] pre_lv = new Node[level];
		Node<K,V> n = head[0];
		while (n != null) {
			Node<K,V> c = new Node<K,V>();
			c.key = n.key;
			c.value = n.value;
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

		Node<K,V> n = null;
		for (int i = 0; i < size; ++i) {
			n = (n == null ? head[0] : n.next[0]);
			for (int j = 0; j < level; ++j) {
				if (j < n.next.length)
					sb.append("○");
				else
					sb.append("|");
			}

			sb.append(" ").append(n.key).append(":").append(n.value).append("\n");
		}
		return sb.toString();
	}
}

package grape.container.range;

import grape.container.binarytree.*;
import grape.container.binarytree.BinaryTree.TraversalIterator;
import grape.container.binarytree.RedBlackTree.RedBlackTreeOperationListener;
import grape.container.binarytree.node.BinaryTreeNode;
import grape.container.binarytree.node.RedBlackTreeNode;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 使用红黑树组织 range 列表
 *
 * @author jingqi
 */
public class RangeTree extends AbstractRangeContainer implements IndexedRangeContainer {

	private static class Node extends Range implements RedBlackTreeNode {

		private Node parent, left, right;
		private boolean red;

		private int size; // size = getLeftChild().getSize() + length() + getRightChild().getSize()

		public Node(int size, int firstValue, int lastValue) {
			super(firstValue, lastValue);
			this.size = size;
		}

		public int getSize() {
			return size;
		}

		public void setSize(int size) {
			this.size = size;
		}

		@Override
		public int compareTo(Object o) {
			if (!(o instanceof Range))
				throw new IllegalArgumentException();
			Range r = (Range) o;
			if (getFirstValue() > r.getLastValue())
				return 1;
			else if (getLastValue() < r.getFirstValue())
				return -1;
			return 0;
		}

		@Override
		public BinaryTreeNode getParent() {
			return parent;
		}

		@Override
		public void setParent(BinaryTreeNode p) {
			parent = (Node) p;
		}

		@Override
		public BinaryTreeNode getLeftChild() {
			return left;
		}

		@Override
		public void setLeftChild(BinaryTreeNode l) {
			left = (Node) l;
		}

		@Override
		public BinaryTreeNode getRightChild() {
			return right;
		}

		@Override
		public void setRightChild(BinaryTreeNode r) {
			right = (Node) r;
		}

		@Override
		public boolean isRed() {
			return red;
		}

		@Override
		public void setRed(boolean r) {
			red = r;
		}
	}

	/**
	 * 在红黑树插入、删除操作的同时，修正节点的 size 值
	 */
	private static final RedBlackTreeOperationListener listener = new RedBlackTreeOperationListener() {

		@Override
		public void rightRotated(RedBlackTreeNode n) {
			fixSize((Node) n);
			fixSize((Node) n.getParent());
		}

		@Override
		public void leftRotated(RedBlackTreeNode n) {
			fixSize((Node) n);
			fixSize((Node) n.getParent());
		}

		@Override
		public void disattachedFromTree(RedBlackTreeNode parent) {
			fixSizeUpToRoot((Node) parent);
		}

		@Override
		public void attachedToTree(RedBlackTreeNode attached) {
			fixSizeUpToRoot((Node) attached);
		}
	};

	private Node root;

	@Override
	public int getFirstValue() {
		if (root == null)
			throw new IllegalStateException("empty");
		Node n = (Node) BinarySearchTree.minimum(root);
		return n.getFirstValue();
	}

	@Override
	public int getLastValue() {
		if (root == null)
			throw new IllegalStateException("empty");
		Node n = (Node) BinarySearchTree.maximum(root);
		return n.getLastValue();
	}

	@Override
	public int size() {
		if (root == null)
			return 0;
		return root.getSize();
	}

	@Override
	public boolean isEmpty() {
		return root == null;
	}

	@Override
	public int getValue(int index) {
		if (index < 0 || index >= size())
			throw new IndexOutOfBoundsException();

		Node n = root;
		while (true) {
			// check left
			Node nn = (Node) n.getLeftChild();
			if (nn != null) {
				if (index < nn.getSize()) {
					n = nn;
					continue;
				} else {
					index -= nn.getSize();
				}
			}

			// check current
			if (index < n.length())
				return n.getFirstValue() + index;
			else
				index -= n.length();

			// check right
			n = (Node) n.getRightChild();
			assert n != null && index < n.getSize();
		}
	}

	@Override
	public boolean contains(int value) {
		return search(value) != null;
	}

	@Override
	public int indexOfValue(int value) {
		int index = 0;
		Node n = root;
		while (true) {
			if (n == null)
				return -1;

			if (value < n.getFirstValue()) {
				n = (Node) n.getLeftChild();
				continue;
			} else if (n.getLeftChild() != null) {
				index += ((Node) n.getLeftChild()).getSize();
			}

			if (value <= n.getLastValue()) {
				return index + value - n.getFirstValue();
			} else {
				index += n.length();
				n = (Node) n.getRightChild();
				continue;
			}
		}
	}

	/**
	 * 搜索值所在的节点
	 *
	 * @return 没有找到则返回 null
	 */
	private Node search(int value) {
		Node n = root;
		while (n != null) {
			if (value < n.getFirstValue())
				n = (Node) n.getLeftChild();
			else if (value > n.getLastValue())
				n = (Node) n.getRightChild();
			else
				return n;
		}
		return null;
	}

	/**
	 * 搜索值所在的节点
	 *
	 * @return 没有找到则返回其插入位置右侧的节点
	 */
	private Node searchToRight(int value) {
		Node n = root;
		while (true) {
			if (value < n.getFirstValue()) {
				if (n.getLeftChild() != null)
					n = (Node) n.getLeftChild();
				else
					break;
			} else if (value > n.getLastValue()) {
				if (n.getRightChild() != null) {
					n = (Node) n.getRightChild();
				} else {
					n = (Node) BinarySearchTree.successor(n);
					break;
				}
			} else {
				break;
			}
		}
		return n;
	}

	/**
	 * 搜索值所在的节点
	 *
	 * @return 没有找到则返回其插入位置左侧的节点
	 */
	private Node searchToLeft(int value) {
		Node n = root;
		while (true) {
			if (value < n.getFirstValue()) {
				if (n.getLeftChild() != null) {
					n = (Node) n.getLeftChild();
				} else {
					n = (Node) BinarySearchTree.predecessor(n);
					break;
				}
			} else if (value > n.getLastValue()) {
				if (n.getRightChild() != null)
					n = (Node) n.getRightChild();
				else
					break;
			} else {
				break;
			}
		}
		return n;
	}

	/**
	 * 修正节点的 size 值
	 */
	private static void fixSize(Node x) {
		int size = 0;
		if (x.getLeftChild() != null)
			size += ((Node) x.getLeftChild()).getSize();
		size += x.length();
		if (x.getRightChild() != null)
			size += ((Node) x.getRightChild()).getSize();
		x.setSize(size);
	}

	/**
	 * 修正节点及其祖先节点的 size 值
	 */
	private static void fixSizeUpToRoot(Node x) {
		while (x != null) {
			fixSize(x);
			x = (Node) x.getParent();
		}
	}

	@Override
	public void addValueRange(int firstValue, int lastValue) {
		if (firstValue > lastValue)
			throw new IllegalArgumentException();

		// 处理空节点的问题
		if (root == null) {
			root = new Node(lastValue - firstValue + 1, firstValue, lastValue);
			return;
		}

		// 定位 firstValue - 1
		Node n1 = searchToRight(firstValue - 1);

		// 定位 lastValue + 1
		Node n2 = searchToLeft(lastValue + 1);

		// 插入
		if (n1 != null && n2 != null && BinarySearchTree.successor(n2) != n1) {
			int min_first = Math.min(firstValue, n1.getFirstValue());
			int max_last = Math.max(lastValue, n2.getLastValue());
			root = (Node) RedBlackTree.removeRange(root, n1, n2, listener);
			Node n = new Node(0, min_first, max_last);
			root = (Node) RedBlackTree.insert(root, n, listener);
		} else {
			Node n = new Node(0, firstValue, lastValue);
			root = (Node) RedBlackTree.insert(root, n, listener);
		}
	}

	@Override
	public void removeValueRange(int firstValue, int lastValue) {
		if (firstValue > lastValue)
			throw new IllegalArgumentException();

		if (root == null)
			return;

		// 定位 firstValue
		Node n1 = searchToRight(firstValue);

		// 定位 lastValue
		Node n2 = searchToLeft(lastValue);

		// 删除区域
		if (n1 != null && n2 != null) {
			if (n1 == n2) {
				if (firstValue <= n1.getFirstValue() && lastValue >= n1.getLastValue()) {
					root = (Node) RedBlackTree.remove(root, n1, listener);
					return;
				} else if (firstValue > n1.getFirstValue() && lastValue < n1.getLastValue()) {
					Node n = new Node(0, lastValue + 1, n1.getLastValue());
					n1.setLastValue(firstValue - 1);
					fixSizeUpToRoot(n1);
					root = (Node) RedBlackTree.insert(root, n, listener);
					return;
				} else if (firstValue <= n1.getFirstValue()) {
					n1.setFirstValue(lastValue + 1);
					fixSizeUpToRoot(n1);
					return;
				} else {
					n1.setLastValue(firstValue - 1);
					fixSizeUpToRoot(n1);
					return;
				}
			} else if (BinarySearchTree.successor(n2) != n1) {
				if (n1.getFirstValue() < firstValue) {
					n1.setLastValue(firstValue - 1);
					fixSizeUpToRoot(n1);
					n1 = (Node) BinarySearchTree.successor(n1);
				}
				if (n2.getLastValue() > lastValue) {
					n2.setFirstValue(lastValue + 1);
					fixSizeUpToRoot(n2);
					n2 = (Node) BinarySearchTree.predecessor(n2);
				}
				if (n1 != null && n2 != null && BinarySearchTree.successor(n2) != n1) {
					root = (Node) RedBlackTree.removeRange(root, n1, n2, listener);
				}
			}
		}
	}

	@Override
	public RangeTree intersectWith(RangeContainer x) {
		RangeTree ret = new RangeTree();
		intersectWith(this, x, ret);
		return ret;
	}

	@Override
	public RangeTree mergeWith(RangeContainer x) {
		RangeTree ret = new RangeTree();
		mergeWith(this, x, ret);
		return ret;
	}

	@Override
	public RangeTree remainder(RangeContainer x) {
		RangeTree ret = new RangeTree();
		remainder(this, x, ret);
		return ret;
	}

	@Override
	public void clear() {
		root = null;
	}

	@Override
	public Iterator<Range> rangeIterator() {
		return new Iterator<Range>() {

			Iterator<BinaryTreeNode> iter = new TraversalIterator(root, RedBlackTree.TRAVERSAL_ORDER);

			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public Range next() {
				if (!hasNext())
					throw new NoSuchElementException();

				return (Node) iter.next();
			}

			@Override
			public void remove() {
				throw new RuntimeException("not supported");
			}
		};
	}

	@Override
	public Iterator<Range> rangeIterator(final int firstValue, final int lastValue) {
		return new Iterator<Range>() {

			Node next, last;

			{
				next = searchToRight(firstValue);
				last = searchToLeft(lastValue);
			}

			@Override
			public boolean hasNext() {
				return next != null;
			}

			@Override
			public Range next() {
				if (!hasNext())
					throw new NoSuchElementException();
				Node ret = next;
				if (next == last)
					next = null;
				else
					next = (Node) BinarySearchTree.successor(next);

				return ret;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	public boolean isValid() {
		Iterator<BinaryTreeNode> iter = new TraversalIterator(root, RedBlackTree.TRAVERSAL_ORDER);
		while (iter.hasNext()) {
			Node n = (Node) iter.next();
			if (n.getFirstValue() > n.getLastValue())
				return false;

			Node nn = (Node) BinarySearchTree.predecessor(n);
			if (nn != null) {
				if (n.getFirstValue() - 1 <= nn.getLastValue())
					return false;
			}

			int size = 0;
			if (n.getLeftChild() != null)
				size += ((Node) n.getLeftChild()).getSize();
			size += n.length();
			if (n.getRightChild() != null)
				size += ((Node) n.getRightChild()).getSize();
			if (size != n.getSize())
				return false;
		}
		return true;
	}

	@Override
	public RangeTree clone() {
		RangeTree ret = new RangeTree();
		Iterator<BinaryTreeNode> iter = new TraversalIterator(root, RedBlackTree.TRAVERSAL_ORDER);
		while (iter.hasNext()) {
			Node n = (Node) iter.next();
			ret.addValueRange(n.getFirstValue(), n.getLastValue());
		}
		return ret;
	}
}

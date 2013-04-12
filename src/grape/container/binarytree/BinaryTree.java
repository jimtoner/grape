package grape.container.binarytree;

import grape.container.binarytree.node.BinaryTreeNode;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 二叉树
 *
 * @author jingqi
 */
public class BinaryTree {

	private BinaryTree() {}

    /**
     * 中序遍历迭代器
     *
     *        B
     *       / \
     *      A   C
     */
	public static class InorderTraversalIterator implements Iterator<BinaryTreeNode> {

		final BinaryTreeNode parent_of_sub_tree;
		BinaryTreeNode next;

		public InorderTraversalIterator(BinaryTreeNode root_of_sub_tree) {
			if (root_of_sub_tree == null) {
				parent_of_sub_tree = null;
				next = null;
				return;
			}

			parent_of_sub_tree = root_of_sub_tree.getParent();
	        next = root_of_sub_tree;
	        while (null != next.getLeftChild())
	            next = next.getLeftChild();
		}

		public InorderTraversalIterator(BinaryTreeNode parent_of_sub_tree, BinaryTreeNode next) {
			this.parent_of_sub_tree = parent_of_sub_tree;
			this.next = next;
		}

		private void advance() {
            if (null != next.getRightChild()) {
            	next = next.getRightChild();
                while (null != next.getLeftChild())
                	next = next.getLeftChild();
            } else {
                BinaryTreeNode current = next, parent = next.getParent();
                while (parent_of_sub_tree != parent && current == parent.getRightChild()) {
                    current = parent;
                    parent = current.getParent();
                }
                if (parent_of_sub_tree == parent)
                    next = null; // the end
                else
                    next = parent;
            }
		}

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public BinaryTreeNode next() {
			if (!hasNext())
				throw new NoSuchElementException();
			BinaryTreeNode ret = next;
			advance();
			return ret;
		}

		@Override
		public void remove() {
			throw new RuntimeException("not supported");
		}
	}

    /**
     * 中序逆序遍历迭代器
     *
     *        B
     *       / \
     *      C   A
     */
	public static class InorderTraversalReverseIterator implements Iterator<BinaryTreeNode> {

		final BinaryTreeNode parent_of_sub_tree;
		BinaryTreeNode next;

		public InorderTraversalReverseIterator(BinaryTreeNode root_of_sub_tree) {
			if (root_of_sub_tree == null) {
				parent_of_sub_tree = null;
				next = null;
				return;
			}

	        parent_of_sub_tree = root_of_sub_tree.getParent();
	        next = root_of_sub_tree;
	        while (null != next.getRightChild())
	        	next = next.getRightChild();
		}

		public InorderTraversalReverseIterator(BinaryTreeNode parent_of_sub_tree, BinaryTreeNode next) {
			this.parent_of_sub_tree = parent_of_sub_tree;
			this.next = next;
		}

		private void advance() {
            if (null != next.getLeftChild()) {
                next = next.getLeftChild();
                while (null != next.getRightChild())
                    next = next.getRightChild();
            } else {
                BinaryTreeNode parent = next.getParent();
                while (parent_of_sub_tree != parent && next == parent.getLeftChild()) {
                    next = parent;
                    parent = next.getParent();
                }
                if (parent_of_sub_tree == parent)
                	next = null; // the end
                else
                	next = parent;
            }
		}

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public BinaryTreeNode next() {
			if (!hasNext())
				throw new NoSuchElementException();
			BinaryTreeNode ret = next;
			advance();
			return ret;
		}

		@Override
		public void remove() {
			throw new RuntimeException("not supported");
		}
	}

    /**
     * 前序遍历迭代器
     *
     *        A
     *       / \
     *      B   C
     */
	public static class PreorderTraversalIterator implements Iterator<BinaryTreeNode> {

		final BinaryTreeNode parent_of_sub_tree;
		BinaryTreeNode next;

		public PreorderTraversalIterator(BinaryTreeNode root_of_sub_tree) {
			if (root_of_sub_tree == null) {
				parent_of_sub_tree = null;
				next = null;
				return;
			}

	        parent_of_sub_tree = root_of_sub_tree.getParent();
	        next = root_of_sub_tree;
		}

		public PreorderTraversalIterator(BinaryTreeNode parent_of_sub_tree, BinaryTreeNode next) {
			this.parent_of_sub_tree = parent_of_sub_tree;
			this.next = next;
		}

		private void advance() {
            if (null != next.getLeftChild()) {
                next = next.getLeftChild();
            } else if (null != next.getRightChild()) {
                next = next.getRightChild();
            } else {
                BinaryTreeNode current = next, parent = next.getParent();
                while (parent_of_sub_tree != parent &&
                    (current == parent.getRightChild() || null == parent.getRightChild())) {
                    current = parent;
                    parent = current.getParent();
                }
                if (parent_of_sub_tree == parent)
                	next = null; // the end
                else
                    next = parent.getRightChild();
            }
		}

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public BinaryTreeNode next() {
			if (!hasNext())
				throw new NoSuchElementException();
			BinaryTreeNode ret = next;
			advance();
			return ret;
		}

		@Override
		public void remove() {
			throw new RuntimeException("not supported");
		}
	}

    /**
     * 前序逆序遍历迭代器
     *
     *        C
     *       / \
     *      B   A
     */
	public static class PreorderTraversalReverseIterator implements Iterator<BinaryTreeNode> {

		final BinaryTreeNode parent_of_sub_tree;
		BinaryTreeNode next;

		public PreorderTraversalReverseIterator(BinaryTreeNode root_of_sub_tree) {
			if (root_of_sub_tree == null) {
				parent_of_sub_tree = null;
				next = null;
				return;
			}

			parent_of_sub_tree = root_of_sub_tree.getParent();
			next = root_of_sub_tree;
	        while (null != next.getRightChild())
	        	next = next.getRightChild();
		}

		public PreorderTraversalReverseIterator(BinaryTreeNode parent_of_sub_tree, BinaryTreeNode next) {
			this.parent_of_sub_tree = parent_of_sub_tree;
			this.next = next;
		}

		private void advance() {
            BinaryTreeNode parent = next.getParent();
            if (parent == parent_of_sub_tree) {
            	next = null; // the end
            } else if (next == parent.getLeftChild() || null == parent.getLeftChild()) {
                next = parent;
            } else {
                next = parent.getLeftChild();
                while (true) {
                    if (null != next.getRightChild())
                        next = next.getRightChild();
                    else if (null != next.getLeftChild())
                        next = next.getLeftChild();
                    else
                        break;
                }
            }
		}

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public BinaryTreeNode next() {
			if (!hasNext())
				throw new NoSuchElementException();
			BinaryTreeNode ret = next;
			advance();
			return ret;
		}

		@Override
		public void remove() {
			throw new RuntimeException("not supported");
		}
	}

    /**
     * 后序遍历迭代器
     *
     *        C
     *       / \
     *      A   B
     */
	public static class PostorderTraversalIterator implements Iterator<BinaryTreeNode> {

		final BinaryTreeNode parent_of_sub_tree;
		BinaryTreeNode next;

		public PostorderTraversalIterator(BinaryTreeNode root_of_sub_tree) {
			if (root_of_sub_tree == null) {
				parent_of_sub_tree = null;
				next = null;
				return;
			}

			parent_of_sub_tree = root_of_sub_tree.getParent();
			next = root_of_sub_tree;
	        while (null != next.getLeftChild())
	            next = next.getLeftChild();
		}

		public PostorderTraversalIterator(BinaryTreeNode parent_of_sub_tree, BinaryTreeNode next) {
			this.parent_of_sub_tree = parent_of_sub_tree;
			this.next = next;
		}

		private void advance() {
            BinaryTreeNode parent = next.getParent();
            if (parent_of_sub_tree == parent) {
            	next = null; // the end
            } else if (next == parent.getRightChild() || null == parent.getRightChild()) {
            	next = parent;
            } else {
            	next = parent.getRightChild();
                while (true) {
                    if (null != next.getLeftChild())
                    	next = next.getLeftChild();
                    else if (null != next.getRightChild())
                    	next = next.getRightChild();
                    else
                        break;
                }
            }
		}

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public BinaryTreeNode next() {
			if (!hasNext())
				throw new NoSuchElementException();
			BinaryTreeNode ret = next;
			advance();
			return ret;
		}

		@Override
		public void remove() {
			throw new RuntimeException("not supported");
		}
	}


    /**
     * 后序逆序遍历迭代器
     *
     *        A
     *       / \
     *      C   B
     */
	public static class PostorderTraversalReverseIterator implements Iterator<BinaryTreeNode> {

		final BinaryTreeNode parent_of_sub_tree;
		BinaryTreeNode next;

		public PostorderTraversalReverseIterator(BinaryTreeNode root_of_sub_tree) {
			if (root_of_sub_tree == null) {
				parent_of_sub_tree = null;
				next = null;
				return;
			}

			parent_of_sub_tree = root_of_sub_tree.getParent();
			next = root_of_sub_tree;
		}

		public PostorderTraversalReverseIterator(BinaryTreeNode parent_of_sub_tree, BinaryTreeNode next) {
			this.parent_of_sub_tree = parent_of_sub_tree;
			this.next = next;
		}

		private void advance() {
            if (null != next.getRightChild()) {
                next = next.getRightChild();
            } else if (null != next.getLeftChild()) {
                next = next.getLeftChild();
            } else {
                BinaryTreeNode parent = next.getParent();
                while (parent_of_sub_tree != parent &&
                    (next == parent.getLeftChild() || null == parent.getLeftChild())) {
                    next = parent;
                    parent = next.getParent();
                }
                if (parent_of_sub_tree == parent)
                	next = null;
                else
                	next = parent.getLeftChild();
            }
		}

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public BinaryTreeNode next() {
			if (!hasNext())
				throw new NoSuchElementException();
			BinaryTreeNode ret = next;
			advance();
			return ret;
		}

		@Override
		public void remove() {
			throw new RuntimeException("not supported");
		}
	}
}

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
	 * 二叉树遍历的顺序
	 */
	public static enum TraversalOrder {

	    /**
	     * 前序遍历
	     *
	     *        A
	     *       / \
	     *      B   C
	     */
		Preorder,

	    /**
	     * 前序逆序遍历
	     *
	     *        C
	     *       / \
	     *      B   A
	     */
		PreorderReverse,

	    /**
	     * 中序遍历
	     *
	     *        B
	     *       / \
	     *      A   C
	     */
		Inorder,

	    /**
	     * 中序逆序遍历
	     *
	     *        B
	     *       / \
	     *      C   A
	     */
		InorderReverse,

	    /**
	     * 后序遍历
	     *
	     *        C
	     *       / \
	     *      A   B
	     */
		Postorder,

	    /**
	     * 后序逆序遍历
	     *
	     *        A
	     *       / \
	     *      C   B
	     */
		PostorderReverse;

		/**
		 * 取相反的方向
		 */
		public TraversalOrder oppositeOrder() {
			switch (this) {
			case Preorder:
				return PreorderReverse;

			case PreorderReverse:
				return Preorder;

			case Inorder:
				return InorderReverse;

			case InorderReverse:
				return Inorder;

			case Postorder:
				return PostorderReverse;

			case PostorderReverse:
				return Postorder;

			default:
				throw new IllegalArgumentException();
			}
		}
	}

	/**
	 * 子树遍历的第一个节点
	 *
	 * @param root_of_sub_tree 子树根节点
	 */
	public static BinaryTreeNode minimum(BinaryTreeNode root_of_sub_tree, TraversalOrder order) {
		BinaryTreeNode ret = root_of_sub_tree;
		switch (order) {
		case Preorder:
		case PostorderReverse:
			return ret;

		case Inorder:
		case Postorder:
			ret = root_of_sub_tree;
	        while (null != ret.getLeftChild())
	            ret = ret.getLeftChild();
	        return ret;

		case PreorderReverse:
		case InorderReverse:
	        while (null != ret.getRightChild())
	        	ret = ret.getRightChild();
	        return ret;

		default:
			throw new IllegalArgumentException();
		}
	}

	/**
	 * 子树遍历的最后一个节点
	 *
	 * @param root_of_sub_tree 子树根节点
	 */
	public static BinaryTreeNode maximum(BinaryTreeNode root_of_sub_tree, TraversalOrder order) {
		return minimum(root_of_sub_tree, order.oppositeOrder());
	}

	/**
	 * 子树遍历的下一个节点
	 *
	 * @param parent_of_sub_tree 子树父节点, null 表示整棵树
	 */
	public static BinaryTreeNode successor(BinaryTreeNode parent_of_sub_tree, BinaryTreeNode x, TraversalOrder order) {
		BinaryTreeNode ret = x;
		switch (order) {
		case Preorder:
            if (null != ret.getLeftChild()) {
                ret = ret.getLeftChild();
            } else if (null != ret.getRightChild()) {
                ret = ret.getRightChild();
            } else {
                BinaryTreeNode current = ret, parent = ret.getParent();
                while (parent_of_sub_tree != parent &&
                    (current == parent.getRightChild() || null == parent.getRightChild())) {
                    current = parent;
                    parent = current.getParent();
                }
                if (parent_of_sub_tree == parent)
                	ret = null; // the end
                else
                    ret = parent.getRightChild();
            }
            return ret;

		case PreorderReverse: {
            BinaryTreeNode parent = ret.getParent();
            if (parent == parent_of_sub_tree) {
            	ret = null; // the end
            } else if (ret == parent.getLeftChild() || null == parent.getLeftChild()) {
                ret = parent;
            } else {
                ret = parent.getLeftChild();
                while (true) {
                    if (null != ret.getRightChild())
                        ret = ret.getRightChild();
                    else if (null != ret.getLeftChild())
                        ret = ret.getLeftChild();
                    else
                        break;
                }
            }
            return ret;
		}

		case Inorder:
            if (null != ret.getRightChild()) {
            	ret = ret.getRightChild();
                while (null != ret.getLeftChild())
                	ret = ret.getLeftChild();
            } else {
                BinaryTreeNode current = ret, parent = ret.getParent();
                while (parent_of_sub_tree != parent && current == parent.getRightChild()) {
                    current = parent;
                    parent = current.getParent();
                }
                if (parent_of_sub_tree == parent)
                    ret = null; // the end
                else
                    ret = parent;
            }
            return ret;

		case InorderReverse:
            if (null != ret.getLeftChild()) {
                ret = ret.getLeftChild();
                while (null != ret.getRightChild())
                    ret = ret.getRightChild();
            } else {
                BinaryTreeNode parent = ret.getParent();
                while (parent_of_sub_tree != parent && ret == parent.getLeftChild()) {
                    ret = parent;
                    parent = ret.getParent();
                }
                if (parent_of_sub_tree == parent)
                	ret = null; // the end
                else
                	ret = parent;
            }
            return ret;

		case Postorder: {
            BinaryTreeNode parent = ret.getParent();
            if (parent_of_sub_tree == parent) {
            	ret = null; // the end
            } else if (ret == parent.getRightChild() || null == parent.getRightChild()) {
            	ret = parent;
            } else {
            	ret = parent.getRightChild();
                while (true) {
                    if (null != ret.getLeftChild())
                    	ret = ret.getLeftChild();
                    else if (null != ret.getRightChild())
                    	ret = ret.getRightChild();
                    else
                        break;
                }
            }
            return ret;
		}

		case PostorderReverse:
            if (null != ret.getRightChild()) {
                ret = ret.getRightChild();
            } else if (null != ret.getLeftChild()) {
                ret = ret.getLeftChild();
            } else {
                BinaryTreeNode parent = ret.getParent();
                while (parent_of_sub_tree != parent &&
                    (ret == parent.getLeftChild() || null == parent.getLeftChild())) {
                    ret = parent;
                    parent = ret.getParent();
                }
                if (parent_of_sub_tree == parent)
                	ret = null; // the end
                else
                	ret = parent.getLeftChild();
            }
            return ret;

        default:
        	throw new IllegalArgumentException();
		}
	}

	/**
	 * 子树遍历的上一个节点
	 *
	 * @param parent_of_sub_tree 子树父节点, null 表示整棵树
	 */
	public static BinaryTreeNode predecessor(BinaryTreeNode parent_of_sub_tree, BinaryTreeNode x, TraversalOrder order) {
		return successor(parent_of_sub_tree, x, order.oppositeOrder());
	}


	/**
	 * 二叉树遍历迭代器
	 *
	 * @author jingqi
	 */
	public static class TraversalIterator implements Iterator<BinaryTreeNode> {

		private final TraversalOrder order;
		private final BinaryTreeNode last;
		private BinaryTreeNode next;

		/**
		 * @param root_of_sub_tree 子树根节点
		 * @param order 遍历顺序
		 */
		public TraversalIterator(BinaryTreeNode root_of_sub_tree, TraversalOrder order) {
			this.order = order;
			this.next = minimum(root_of_sub_tree, order);
			this.last = maximum(root_of_sub_tree, order);
		}

		/**
		 * @param first 第一个要访问的节点
		 * @param last 最后一个要访问的节点
		 * @param order 遍历顺序
		 */
		public TraversalIterator(BinaryTreeNode first, BinaryTreeNode last, TraversalOrder order) {
			this.order = order;
			this.next = first;
			this.last = last;
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
			if (ret == last)
				next = null;
			else
				next = successor(null, next, order);
			return ret;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}

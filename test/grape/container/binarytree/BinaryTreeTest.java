package grape.container.binarytree;

import static org.junit.Assert.assertEquals;
import grape.container.binarytree.BinaryTree.TraversalIterator;
import grape.container.binarytree.BinaryTree.TraversalOrder;
import grape.container.binarytree.node.BinarySearchTreeNode;
import grape.container.binarytree.node.BinaryTreeNode;

import java.util.Iterator;

import org.junit.Test;

public class BinaryTreeTest {

	static class Node implements BinarySearchTreeNode {

		int value;
		Node parent, left, right;

		public Node(int v) {
			value = v;
		}

		@Override
		public Node getParent() {
			return parent;
		}

		@Override
		public void setParent(BinaryTreeNode p) {
			parent = (Node) p;
		}

		@Override
		public Node getLeftChild() {
			return left;
		}

		@Override
		public void setLeftChild(BinaryTreeNode l) {
			left = (Node) l;
		}

		@Override
		public Node getRightChild() {
			return right;
		}

		@Override
		public void setRightChild(BinaryTreeNode r) {
			right = (Node) r;
		}

		@Override
		public int compareTo(Object o) {
			if (!(o instanceof Node))
				throw new IllegalArgumentException();
			Node n = (Node) o;
			return value - n.value;
		}
	}

	static Node buildTree() {
        // 构建这样一颗树
        //       4
        //     /   \
        //    2     6
        //   / \   / \
        //  1   3 5   7
        //
        Node root = null;
        int[] nodes = {4, 2, 6, 1, 3, 5, 7 };
        for (int i = 0; i < 7; ++i)
            root = (Node) BinarySearchTree.insert(root, new Node(nodes[i]));
        return root;
    }

	void checkIterator(Iterator<BinaryTreeNode> iter, int[] expected) {
		int i = 0;
		while (iter.hasNext()) {
			Node n = (Node) iter.next();
			assertEquals(expected[i++], n.value);
		}
		assertEquals(i, expected.length);
	}

	@Test
	public void testIterator() {
		Node root = buildTree();

		// 中序遍历
		Iterator<BinaryTreeNode> iter = new TraversalIterator(root, TraversalOrder.Inorder);
		checkIterator(iter, new int[] {1, 2, 3, 4, 5, 6, 7});

		iter = new TraversalIterator(root, TraversalOrder.InorderReverse);
		checkIterator(iter, new int[] {7, 6, 5, 4, 3, 2, 1});

		// 先序遍历
		iter = new TraversalIterator(root, TraversalOrder.Preorder);
		checkIterator(iter, new int[] {4, 2, 1, 3, 6, 5, 7});

		iter = new TraversalIterator(root, TraversalOrder.PreorderReverse);
		checkIterator(iter, new int[] {7, 5, 6, 3, 1, 2, 4});

		// 后序遍历
		iter = new TraversalIterator(root, TraversalOrder.Postorder);
		checkIterator(iter, new int[] {1, 3, 2, 5, 7, 6, 4});

		iter = new TraversalIterator(root, TraversalOrder.PostorderReverse);
		checkIterator(iter, new int[] {4, 6, 7, 5, 2, 3, 1});
	}

}

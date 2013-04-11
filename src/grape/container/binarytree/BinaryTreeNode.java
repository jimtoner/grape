package grape.container.binarytree;


/**
 * 二叉树节点
 *
 * @author jingqi
 */
public interface BinaryTreeNode {

	/**
	 * 获取父节点
	 */
	BinaryTreeNode getParent();

	/**
	 * 设置父节点
	 */
	void setParent(BinaryTreeNode p);

	/**
	 * 获取左子节点
	 */
	BinaryTreeNode getLeftChild();

	/**
	 * 设置左子节点
	 */
	void setLeftChild(BinaryTreeNode l);

	/**
	 * 获取右子节点
	 */
	BinaryTreeNode getRightChild();

	/**
	 * 设置右子节点
	 */
	void setRightChild(BinaryTreeNode r);
}

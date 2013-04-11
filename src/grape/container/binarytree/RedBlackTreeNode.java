package grape.container.binarytree;

/**
 * 红黑树节点
 *
 * @author jingqi
 */
public interface RedBlackTreeNode extends BinarySearchTreeNode {

	/**
	 * 是红色节点还是黑色节点
	 */
	boolean isRed();

	/**
	 * 设置节点颜色
	 */
	void setRed(boolean r);
}

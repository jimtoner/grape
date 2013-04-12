package grape.container.binarytree.node;

/**
 * 二叉查找树节点
 *
 * @author jingqi
 */
public interface BinarySearchTreeNode extends BinaryTreeNode {

	/**
	 * 与另一个对象做对比，以便进行排序
	 *
	 * @return 小于该对象则返回负数，等于则返回0，否则返回正数
	 */
	int compareTo(Object o);
}

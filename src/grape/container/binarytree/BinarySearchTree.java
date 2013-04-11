package grape.container.binarytree;


/**
 * 二叉查找树
 *
 * @author jingqi
 */
public class BinarySearchTree {

	private BinarySearchTree() {}

    /**
     * 查找数据所在的节点
     *
     * @return
     *      没有找到则返回 null
     */
    public static BinarySearchTreeNode search(BinarySearchTreeNode sub_root, Object key) {
        while (null != sub_root) {
        	int rs = sub_root.compareTo(key);
            if (rs > 0)
                sub_root = (BinarySearchTreeNode) sub_root.getLeftChild();
            else if (rs < 0)
                sub_root = (BinarySearchTreeNode) sub_root.getRightChild();
            else
                return sub_root;
        }
        return null;
    }

    /**
     * 插入新节点到二叉查找树
     *
     * @return 新的根
     */
    public static BinarySearchTreeNode insert(BinarySearchTreeNode root, BinarySearchTreeNode new_node) {
    	BinarySearchTreeNode parent = null;
        boolean insertToLeft = true;
        for (BinarySearchTreeNode x = root; null != x; ) {
            parent = (BinarySearchTreeNode) x;
            if (new_node.compareTo(x) < 0) {
                x = (BinarySearchTreeNode) x.getLeftChild();
                insertToLeft = true;
            } else {
                x = (BinarySearchTreeNode) x.getRightChild();
                insertToLeft = false;
            }
        }

        new_node.setParent(parent);
        if (null == parent)
            root = new_node;
        else if (insertToLeft)
            parent.setLeftChild(new_node);
        else
            parent.setRightChild(new_node);
        return root;
    }

    /**
     * 从二叉查找树中删除已有节点
     *
     * @return 新的根
     */
    public static BinarySearchTreeNode remove(BinarySearchTreeNode root, BinarySearchTreeNode to_be_del) {
    	BinarySearchTreeNode escaper = null;
        if (null == to_be_del.getLeftChild() || null == to_be_del.getRightChild())
            escaper = to_be_del;
        else
            escaper = (BinarySearchTreeNode) successor(to_be_del);

        BinarySearchTreeNode sublink = null;
        if (null != escaper.getLeftChild())
            sublink = (BinarySearchTreeNode) escaper.getLeftChild();
        else
            sublink = (BinarySearchTreeNode) escaper.getRightChild();

        BinarySearchTreeNode sublink_parent = (BinarySearchTreeNode) escaper.getParent();
        if (null != sublink)
            sublink.setParent(sublink_parent);

        if (null == sublink_parent)
            root = sublink;
        else if (escaper == sublink_parent.getLeftChild())
            sublink_parent.setLeftChild(sublink);
        else
            sublink_parent.setRightChild(sublink);

        if (escaper != to_be_del) {
            escaper.setParent(to_be_del.getParent());
            escaper.setLeftChild(to_be_del.getLeftChild());
            escaper.setRightChild(to_be_del.getRightChild());
            if (null == to_be_del.getParent())
                root = escaper;
            else if (to_be_del == to_be_del.getParent().getLeftChild())
                to_be_del.getParent().setLeftChild(escaper);
            else
                to_be_del.getParent().setRightChild(escaper);
        }
        return root;
    }

    /**
     * 找到最小数据所在的节点
     */
    public static BinarySearchTreeNode minimum(BinarySearchTreeNode sub_root) {
        while (null != sub_root.getLeftChild())
            sub_root = (BinarySearchTreeNode) sub_root.getLeftChild();
        return sub_root;
    }

    /**
     * 找到最大数据所在的节点
     */
    public static BinarySearchTreeNode maximum(BinarySearchTreeNode sub_root) {
        while (null != sub_root.getRightChild())
            sub_root = (BinarySearchTreeNode) sub_root.getRightChild();
        return sub_root;
    }

    /**
     * 按照数据大小顺序找到下一个结点
     */
    public static BinarySearchTreeNode successor(BinarySearchTreeNode x) {
        if (null != x.getRightChild())
            return minimum((BinarySearchTreeNode) x.getRightChild());
        BinarySearchTreeNode parent = (BinarySearchTreeNode) x.getParent();
        while (null != parent && x == parent.getRightChild()) {
            x = parent;
            parent = (BinarySearchTreeNode) x.getParent();
        }
        return parent;
    }

    /**
     * 按照数据大小顺序找到上一个结点
     */
    public static BinarySearchTreeNode predecessor(BinarySearchTreeNode x) {
        if (null != x.getLeftChild())
            return maximum((BinarySearchTreeNode) x.getLeftChild());
        BinarySearchTreeNode parent = (BinarySearchTreeNode) x.getParent();
        while (null != parent && x == parent.getLeftChild()) {
            x = parent;
            parent = (BinarySearchTreeNode) x.getParent();
        }
        return parent;
    }
}

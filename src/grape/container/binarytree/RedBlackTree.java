package grape.container.binarytree;

import grape.container.binarytree.node.RedBlackTreeNode;


/**
 * 红黑树
 *
 * @author jingqi
 */
public class RedBlackTree {

	private RedBlackTree() {}

	/**
	 * 红黑树从小到大遍历的顺序
	 */
	public static final BinaryTree.TraversalOrder TRAVERSAL_ORDER = BinarySearchTree.TRAVERSAL_ORDER;

	/**
	 * 红黑树插入、删除操作监听器
	 *
	 * @author jingqi
	 */
	public static interface RedBlackTreeOperationListener {
		/**
		 * 节点被添加到了树上
		 */
		void attachedToTree(RedBlackTreeNode attached);

		/**
		 * 节点被从树上摘除
		 * NOTE: 实际上可能是一个 “摘除-替换” 的过程，被替换的节点必然是其 parent 或者祖先
		 *
		 * @param parent 被摘除节点的父节点，如果为 null，说明根节点被摘除
		 */
		void disattachedFromTree(RedBlackTreeNode parent);

		/**
		 * 左旋转完成
		 *
		 * @param n 两个关键节点中的子节点
		 */
		void leftRotated(RedBlackTreeNode n);

		/**
		 * 右旋转完成
		 *
		 * @param n 两个关键节点中的子节点
		 */
		void rightRotated(RedBlackTreeNode n);
	}

    /**
     * 插入新节点到红黑树
     *
     * @return 新的根
     */
    public static RedBlackTreeNode insert(RedBlackTreeNode root, RedBlackTreeNode new_node, RedBlackTreeOperationListener listener) {

    	// 先用二叉搜索树的方式插入新节点，并设置颜色为红色
        root = (RedBlackTreeNode) BinarySearchTree.insert(root, new_node);
        new_node.setRed(true);
        if (listener != null)
        	listener.attachedToTree(new_node);

        // 然后修正红黑树
        root = _rb_insert_fixup(root, new_node, listener);

        return root;
    }

    public static RedBlackTreeNode insert(RedBlackTreeNode root, RedBlackTreeNode new_node) {
    	return insert(root, new_node, null);
    }

    /**
     * 从红黑树中删除已有节点
     *
     * @return 新的根
     */
    public static RedBlackTreeNode remove(RedBlackTreeNode root, RedBlackTreeNode to_be_del, RedBlackTreeOperationListener listener) {
    	// 找到要从树上移除的节点 escaper，其子节点个数必然小于等于1
        RedBlackTreeNode escaper = null;
        if (null == to_be_del.getLeftChild() || null == to_be_del.getRightChild())
            escaper = to_be_del;
        else
            escaper = (RedBlackTreeNode) BinarySearchTree.successor(to_be_del);

        // 记录 escaper 的子树
        RedBlackTreeNode sublink = null;
        if (null != escaper.getLeftChild())
            sublink = (RedBlackTreeNode) escaper.getLeftChild();
        else
            sublink = (RedBlackTreeNode) escaper.getRightChild();

        // 摘除 escaper
        RedBlackTreeNode sublink_parent = (RedBlackTreeNode) escaper.getParent();
        if (null != sublink)
            sublink.setParent(sublink_parent);

        if (null == sublink_parent)
            root = sublink;
        else if (escaper == sublink_parent.getLeftChild())
            sublink_parent.setLeftChild(sublink);
        else
            sublink_parent.setRightChild(sublink);
        if (listener != null)
        	listener.disattachedFromTree(sublink_parent);

        final boolean red_escaper = escaper.isRed();
        if (escaper != to_be_del) {
        	// 用 escaper 置换 to_be_del
            escaper.setLeftChild(to_be_del.getLeftChild());
            if (to_be_del.getLeftChild() != null)
            	to_be_del.getLeftChild().setParent(escaper);
            escaper.setRightChild(to_be_del.getRightChild());
            if (to_be_del.getRightChild() != null)
            	to_be_del.getRightChild().setParent(escaper);
            escaper.setParent(to_be_del.getParent());
            if (to_be_del.getParent() == null)
            	root = escaper;
            else if (to_be_del == to_be_del.getParent().getLeftChild())
            	to_be_del.getParent().setLeftChild(escaper);
            else
            	to_be_del.getParent().setRightChild(escaper);
            escaper.setRed(to_be_del.isRed());

            if (sublink_parent == to_be_del)
            	sublink_parent = escaper;
        }
        if (listener != null)
        	listener.disattachedFromTree(sublink_parent);

        if (!red_escaper)
            root = _rb_delete_fixup(root, sublink, sublink_parent, listener);

        return root;
    }

    public static RedBlackTreeNode remove(RedBlackTreeNode root, RedBlackTreeNode to_be_del) {
    	return remove(root, to_be_del, null);
    }

    /**
     * 删除一个范围
     */
    public static RedBlackTreeNode removeRange(RedBlackTreeNode root, RedBlackTreeNode first, RedBlackTreeNode last, RedBlackTreeOperationListener listener) {
    	RedBlackTreeNode n = first;
    	while (true) {
    		RedBlackTreeNode next = (RedBlackTreeNode) BinarySearchTree.successor(n);
    		root = remove(root, n, listener);
    		if (n == last)
    			break;
    		n = next;
    	}
    	return root;
    }

    public static RedBlackTreeNode removeRange(RedBlackTreeNode root, RedBlackTreeNode first, RedBlackTreeNode last) {
    	return removeRange(root, first, last, null);
    }

    /**
     * 左旋转
     */
    static RedBlackTreeNode _left_rotate(RedBlackTreeNode root, RedBlackTreeNode x) {
        //
        //       |                           |
        //       X                           Y
        //      / \     left-rotate(X)      / \
        //         Y         ->            X
        //        / \                     / \
        //
        RedBlackTreeNode y = (RedBlackTreeNode) x.getRightChild();
        x.setRightChild(y.getLeftChild());
        if (null != y.getLeftChild())
            y.getLeftChild().setParent(x);
        y.setParent(x.getParent());
        if (null == x.getParent())
            root = y;
        else if (x == x.getParent().getLeftChild())
            x.getParent().setLeftChild(y);
        else
            x.getParent().setRightChild(y);
        y.setLeftChild(x);
        x.setParent(y);

        return root;
    }

    /**
     * 右旋转
     */
    static RedBlackTreeNode _right_rotate(RedBlackTreeNode root, RedBlackTreeNode x) {
        //
        //        |                           |
        //        X                           Y
        //       / \     right-rotate(X)     / \
        //      Y             ->                X
        //     / \                             / \
        //
        RedBlackTreeNode y = (RedBlackTreeNode) x.getLeftChild();
        x.setLeftChild(y.getRightChild());
        if (null != y.getRightChild())
            y.getRightChild().setParent(x);
        y.setParent(x.getParent());
        if (null == x.getParent())
            root = y;
        else if (x == x.getParent().getLeftChild())
            x.getParent().setLeftChild(y);
        else
            x.getParent().setRightChild(y);
        y.setRightChild(x);
        x.setParent(y);

        return root;
    }

    static RedBlackTreeNode _rb_insert_fixup(RedBlackTreeNode root, RedBlackTreeNode x, RedBlackTreeOperationListener listener) {
        while (null != x.getParent() && ((RedBlackTreeNode) x.getParent()).isRed()) {
        	RedBlackTreeNode parent = (RedBlackTreeNode) x.getParent();
            if (parent == parent.getParent().getLeftChild()) {
            	RedBlackTreeNode uncle = (RedBlackTreeNode) parent.getParent().getRightChild();
                if (null != uncle && uncle.isRed()) {
                    // case 1:
                    //
                    //       |                   |
                    //       B                  [R]
                    //      / \      color      / \
                    //     R   R      ->       B   B
                    //      \                   \
                    //      [R]                  R
                    //
                    parent.setRed(false);
                    uncle.setRed(false);
                    ((RedBlackTreeNode) parent.getParent()).setRed(true);
                    x = (RedBlackTreeNode) parent.getParent();
                } else {
                    if (x == parent.getRightChild()) {
                        // case 2:
                        //
                        //      |                  |
                        //      B                  B
                        //     / \     rotate     / \
                        //    R   B      ->      R   B
                        //     \                /
                        //     [R]            [R]
                        //
                        x = parent;
                        root = _left_rotate(root, x);
                        if (listener != null)
                        	listener.leftRotated(x);
                    }

                    // case 3:
                    //
                    //         |                         |
                    //         B                         B
                    //        / \     color & rotate    / \
                    //       R   B          ->        [R]  R
                    //      /                               \
                    //     [R]                               B
                    //
                    parent.setRed(false);
                    final RedBlackTreeNode xx = (RedBlackTreeNode) parent.getParent();
                    xx.setRed(true);
                    root = _right_rotate(root, xx);
                    if (listener != null)
                    	listener.rightRotated(xx);
                }
            } else {
            	RedBlackTreeNode uncle = (RedBlackTreeNode) parent.getParent().getLeftChild();
                if (null != uncle && uncle.isRed()) {
                    // case 1:
                    //
                    //       |                   |
                    //       B                  [R]
                    //      / \      color      / \
                    //     R   R      ->       B   B
                    //        /                   /
                    //      [R]                  R
                    //
                    parent.setRed(false);
                    uncle.setRed(false);
                    ((RedBlackTreeNode) parent.getParent()).setRed(true);
                    x = (RedBlackTreeNode) parent.getParent();
                } else {
                    if (x == parent.getLeftChild()) {
                        // case 2:
                        //
                        //      |                  |
                        //      B                  B
                        //     / \     rotate     / \
                        //    B   R      ->      B   R
                        //       /                    \
                        //     [R]                    [R]
                        //
                        x = parent;
                        root = _right_rotate(root, x);
                        if (listener != null)
                        	listener.rightRotated(x);
                    }

                    // case 3:
                    //
                    //         |                         |
                    //         B                         B
                    //        / \     color & rotate    / \
                    //       B   R          ->         R  [R]
                    //            \                   /
                    //            [R]                B
                    //
                    parent.setRed(false);
                    final RedBlackTreeNode xx = (RedBlackTreeNode) parent.getParent();
                    xx.setRed(true);
                    root = _left_rotate(root, xx);
                    if (listener != null)
                    	listener.leftRotated(xx);
                }
            }
        }
        root.setRed(false); // root is always black
        return root;
    }

    static RedBlackTreeNode _rb_delete_fixup(RedBlackTreeNode root, RedBlackTreeNode sublink, RedBlackTreeNode sublink_parent,
    		RedBlackTreeOperationListener listener) {
        while (sublink != root && (null == sublink || !sublink.isRed())) {
            if (sublink == sublink_parent.getLeftChild()) {
            	RedBlackTreeNode brother = (RedBlackTreeNode) sublink_parent.getRightChild();

                if (brother.isRed()) {
                    // case 1:
                    //
                    //     |                                 |
                    //     B                                 B
                    //    / \        color & rotate         / \
                    //  [B]  R            ->               R   B
                    //      / \                           / \
                    //     B   B                        [B]  B
                    //
                    brother.setRed(false);
                    sublink_parent.setRed(true);
                    root = _left_rotate(root, sublink_parent);
                    if (listener != null)
                    	listener.leftRotated(sublink_parent);
                    brother = (RedBlackTreeNode) sublink_parent.getRightChild();
                }

                if ((null == brother.getLeftChild() || !((RedBlackTreeNode) brother.getLeftChild()).isRed()) &&
                		(null == brother.getRightChild() || !((RedBlackTreeNode) brother.getRightChild()).isRed())) {
                    // case 2:
                    //
                    //    |                      |
                    //    ?                     [?]
                    //   / \         color      / \
                    // [B]  B         ->       B   R
                    //     / \                    / \
                    //    B   B                  B   B
                    //
                    brother.setRed(true);
                    sublink = sublink_parent;
                    sublink_parent = (RedBlackTreeNode) sublink.getParent();
                } else {
                    if (null == brother.getRightChild() || !((RedBlackTreeNode) brother.getRightChild()).isRed()) {
                        // case 3:
                        //
                        //       |                             |
                        //       ?                             ?
                        //      / \       color & rotate      / \
                        //    [B]  B            ->          [B]  B
                        //        / \                             \
                        //       R   B                             R
                        //                                          \
                        //                                           B
                        //
                        ((RedBlackTreeNode) brother.getLeftChild()).setRed(false);
                        brother.setRed(true);
                        root = _right_rotate(root, brother);
                        if (listener != null)
                        	listener.rightRotated(brother);
                        brother = (RedBlackTreeNode) sublink_parent.getRightChild();
                    }

                    // case 4:
                    //
                    //     |                                     |
                    //     ?                                     ?
                    //    / \            color & rotate         / \
                    //  [B]  B                ->               B   B
                    //      / \                               / \
                    //     ?   R                             B   ?
                    //
                    brother.setRed(sublink_parent.isRed());
                    sublink_parent.setRed(false);
                    ((RedBlackTreeNode) brother.getRightChild()).setRed(false);
                    root = _left_rotate(root, sublink_parent);
                    if (listener != null)
                    	listener.leftRotated(sublink_parent);
                    sublink = root; // end the loop
                    sublink_parent = null;
               }
            } else {
            	RedBlackTreeNode brother = (RedBlackTreeNode) sublink_parent.getLeftChild();

                if (brother.isRed()) {
                    // case 1:
                    //
                    //     |                                 |
                    //     B                                 B
                    //    / \        color & rotate         / \
                    //   R  [B]            ->              B   R
                    //  / \                                   / \
                    // B   B                                 B  [B]
                    //
                    brother.setRed(false);
                    sublink_parent.setRed(true);
                    root = _right_rotate(root, sublink_parent);
                    if (listener != null)
                    	listener.rightRotated(sublink_parent);
                    brother = (RedBlackTreeNode) sublink_parent.getLeftChild();
                }

                if ((null == brother.getLeftChild() || !((RedBlackTreeNode) brother.getLeftChild()).isRed()) &&
                		(null == brother.getRightChild() || !((RedBlackTreeNode) brother.getRightChild()).isRed())) {
                    // case 2:
                    //
                    //     |                      |
                    //     ?                     [?]
                    //    / \         color      / \
                    //   B  [B]        ->       R   B
                    //  / \                    / \
                    // B   B                  B   B
                    //
                    brother.setRed(true);
                    sublink = sublink_parent;
                    sublink_parent = (RedBlackTreeNode) sublink.getParent();
                } else {
                    if (null == brother.getLeftChild() || !((RedBlackTreeNode) brother.getLeftChild()).isRed()) {
                        // case 3:
                        //
                        //       |                             |
                        //       ?                             ?
                        //      / \       color & rotate      / \
                        //     B  [B]            ->          B  [B]
                        //    / \                           /
                        //   B   R                         R
                        //                                /
                        //                               B
                        //
                        ((RedBlackTreeNode) brother.getRightChild()).setRed(false);
                        brother.setRed(true);
                        root = _left_rotate(root, brother);
                        if (listener != null)
                        	listener.leftRotated(brother);
                        brother = (RedBlackTreeNode) sublink_parent.getLeftChild();
                    }

                    // case 4:
                    //
                    //     |                                     |
                    //     ?                                     ?
                    //    / \            color & rotate         / \
                    //   B  [B]               ->               B   B
                    //  / \                                       / \
                    // R   ?                                     ?   B
                    //
                    brother.setRed(sublink_parent.isRed());
                    sublink_parent.setRed(false);
                    ((RedBlackTreeNode) brother.getLeftChild()).setRed(false);
                    root = _right_rotate(root, sublink_parent);
                    if (listener != null)
                    	listener.rightRotated(sublink_parent);
                    sublink = root; // end the loop
                    sublink_parent = null;
                }
            }
        }
        if (sublink != null)
        	sublink.setRed(false);
        return root;
    }
}

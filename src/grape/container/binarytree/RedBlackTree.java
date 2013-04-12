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
     * 插入新节点到红黑树
     *
     * @return 新的根
     */
    public static RedBlackTreeNode insert(RedBlackTreeNode root, RedBlackTreeNode new_node) {
    	RedBlackTreeNode parent = null;
        boolean insertToLeft = true;
        for (RedBlackTreeNode current = root; null != current; ) {
            parent = current;
            if (new_node.compareTo(current) < 0) {
                current = (RedBlackTreeNode) current.getLeftChild();
                insertToLeft = true;
            } else {
                current = (RedBlackTreeNode) current.getRightChild();
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

        new_node.setRed(true);
        root = _rb_insert_fixup(root, new_node);

        return root;
    }

    /**
     * 从红黑树中删除已有节点
     *
     * @return 新的根
     */
    public static RedBlackTreeNode remove(RedBlackTreeNode root, RedBlackTreeNode to_be_del) {
        RedBlackTreeNode escaper = null;
        if (null == to_be_del.getLeftChild() || null == to_be_del.getRightChild())
            escaper = to_be_del;
        else
            escaper = (RedBlackTreeNode) BinarySearchTree.successor(to_be_del);

        RedBlackTreeNode sublink = null;
        if (null != escaper.getLeftChild())
            sublink = (RedBlackTreeNode) escaper.getLeftChild();
        else
            sublink = (RedBlackTreeNode) escaper.getRightChild();

        RedBlackTreeNode sublink_parent = (RedBlackTreeNode) escaper.getParent();
        if (null != sublink)
            sublink.setParent(sublink_parent);

        if (null == sublink_parent)
            root = sublink;
        else if (escaper == sublink_parent.getLeftChild())
            sublink_parent.setLeftChild(sublink);
        else
            sublink_parent.setRightChild(sublink);

        final boolean red_escaper = escaper.isRed();
        if (escaper != to_be_del) {
            // replace x with escaper
            escaper.setLeftChild(to_be_del.getLeftChild());
            escaper.setRightChild(to_be_del.getRightChild());
            escaper.setParent(to_be_del.getParent());
            escaper.setRed(to_be_del.isRed());
            if (null == to_be_del.getParent())
                root = escaper;
            else if (to_be_del == to_be_del.getParent().getLeftChild())
            	to_be_del.getParent().setLeftChild(escaper);
            else
            	to_be_del.getParent().setRightChild(escaper);
        }

        if (!red_escaper)
            root = _rb_delete_fixup(root, sublink, sublink_parent);

        return root;
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

    static RedBlackTreeNode _rb_insert_fixup(RedBlackTreeNode root, RedBlackTreeNode x) {
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
                    ((RedBlackTreeNode) parent.getParent()).setRed(true);
                    root = _right_rotate(root, (RedBlackTreeNode) parent.getParent());
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
                    ((RedBlackTreeNode) parent.getParent()).setRed(true);
                    root = _left_rotate(root, (RedBlackTreeNode) parent.getParent());
                }
            }
        }
        root.setRed(false); // root is always black
        return root;
    }

    static RedBlackTreeNode _rb_delete_fixup(RedBlackTreeNode root, RedBlackTreeNode sublink, RedBlackTreeNode sublink_parent) {
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
                    sublink = root; // end the loop
                    sublink_parent = null;
                }
            }
        }
        sublink.setRed(false);
        return root;
    }
}

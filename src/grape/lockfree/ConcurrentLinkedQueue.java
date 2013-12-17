package grape.lockfree;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 使用 Michael-Scott 算法的并发无锁队列<br/>
 * <br/>
 * 注意:<br/>
 * <ol>
 * <li> 这里并没有处理 CAS 操作的 ABA 问题，但是并不影响正确性</li>
 * <li> 没有使用 <b>消隐(Shavit And Touitou)</b> 策略增加吞吐量，不适合作为高性能服务器关键节点</li>
 * </ol>
 *
 * 参考资料：<br/>
 * [1] http://www.ibm.com/developerworks/cn/java/j-jtp04186/<br/>
 * [2] 钱立兵，陈波，晏涛，徐云，孟金涛，刘涛. 多线程并发访问无锁队列的算法研究[J]. 先进技术研究通报，2009，3(8)：50 - 55<br/>
 *
 * @author jingqi
 */
public class ConcurrentLinkedQueue <E> {

	private static class Node <E> {
		E item;
		final AtomicReference<Node<E> > next = new AtomicReference<Node<E> >();

		Node(E item) {
			this.item = item;
		}
	}

	private final AtomicInteger size = new AtomicInteger(0);
	private final AtomicReference<Node<E> > head = new AtomicReference<Node<E> >(new Node<E>(null));
	private final AtomicReference<Node<E> > tail = new AtomicReference<Node<E> >(head.get());

	/**
	 * push to tail
	 */
	public void push(E item) {
		Node<E> newNode = new Node<E>(item);
		while (true) {
			Node<E> curTail = tail.get();
			Node<E> residue = curTail.next.get();
			if (curTail == tail.get()) {
				if (residue == null) {
					if (curTail.next.compareAndSet(null, newNode)) {
						tail.compareAndSet(curTail, newNode); // 这一步如果失败，会在 * 步骤中修复
						// increase size
						size.incrementAndGet();
						return;
					}
				} else {
					tail.compareAndSet(curTail, residue); // *
				}
			}
		}
	}

	/**
	 * try to push to tail
	 *
	 * @return false if concurrent failure
	 */
	public boolean tryPush(E item) {
		Node<E> newNode = new Node<E>(item);
		Node<E> curTail = tail.get();
		Node<E> residue = curTail.next.get();
		if (curTail == tail.get()) {
			if (residue == null) {
				if (curTail.next.compareAndSet(null, newNode)) {
					tail.compareAndSet(curTail, newNode); // 这一步如果失败，会在 * 步骤中修复
					// increase size
					size.incrementAndGet();
					return true;
				}
			} else {
				tail.compareAndSet(curTail, residue); // *
			}
		}
		return false;
	}

	/**
	 * pop from head
	 *
	 * @return null if queue is empty
	 */
	public E pop() {
		while (true) {
			Node<E> headNode = head.get();
			Node<E> tailNode = tail.get();
			Node<E> nextNode = headNode.next.get();
			if (headNode == head.get()) {
				if (headNode == tailNode) { // empty queue or tail falling behind
					if (nextNode == null)
						return null;
					tail.compareAndSet(tailNode, nextNode); // tail failling behind, advance it
				} else {
					if (head.compareAndSet(headNode, nextNode)) {
						// decrease size
						size.decrementAndGet();

						E ret = nextNode.item;
						nextNode.item = null; // nextNode 作为新的 dummy 节点
						return ret;
					}
				}
			}
		}
	}

	/**
	 * try to pop from head
	 *
	 * @return null if queue is empty or concurrent failure
	 */
	public E tryPop() {
		Node<E> headNode = head.get();
		Node<E> tailNode = tail.get();
		Node<E> nextNode = headNode.next.get();
		if (headNode == head.get()) {
			if (headNode == tailNode) { // empty queue or tail falling behind
				if (nextNode == null)
					return null;
				tail.compareAndSet(tailNode, nextNode); // tail failling behind, advance it
			} else {
				if (head.compareAndSet(headNode, nextNode)) {
					// decrease size
					size.decrementAndGet();

					E ret = nextNode.item;
					nextNode.item = null; // nextNode 作为新的 dummy 节点
					return ret;
				}
			}
		}
		return null;
	}

	public void clear() {
		while (size() > 0)
			pop();
	}

	/**
	 * get head
	 */
	public E top() {
		Node<E> n = head.get().next.get();
		if (n == null)
			return null;
		return n.item;
	}

	public int size() {
		return size.get();
	}

	public boolean isEmpty() {
		return size() == 0;
	}
}

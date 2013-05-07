package grape.lockfree;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 使用 Treiber 算法的并发无锁栈<br/>
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
public class ConcurrentLinkedStack <E> {

	private static class Node <E> {
		final E item;
		Node<E> next;

		Node(E item) {
			this.item = item;
		}
	}

	private final AtomicInteger size = new AtomicInteger(0);
	private AtomicReference<Node<E> > head = new AtomicReference<Node<E> >();

	/**
	 * 压栈
	 */
	public void push(E item) {
		Node<E> newHead = new Node<E>(item);
		Node<E> oldHead;
		do {
			// fetch value
			oldHead = head.get();
			newHead.next = oldHead;
		} while (!head.compareAndSet(oldHead, newHead)); // CAS

		// increase size
		size.incrementAndGet();
	}

	/**
	 * 从栈中弹出
	 *
	 * @return null if emtpy
	 */
	public E pop() {
		Node<E> oldHead, newHead;
		do {
			// fetch value
			oldHead = head.get();
			if (oldHead == null)
				return null;
			newHead = oldHead.next;
		} while (!head.compareAndSet(oldHead, newHead)); // CAS

		// decrease size
		size.decrementAndGet();

		return oldHead.item;
	}

	public void clear() {
		while (size() > 0)
			pop();
	}

	/**
	 * 返回栈顶元素，但是并不弹出元素
	 */
	public E top() {
		Node<E> t = head.get();
		if (t == null)
			return null;
		return t.item;
	}

	public int size() {
		return size.get();
	}

	public boolean isEmpty() {
		return size() == 0;
	}
}

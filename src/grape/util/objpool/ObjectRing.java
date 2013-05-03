package grape.util.objpool;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 对象池
 * 在某些情况下，需要避免大量的 new 操作，故此对象池内部也需要避免 new 操作
 *
 * @author jingqi
 */
public class ObjectRing <T> {

	// XXX 使用数组而不是链表，是为了避免反复的 new 节点
	private final AtomicReference<T>[] array;
	private final AtomicInteger from, to;
	private final PoolableObjectFactory<T> factory;

	@SuppressWarnings("unchecked")
	public ObjectRing(PoolableObjectFactory<T> factory) {
		this.factory = factory;
		this.array = (AtomicReference<T>[]) new AtomicReference[factory.maxPooled() + 1];
		for (int i = 0; i < array.length; ++i)
			array[i] = new AtomicReference<T>();
		from = new AtomicInteger(0);
		to = new AtomicInteger(0);
	}

	private void increaseHead() {
		int head, newHead;
		do {
			head = from.get();
			newHead = (head + 1) % array.length;
 		} while (!from.compareAndSet(head, newHead));
	}

	private void increaseTail() {
		int tail, newTail;
		do {
			tail = to.get();
			newTail = (tail + 1) % array.length;
		} while (!to.compareAndSet(tail, newTail));
	}

	public T borrowObject() {
		while (true) {
			int head = from.get();
			if (head == to.get()) // 队列为空，或者 tail 滞后
				break;

			T ret = array[head].get();
			if (ret == null) // head 滞后
				continue;
			if (array[head].compareAndSet(ret, null)) {
				increaseHead();
				return ret;
			}
		}

		return factory.newObject();
	}

	public T returnObject(T obj) {
		if (obj == null)
			return null;

		while (true) {
			int tail = to.get();
			int newTail = (tail + 1) % array.length;
			if (newTail == from.get()) // 队列已满，或者 head 滞后
				return null;

			if (array[tail].compareAndSet(null, obj)) {
				increaseTail();
				return null;
			}
		}
	}
}

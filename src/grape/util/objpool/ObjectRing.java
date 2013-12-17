package grape.util.objpool;

import grape.lockfree.ConcurrentArrayQueue;

/**
 * 对象池
 * 在某些情况下，需要避免大量的 new 操作，故此对象池内部也需要避免 new 操作
 *
 * @author jingqi
 */
public class ObjectRing <T> {

	// XXX 使用数组而不是链表，是为了避免反复的 new 节点
	private final ConcurrentArrayQueue<T> ring;
	private final PoolableObjectFactory<T> factory;

	public ObjectRing(PoolableObjectFactory<T> factory) {
		this.factory = factory;
		this.ring = new ConcurrentArrayQueue<T>(factory.maxPooled());
	}

	public T borrowObject() {
		T ret = ring.tryPop();
		if (ret == null)
			ret = factory.newObject();
		return ret;
	}

	public T returnObject(T obj) {
		if (obj == null)
			return null;

		// 清理对象
		factory.passivateObject(obj);

		// 对象入池
		ring.tryPush(obj);

		// 丢弃对象
		return null;
	}

	public void clear() {
		ring.clear();
	}
}

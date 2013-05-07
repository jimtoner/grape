package grape.util.objpool;

import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 对象池
 * 在某些情况下，需要避免大量的 new 操作，故此对象池内部也需要避免 new 操作
 *
 * @author jingqi
 */
public class ObjectRing <T> {

	// XXX 使用数组而不是链表，是为了避免反复的 new 节点
	private final AtomicReference<T>[] ring;
	private final PoolableObjectFactory<T> factory;
	private final Random r = new Random();

	@SuppressWarnings("unchecked")
	public ObjectRing(PoolableObjectFactory<T> factory) {
		this.factory = factory;
		ring = (AtomicReference<T>[]) new AtomicReference[factory.maxPooled()];
		for (int i = 0; i < ring.length; ++i)
			ring[i] = new AtomicReference<T>();
	}

	public T borrowObject() {
		// 从池中取出对象
		int p = Math.abs(r.nextInt()) % ring.length;
		for (int i = 0; i < ring.length; ++i) {
			p = (p + 1) % ring.length;
			T ret = ring[p].get();
			if (ret != null && ring[p].compareAndSet(ret, null))
				return ret;
		}

		// 重新新建对象
		return factory.newObject();
	}

	public T returnObject(T obj) {
		if (obj == null)
			return null;

		// 清理对象
		factory.passivateObject(obj);

		// 对象入池
		int p = Math.abs(r.nextInt()) % ring.length;
		for (int i = 0; i < ring.length; ++i) {
			p = (p + 1) % ring.length;
			if (ring[p].compareAndSet(null, obj))
				return null;
		}

		// 丢弃对象
		return null;
	}

	public void clear() {
		for (int i = 0; i < ring.length; ++i)
			ring[i].set(null);
	}
}

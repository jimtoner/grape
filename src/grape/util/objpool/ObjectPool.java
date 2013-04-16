package grape.util.objpool;

import java.util.LinkedList;

/**
 * 对象池，用于对象复用，减少系统垃圾回收的压力
 *
 * <ol>
 * <li>为了避免内存泄露，为了避免垃圾信息导致的不确定性问题，需要处理好清理动作{@link PoolableObjectFactory#passivateObject(Object)}</li>
 * <li>所有的操作(包括只borrow不return, return一个被new的对象)都是安全的，但是不包括
 * return一个对象后还接着使用这个对象(包括对一个对象重复的调用return动作)。<br/>
 * 为了避免重复的return动作，建议return之后将变量及时置为null</li>
 * </ol>
 *
 * @author jingqi
 *
 */
public class ObjectPool <T> {

	// 对象池
	final LinkedList<T> pool;
	// 对象工厂
	final PoolableObjectFactory<T> factory;

	public ObjectPool(PoolableObjectFactory<T> factory) {
		assert factory != null;
		this.pool = new LinkedList<T>();
		this.factory = factory;
	}

	public T borrowObject() {
		// 进入临界区之前，预先判断一下
		if (pool.isEmpty())
			return factory.newObject();

		synchronized (pool) {
			// 进入临界区后需要重新判断容器是否为空
			if (!pool.isEmpty())
				return pool.removeFirst();
		}

		// 新建对象过程可能比较慢，放在临界区之外
		return factory.newObject();
	}

	/**
	 * 推荐使用这样的写法：
	 * T obj = ObjectPool.borrowObject();
	 * obj = ObjectPool.returnObject(obj);
	 * 在 returnObject的同时及时置空变量
	 *
	 * @return 一定是 null
	 */
	public T returnObject(T obj) {
		assert obj != null;
		if (obj == null || pool.size() >= factory.maxCountPooled())
			return null;

		// 清除对象可能比较慢，放在临界区之外
		factory.passivateObject(obj);

		synchronized (pool) {
			// 这里就不用再判断 maxCountPooled() 了，仅仅多
			// 几个对象应该不影响
			pool.addLast(obj);
		}
		return null;
	}

	public void clear() {
		synchronized (pool) {
			pool.clear();
		}
	}
}

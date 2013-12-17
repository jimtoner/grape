package grape.util.objpool;

import grape.lockfree.ConcurrentLinkedQueue;

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
	private final ConcurrentLinkedQueue<T> pool = new ConcurrentLinkedQueue<T>();
	// 对象工厂
	private final PoolableObjectFactory<T> factory;

	public ObjectPool(PoolableObjectFactory<T> factory) {
		this.factory = factory;
	}

	public T borrowObject() {
		// 进入同步代码之前，预先判断一下
		if (pool.size() == 0)
			return factory.newObject();

		// 从池中取对象
		T ret = pool.tryPop(); // 同步代码

		if (ret == null)
			return factory.newObject();
		return ret;
	}

	/**
	 * 推荐使用这样的写法：<br/>
	 * <code>
	 * T obj = ObjectPool.borrowObject(); <br/>
	 * obj = ObjectPool.returnObject(obj);
	 * </code><br/>
	 * 用 returnObject() 的返回值及时置变量为 null
	 *
	 * @return 一定是 null
	 */
	public T returnObject(T obj) {
		if (obj == null || pool.size() >= factory.maxPooled())
			return null;

		// 清理对象
		factory.passivateObject(obj);

		// 对象入池
		pool.tryPush(obj);

		return null;
	}

	public void clear() {
		pool.clear();
	}
}

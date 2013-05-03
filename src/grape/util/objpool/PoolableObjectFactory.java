package grape.util.objpool;

/**
 * 用于配置对象池
 *
 * @author jingqi
 */
public abstract class PoolableObjectFactory <T> {

	/**
	 * 新建对象
	 */
	public abstract T newObject();

	/**
	 * 还原对象状态，避免内存泄露和垃圾信息导致的不确定性问题
	 */
	public abstract void passivateObject(T obj);

	/**
	 * 最多缓存的对象数
	 */
	public int maxPooled() {
		return 10;
	}
}

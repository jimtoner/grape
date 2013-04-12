package grape.container.range;

/**
 * 有确定值索引的 RangeContainer
 *
 * @author jingqi
 */
public interface IndexedRangeContainer extends RangeContainer {

	/**
	 * 值个数
	 */
	int size();

	/**
	 * 按索引取值
	 */
	int getValue(int index);

	/**
	 * 取值的索引
	 *
	 * @return 值不存在于容器中则返回-1
	 */
	int indexOfValue(int value);
}

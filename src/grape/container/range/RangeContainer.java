package grape.container.range;

import java.util.Iterator;

/**
 * 用于存放整数范围，例如
 * (1,3) (6,6) (8,9)
 *
 * <ul>
 * <li>各个 range 之间不重叠</li>
 * <li>range之间按序排列</li>
 * <li>相邻两个 range 直接不相接</li>
 * </ul>
 *
 * @author jingqi
 */
public interface RangeContainer {

	/**
	 * 获取第一个数值
	 */
	int getFirstValue();

	/**
	 * 获取最后一个数值
	 */
	int getLastValue();

	/**
	 * 是否为空
	 */
	boolean isEmpty();

	/**
	 * 是否包含某个值
	 */
	boolean contains(int value);

	/**
	 * 是否完全包含某个 range 容器
	 */
	boolean contains(RangeContainer x);

	/**
	 * 添加值
	 */
	void addValue(int value);

	/**
	 * 添加值范围
	 */
	void addValueRange(int firstValue, int lastValue);
	void addValueRange(Range range);

	/**
	 * 删除值
	 */
	void removeValue(int value);

	/**
	 * 删除值范围
	 */
	void removeValueRange(int firstValue, int lastValue);
	void removeValueRange(Range range);

	/**
	 * 清空容器
	 */
	void clear();

	/**
	 * 做交集
	 * 例如:
	 * 容器 [(1,3),(5,10),(13,24)]
	 * 容器 [(2,13),(15,100)]
	 * 交集 [(2,3),(5,10),13,(15,24)]
	 */
	RangeContainer intersectWith(RangeContainer x);

	/**
	 * 做并集
	 */
	RangeContainer mergeWith(RangeContainer x);

	/**
	 * 求补集
	 */
	RangeContainer remainder(RangeContainer x);

	/**
	 * 按序迭代数据
	 */
	Iterator<Integer> iterator();

	/**
	 * 按序迭代指定区间的数据
	 */
	Iterator<Integer> iterator(int firstValue, int lastValue);
	Iterator<Integer> iterator(Range range);

	/**
	 * 按序迭代指定区间中不在本容器中的空隙
	 */
	Iterator<Integer> vacuumIterator(int firstValue, int lastValue);
	Iterator<Integer> vacuumIterator(Range range);

	/**
	 * 按序迭代所有的 range
	 */
	Iterator<Range> rangeIterator();
	Iterator<Range> rangeIterator(int firstValue, int lastValue);
	Iterator<Range> rangeIterator(Range valueRange);
}

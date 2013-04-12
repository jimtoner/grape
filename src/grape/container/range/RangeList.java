package grape.container.range;

import grape.container.primeval.list.LongArrayList;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 用来记录一系列连续或者离散的整数，例如
 * (1,3) (6,6) (8,9)
 *
 * @author jingqi
 */
public class RangeList extends AbstractRangeContainer implements RangeContainer, Cloneable, Serializable {

	private static final long serialVersionUID = -8874843405551285255L;

	/**
	 * 一个 [lef, right] 闭区间组成一个 range，该 range 用 long 类型存储，高32位为 left, 低32位为 right
	 */
	private LongArrayList ranges = new LongArrayList();

	private static long makeRange(int left, int right) {
		if (left > right)
			throw new IllegalArgumentException();
		return (((long) left) << 32) + right;
	}

	private static int getLeft(long range) {
		return (int) (range >> 32);
	}

	private static int getRight(long range) {
		return (int) (range & 0x0000FFFF);
	}

	@Override
	public int getFirstValue() {
		if (ranges.size() == 0)
			throw new IllegalStateException("list is empty");
		return getLeft(ranges.get(0));
	}

	@Override
	public int getLastValue() {
		if (ranges.size() == 0)
			throw new IllegalStateException("list is empty");
		return getRight(ranges.get(ranges.size() - 1));
	}

	@Override
	public boolean isEmpty() {
		return ranges.isEmpty();
	}

	@Override
	public boolean contains(int value) {
		return binarySearch(value) >= 0;
	}

	/**
	 * 使用值做二分查找，如果找到则返回找到的位置(>=0)，否则返回 (-insertPoint-1)，
	 * insertPoint 是用来做插入的位置
	 *
	 * @return 找到则返回 >=0，否则 <0
	 */
	private int binarySearch(int value) {
		int left = -1, right = ranges.size();
		while (left + 1 < right) {
			int middle = (left + right) / 2;
			long range = ranges.get(middle);
			if (getRight(range) < value)
				left = middle;
			else if (getLeft(range) > value)
				right = middle;
			else
				return middle;
		}
		return -(right + 1);
	}

	@Override
	public void addValueRange(int firstValue, int lastValue) {
		if (firstValue > lastValue)
			throw new IllegalArgumentException();

		// 对空容器优化
		if (ranges.size() == 0) {
			ranges.add(makeRange(firstValue, lastValue));
			return;
		}

		// 对于头部进行优化
		long range = ranges.get(0);
		int left = getLeft(range);
		int right = getRight(range);
		if (lastValue + 1 < left) {
			ranges.add(0, makeRange(firstValue, lastValue));
			return;
		} else if (lastValue <= right) {
			if (firstValue >= left)
				return;
			ranges.set(0, makeRange(firstValue, right));
			return;
		}

		// 对于末尾进行优化
		range = ranges.get(ranges.size() - 1);
		left = getLeft(range);
		right = getRight(range);
		if (firstValue - 1 > right) {
			ranges.add(makeRange(firstValue, lastValue));
			return;
		} else if (firstValue >= left) {
			if (lastValue <= right)
				return;
			ranges.set(ranges.size() - 1, makeRange(left, lastValue));
			return;
		}

		// 二分查找法确定可以合并的 range 范围
		int i1 = binarySearch(firstValue - 1), i2 = binarySearch(lastValue + 1);
		if (i1 < 0)
			i1 = -i1 - 1;
		if (i2 < 0)
			i2 = -i2 - 2;

		if (i1 <= i2) {
			int min_left = Math.min(firstValue, getLeft(ranges.get(i1)));
			int max_right = Math.max(lastValue, getRight(ranges.get(i2)));
			ranges.removeRange(i1, i2 + 1);
			ranges.add(i1, makeRange(min_left, max_right));
		} else {
			ranges.add(i1, makeRange(firstValue, lastValue));
		}
	}

	@Override
	public void removeValueRange(int firstValue, int lastValue) {
		if (firstValue > lastValue)
			throw new IllegalArgumentException();

		// 二分查找法确定范围
		int i1 = binarySearch(firstValue), i2 = binarySearch(lastValue);
		if (i1 < 0)
			i1 = -i1 - 1;
		if (i2 < 0)
			i2 = -i2 - 2;
		if (i1 < i2) {
			long range = ranges.get(i1);
			int left = getLeft(range);
			int right = getRight(range);
			if (left < firstValue) {
				ranges.set(i1, makeRange(left, firstValue - 1));
				++i1;
			}
			range = ranges.get(i2);
			left = getLeft(range);
			right = getRight(range);
			if (right > lastValue) {
				ranges.set(i2, makeRange(lastValue + 1, right));
				--i2;
			}
			if (i1 <= i2)
				ranges.removeRange(i1, i2 + 1);
			return;
		} else if (i1 == i2) {
			long range = ranges.get(i1);
			int left = getLeft(range);
			int right = getRight(range);
			if (firstValue <= left && lastValue >= right) {
				ranges.remove(i1);
				return;
			} else if (firstValue > left && lastValue < right) {
				ranges.add(i1 + 1, makeRange(lastValue + 1, right));
				ranges.set(i1, makeRange(left, firstValue - 1));
				return;
			} else if (firstValue <= left) {
				ranges.set(i1, makeRange(lastValue + 1, right));
				return;
			} else {
				ranges.set(i1, makeRange(left, firstValue - 1));
				return;
			}
		}

	}

	@Override
	public void clear() {
		ranges.clear();
	}

	@Override
	public Iterator<Range> rangeIterator() {
		return new Iterator<Range>() {

			int location = 0;

			@Override
			public boolean hasNext() {
				return location < ranges.size();
			}

			@Override
			public Range next() {
				if (!hasNext())
					throw new NoSuchElementException();
				long range = ranges.get(location++);
				return new Range(getLeft(range), getRight(range));
			}

			@Override
			public void remove() {
				ranges.remove(location--);
			}
		};
	}

	@Override
	public Iterator<Range> rangeIterator(final int firstValue, final int lastValue) {
		return new Iterator<Range>() {

			int location;
			int lastLocation;

			{
				location = binarySearch(firstValue);
				if (location < 0)
					location = -location - 1;
				lastLocation = binarySearch(lastValue);
				if (lastLocation < 0)
					lastLocation = -lastLocation - 2;
			}

			@Override
			public boolean hasNext() {
				return location <= lastLocation;
			}

			@Override
			public Range next() {
				if (!hasNext())
					throw new NoSuchElementException();
				long range = ranges.get(location++);
				return new Range(getLeft(range), getRight(range));
			}

			@Override
			public void remove() {
				ranges.remove(location--);
				--lastLocation;
			}
		};
	}

	/**
	 * 检查容器数据是否是一致的
	 */
	public boolean isValid() {
		for (int i = 0, size = ranges.size(); i < size; ++i) {
			long r = ranges.get(i);
			int left = getLeft(r);
			int right = getRight(r);
			if (left > right)
				return false;
			if (i != 0) {
				long br = ranges.get(i - 1);
				int bright = getRight(br);
				if (left <= bright + 1)
					return false;
			}
		}
		return true;
	}

	@Override
	public RangeList intersectWith(RangeContainer x) {
		RangeList ret = new RangeList();
		intersectWith(this, x, ret);
		return ret;
	}

	@Override
	public RangeList mergeWith(RangeContainer x) {
		RangeList ret = new RangeList();
		mergeWith(this, x, ret);
		return ret;
	}

	@Override
	public RangeList remainder(RangeContainer x) {
		RangeList ret = new RangeList();
		remainder(this, x, ret);
		return ret;
	}

	@Override
	public RangeList clone() {
		RangeList l = new RangeList();
		l.ranges = ranges.clone();
		return l;
	}
}

package grape.container.rangelist;

import grape.container.primeval.list.LongArrayList;

import java.util.Iterator;

/**
 * 用来记录一系列连续或者离散的整数，例如
 * 行号 1 2 3 6 8 9 将被整合为
 * (1,3) (6,6) (8,9)
 *
 * @author jingqi
 */
public class RangeList {

	/**
	 * 一个 [lef, right] 闭区间组成一个 pair，该 pair 用 long 类型存储，高32位为 left, 低32位为 right
	 */
	private LongArrayList ranges = new LongArrayList();

	private static long makeRange(int left, int right) {
		if (left > right)
			throw new IllegalArgumentException();
		return (((long) left) << 32) + right;
	}

	private static int getLeft(long pair) {
		return (int) (pair >> 32);
	}

	private static int getRight(long pair) {
		return (int) (pair & 0x0000FFFF);
	}

	/**
	 * 获取第一个数值
	 */
	public int getFirstValue() {
		if (ranges.size() == 0)
			throw new IllegalStateException("list is empty");
		return getLeft(ranges.get(0));
	}

	/**
	 * 获取最后一个数值
	 */
	public int getLastValue() {
		if (ranges.size() == 0)
			throw new IllegalStateException("list is empty");
		return getRight(ranges.get(ranges.size() - 1));
	}

	/**
	 * 获取元素数
	 * NOTE: <b>需要线性遍历元素</b>
	 */
	public int size() {
		int ret = 0;
		for (int i = 0, size = ranges.size(); i < size; ++i) {
			long pair = ranges.get(i);
			ret += getRight(pair) - getLeft(pair) + 1;
		}
		return ret;
	}

	public boolean isEmpty() {
		return ranges.isEmpty();
	}

	/**
	 * 获取指定位置的元素
	 * NOTE: <b>需要线性遍历元素</b>
	 */
	public int get(int index) {
		if (index < 0)
			throw new IndexOutOfBoundsException();

		for (int i = 0, size = ranges.size(); i < size; ++i) {
			long pair = ranges.get(i);
			int left = getLeft(pair);
			int len = getRight(pair) - left + 1;
			if (index < len)
				return left + index;
			else
				index -= len;
		}
		throw new IndexOutOfBoundsException();
	}

	/**
	 * 查找指定行号是否在该容器中
	 */
	public boolean contains(int value) {
		return binarySearch(value) >= 0;
	}

	public boolean contains(RangeList x) {
		return remainder(x, this).isEmpty();
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
			long pair = ranges.get(middle);
			if (getRight(pair) < value)
				left = middle;
			else if (getLeft(pair) > value)
				right = middle;
			else
				return middle;
		}
		return -(right + 1);
	}

	/**
	 * 查找指定的值的索引位置
	 * NOTE: <b>需要线性遍历元素</b>
	 *
	 * @return -1, 如果没有找到
	 */
	public int indexOf(int value) {
		int index = 0;
		for (int i = 0, size = ranges.size(); i < size; ++i) {
			long pair = ranges.get(i);
			int left = getLeft(pair), right = getRight(pair);
			if (value < left)
				return -1;
			else if (value <= right)
				return index + value - left;
			else
				index += right - left + 1;
		}
		return -1;
	}

	/**
	 * 添加
	 */
	public void add(int value) {
		addRange(value, value);
	}

	/**
	 * 删除
	 */
	public void remove(int value) {
		removeRange(value, 1);
	}

	/**
	 * 添加行范围(取并集)
	 */
	public void addRange(int start, int count) {
		if (count < 0)
			throw new IllegalArgumentException();
		else if (count == 0)
			return;

		// 对空容器优化
		int last = start + count - 1;
		if (ranges.size() == 0) {
			ranges.add(makeRange(start, last));
			return;
		}

		// 对于头部进行优化
		long pair = ranges.get(0);
		int left = getLeft(pair);
		int right = getRight(pair);
		if (last + 1 < left) {
			ranges.add(0, makeRange(start, last));
			return;
		} else if (last <= right) {
			if (start >= left)
				return;
			ranges.set(0, makeRange(start, right));
			return;
		}

		// 对于末尾进行优化
		pair = ranges.get(ranges.size() - 1);
		left = getLeft(pair);
		right = getRight(pair);
		if (start - 1 > right) {
			ranges.add(makeRange(start, last));
			return;
		} else if (start >= left) {
			if (last <= right)
				return;
			ranges.set(ranges.size() - 1, makeRange(left, last));
			return;
		}

		// 二分查找法确定可以合并的 range 范围
		int i1 = binarySearch(start - 1), i2 = binarySearch(start + count);
		if (i1 < 0)
			i1 = -i1 - 1;
		if (i2 < 0)
			i2 = -i2 - 2;

		if (i1 <= i2) {
			int min_left = Math.min(start, getLeft(ranges.get(i1)));
			int max_right = Math.max(start + count - 1, getRight(ranges.get(i2)));
			ranges.removeRange(i1, i2 + 1);
			ranges.add(i1, makeRange(min_left, max_right));
		} else {
			ranges.add(i1, makeRange(start, start + count - 1));
		}
	}

	/**
	 * 清除一个值范围
	 */
	public void removeRange(int start, int count) {
		if (count < 0)
			throw new IllegalArgumentException();
		else if (count == 0)
			return;

		// 二分查找法确定范围
		int last = start + count - 1;
		int i1 = binarySearch(start), i2 = binarySearch(last);
		if (i1 < 0)
			i1 = -i1 - 1;
		if (i2 < 0)
			i2 = -i2 - 2;
		if (i1 < i2) {
			long pair = ranges.get(i1);
			int left = getLeft(pair);
			int right = getRight(pair);
			if (left < start) {
				ranges.set(i1, makeRange(left, start - 1));
				++i1;
			}
			pair = ranges.get(i2);
			left = getLeft(pair);
			right = getRight(pair);
			if (right > last) {
				ranges.set(i2, makeRange(start + count, right));
				--i2;
			}
			if (i1 <= i2)
				ranges.removeRange(i1, i2 + 1);
			return;
		} else if (i1 == i2) {
			long pair = ranges.get(i1);
			int left = getLeft(pair);
			int right = getRight(pair);
			if (start <= left && last >= right) {
				ranges.remove(i1);
				return;
			} else if (start > left && last < right) {
				ranges.add(i1 + 1, makeRange(last + 1, right));
				ranges.set(i1, makeRange(left, start - 1));
				return;
			} else if (start <= left) {
				ranges.set(i1, makeRange(last + 1, right));
				return;
			} else {
				ranges.set(i1, makeRange(left, start - 1));
				return;
			}
		}

	}

	/**
	 * 两个容器做交集
	 * 例如
	 * 容器 [(1,3),(5,10),(13,24)]
	 * 容器 [(2,13),(15,100)]
	 * 交集 [(2,3),(5,10),13,(15,24)]
	 */
	public static RangeList intersectWith(RangeList x, RangeList y) {
		RangeList ret = new RangeList();
		int index1 = 0, index2 = 0;
		int state = 0; // 0 for none; 1 for single x; 2 for single y; 3 for x and y
		int firstOfInteract = 0;
		while (index1 / 2 < x.ranges.size() && index2 / 2 < y.ranges.size()) {
			long pair1 = x.ranges.get(index1 / 2);
			int value1 = (index1 % 2 == 0 ? getLeft(pair1) : getRight(pair1));
			long pair2 = y.ranges.get(index2 / 2);
			int value2 = (index2 % 2 == 0 ? getLeft(pair2) : getRight(pair2));

			// 对于边界重叠的情况，优先进入 state3
			switch (state) {
			case 0:
				assert index1 % 2 == 0;
				assert index2 % 2 == 0;
				if (value1 < value2) {
					state = 1;
					++index1;
				} else {
					state = 2;
					++index2;
				}
				break;

			case 1:
				assert index1 % 2 == 1;
				assert index2 % 2 == 0;
				if (value1 < value2) {
					state = 0;
					++index1;
				} else {
					state = 3;
					++index2;
					firstOfInteract = value2;
				}
				break;

			case 2:
				assert index1 % 2 == 0;
				assert index2 % 2 == 1;
				if (value1 <= value2) {
					state = 3;
					++index1;
					firstOfInteract = value1;
				} else {
					state = 0;
					++index2;
				}
				break;

			case 3:
				assert index1 % 2 == 1;
				assert index2 % 2 == 1;
				if (value1 < value2) {
					state = 2;
					++index1;
					ret.ranges.add(makeRange(firstOfInteract, value1));
				} else {
					state = 1;
					++index2;
					ret.ranges.add(makeRange(firstOfInteract, value2));
				}
				break;

			default:
				throw new IllegalStateException("Illegal value of state");
			}
		}
		return ret;
	}

	/**
	 * 两个容器做并集
	 */
	public static RangeList mergeWith(RangeList x, RangeList y) {
		RangeList ret = new RangeList();
		// 状态机基本上和intersectWith()方法中一样
		int index1 = 0, index2 = 0;
		int state = 0; // 0 for none; 1 for single x; 2 for single y; 3 for x and y
		int firstOfMerge = 0;
		while (index1 / 2 < x.ranges.size() || index2 / 2 < y.ranges.size()) {
			int value1, value2;
			if (index1 / 2 < x.ranges.size()) {
				long pair1 = x.ranges.get(index1 / 2);
				value1 = (index1 % 2 == 0 ? getLeft(pair1) : getRight(pair1));
			} else {
				value1 = Integer.MAX_VALUE;
			}

			if (index2 / 2 < y.ranges.size()) {
				long pair2 = y.ranges.get(index2 / 2);
				value2 = (index2 % 2 == 0 ? getLeft(pair2) : getRight(pair2));
			} else {
				value2 = Integer.MAX_VALUE;
			}

			// 对于边界重叠的情况，优先进入 state3
			switch (state) {
			case 0:
				assert index1 % 2 == 0;
				assert index2 % 2 == 0;
				if (value1 < value2) {
					state = 1;
					++index1;
					firstOfMerge = value1;
				} else {
					state = 2;
					++index2;
					firstOfMerge = value2;
				}
				break;

			case 1:
				assert index1 % 2 == 1;
				assert index2 % 2 == 0;
				if (value1 < value2) {
					state = 0;
					++index1;
					if (ret.ranges.size() > 0 && getRight(ret.ranges.get(ret.ranges.size() - 1)) + 1 == firstOfMerge)
						ret.ranges.set(ret.ranges.size() - 1, makeRange(getLeft(ret.ranges.get(ret.ranges.size() - 1)), value1));
					else
						ret.ranges.add(makeRange(firstOfMerge, value1));
				} else {
					state = 3;
					++index2;
				}
				break;

			case 2:
				assert index1 % 2 == 0;
				assert index2 % 2 == 1;
				if (value1 <= value2) {
					state = 3;
					++index1;
				} else {
					state = 0;
					++index2;
					if (ret.ranges.size() > 0 && getRight(ret.ranges.get(ret.ranges.size() - 1)) + 1 == firstOfMerge)
						ret.ranges.set(ret.ranges.size() - 1, makeRange(getLeft(ret.ranges.get(ret.ranges.size() - 1)), value2));
					else
						ret.ranges.add(makeRange(firstOfMerge, value2));
				}
				break;

			case 3:
				assert index1 % 2 == 1;
				assert index2 % 2 == 1;
				if (value1 < value2) {
					state = 2;
					++index1;
				} else {
					state = 1;
					++index2;
				}
				break;

			default:
				throw new IllegalStateException("Illegal value of state");
			}
		}
		return ret;
	}

	/**
	 * 两个容器做补集
	 * {x} - {y}
	 */
	public static RangeList remainder(RangeList x, RangeList y) {
		RangeList ret = new RangeList();
		int index1 = 0, index2 = 0;
		int state = 0;
		int firstOfRemainder = 0;
		while (index1 / 2 < x.ranges.size()) {
			int value1, value2;
			if (index1 / 2 < x.ranges.size()) {
				long pair1 = x.ranges.get(index1 / 2);
				value1 = (index1 % 2 == 0 ? getLeft(pair1) : getRight(pair1));
			} else {
				value1 = Integer.MAX_VALUE;
			}

			if (index2 / 2 < y.ranges.size()) {
				long pair2 = y.ranges.get(index2 / 2);
				value2 = (index2 % 2 == 0 ? getLeft(pair2) : getRight(pair2));
			} else {
				value2 = Integer.MAX_VALUE;
			}

			// 对于边界重叠的情况，优先进入 state3 和 state2
			switch (state) {
			case 0:
				assert index1 % 2 == 0;
				assert index2 % 2 == 0;
				if (value1 < value2) {
					state = 1;
					++index1;
					firstOfRemainder = value1;
				} else {
					state = 2;
					++index2;
				}
				break;

			case 1:
				assert index1 % 2 == 1;
				assert index2 % 2 == 0;
				if (value1 < value2) {
					state = 0;
					++index1;
					ret.ranges.add(makeRange(firstOfRemainder, value1));
				} else {
					state = 3;
					++index2;
					ret.ranges.add(makeRange(firstOfRemainder, value2 - 1));
				}
				break;

			case 2:
				assert index1 % 2 == 0;
				assert index2 % 2 == 1;
				if (value1 <= value2) {
					state = 3;
					++index1;
				} else {
					state = 0;
					++index2;
				}
				break;

			case 3:
				assert index1 % 2 == 1;
				assert index2 % 2 == 1;
				if (value1 <= value2) {
					state = 2;
					++index1;
				} else {
					state = 1;
					++index2;
					firstOfRemainder = value2 + 1;
				}
				break;

			default:
				throw new IllegalStateException("Illegal value of state");
			}
		}
		return ret;
	}

	/**
	 * 清空容器
	 */
	public void clear() {
		ranges.clear();
	}

	/**
	 * 迭代数据
	 */
	public Iterator<Integer> iterator() {
		return new Iterator<Integer>() {

			int currentValue;
			int nextPairIndex, secondOfPair;
			int nextValue;

			{
				currentValue = -1;
				nextPairIndex = 0;
				if (nextPairIndex < RangeList.this.ranges.size()) {
					long pair = RangeList.this.ranges.get(nextPairIndex);
					nextValue = RangeList.getLeft(pair);
					secondOfPair = RangeList.getRight(pair);
				}
			}

			@Override
			public boolean hasNext() {
				return nextPairIndex < RangeList.this.ranges.size();
			}

			@Override
			public Integer next() {
				if (!hasNext())
					throw new IllegalStateException("There is NO next step!");

				currentValue = nextValue;

				++nextValue;
				if (nextValue > secondOfPair) {
					++nextPairIndex;
					if (nextPairIndex < RangeList.this.ranges.size()) {
						long pair = RangeList.this.ranges.get(nextPairIndex);
						nextValue = RangeList.getLeft(pair);
						secondOfPair = RangeList.getRight(pair);
					}
				}

				return currentValue;
			}

			@Override
			public void remove() {
				throw new RuntimeException("Don\'t call this method!");
			}
		};
	}

	/**
	 * 迭代指定区间的数据
	 */
	public Iterator<Integer> iterator(final int firstValue, final int lastValue) {
		// 用二分法找到迭代的起点
		int left = -1, right = ranges.size();
		while (left + 1 < right) {
			int middle = (left + right) / 2;
			long middlePair = ranges.get(middle);
			int first = getLeft(middlePair);
			int second = getRight(middlePair);
			if (firstValue < first) {
				right = middle;
			} else if (firstValue > second) {
				left = middle;
			} else {
				right = middle;
				break;
			}
		}
		final int startPairIndex = right;

		// 生成迭代器
		return new Iterator<Integer>() {

			int currentValue;
			int nextPairIndex, secondOfPair;
			int nextValue;

			{
				currentValue = -1;
				nextValue = lastValue + 1;
				nextPairIndex = startPairIndex;
				if (nextPairIndex < RangeList.this.ranges.size()) {
					long pair = RangeList.this.ranges.get(nextPairIndex);
					int firstOfpair = RangeList.getLeft(pair);
					secondOfPair = RangeList.getRight(pair);
					nextValue = Math.max(firstOfpair, firstValue);
				}
			}

			@Override
			public boolean hasNext() {
				return nextValue <= lastValue;
			}

			@Override
			public Integer next() {
				if (!hasNext())
					throw new IllegalStateException("There is NO next step!");

				currentValue = nextValue;
				++nextValue;
				if (nextValue > secondOfPair) {
					++nextPairIndex;
					nextValue = lastValue + 1;
					if (nextPairIndex < RangeList.this.ranges.size()) {
						long pair = RangeList.this.ranges.get(nextPairIndex);
						int firstOfpair = RangeList.getLeft(pair);
						secondOfPair = RangeList.getRight(pair);
						nextValue = firstOfpair;
					}
				}

				return currentValue;
			}

			@Override
			public void remove() {
				throw new RuntimeException("Don\'t call this method!");
			}
		};
	}

	/**
	 * 迭代指定区间中不在本容器中的空隙
	 */
	public Iterator<Integer> vacuum_iterator(final int firstValue, final int lastValue) {
		// 二插查找法找到迭代起点
		int left = -1, right = ranges.size();
		while (left + 1 < right) {
			int middle = (left + right) / 2;
			long pair = ranges.get(middle);
			int first = getLeft(pair);
			int second = getRight(pair);
			if (firstValue < first) {
				right = middle;
			} else if (firstValue > second) {
				left = middle;
			} else {
				right = middle + 1;
				break;
			}
		}
		final int startPairIndex = right;

		// 生成迭代器
		return new Iterator<Integer>() {

			int currentValue;
			int nextValue;
			int rightBoundPairIndex, rightBound, nextStart;

			{
				currentValue = -1;
				nextValue = lastValue + 1;
				rightBoundPairIndex = startPairIndex;
				if (rightBoundPairIndex - 1 >= 0) {
					long pair = RangeList.this.ranges.get(rightBoundPairIndex - 1);
					int second = getRight(pair);
					nextValue = Math.max(second + 1, firstValue);
				} else {
					nextValue = firstValue;
				}

				rightBound = lastValue + 1;
				nextStart = lastValue + 1;
				if (rightBoundPairIndex < RangeList.this.ranges.size()) {
					long pair = RangeList.this.ranges.get(rightBoundPairIndex);
					rightBound = getLeft(pair);
					nextStart = getRight(pair);
				}
			}

			@Override
			public boolean hasNext() {
				return nextValue <= lastValue;
			}

			@Override
			public Integer next() {
				if (!hasNext())
					throw new IllegalStateException("There is No next step!");

				currentValue = nextValue;

				++nextValue;
				if (nextValue >= rightBound) {
					nextValue = nextStart + 1;
					++rightBoundPairIndex;
					rightBound = lastValue + 1;
					nextStart = lastValue + 1;
					if (rightBoundPairIndex < RangeList.this.ranges.size()) {
						long pair = RangeList.this.ranges.get(rightBoundPairIndex);
						rightBound = getLeft(pair);
						nextStart = getRight(pair);
					}
				}

				return currentValue;
			}

			@Override
			public void remove() {
				throw new IllegalStateException("Don\'t call this method!");
			}
		};
	}

	/**
	 * 检查容器数据是否是一致的
	 */
	public boolean isValid() {
		for (int i = 0, size = ranges.size(); i < size; ++i) {
			long pair = ranges.get(i);
			int left = getLeft(pair);
			int right = getRight(pair);
			if (left > right)
				return false;
			if (i != 0) {
				long bpair = ranges.get(i - 1);
				int bright = getRight(bpair);
				if (left - 1 <= bright)
					return false;
			}
		}
		return true;
	}

	@Override
	public RangeList clone() {
		RangeList l = new RangeList();
		l.ranges = ranges.clone();
		return l;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0, size = ranges.size(); i < size; ++i) {
			long pair = ranges.get(i);
			int first = getLeft(pair);
			int second = getRight(pair);
			if (first == second)
				sb.append(Integer.toString(first));
			else
				sb.append("(").append(Integer.toString(first)).append(",")
				.append(Integer.toString(second)).append(")");
			if (i != size - 1)
				sb.append(",");
		}
		sb.append("]");
		return sb.toString();
	}
}

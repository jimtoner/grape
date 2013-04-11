package grape.container.rangelist;

import grape.container.primeval.list.LongArrayList;

import java.io.Serializable;
import java.util.Iterator;

/**
 * 用来记录一系列连续或者离散的整数，例如
 * 行号 1 2 3 6 8 9 将被整合为
 * (1,3) (6,6) (8,9)
 *
 * @author jingqi
 */
public class RangeList implements Cloneable, Serializable {

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

	public boolean isEmpty() {
		return ranges.isEmpty();
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

	/**
	 * 添加
	 */
	public void addValue(int value) {
		addValueRange(value, value);
	}

	/**
	 * 删除
	 */
	public void removeValue(int value) {
		removeValueRange(value, value);
	}

	/**
	 * 添加行范围(取并集)
	 */
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

	/**
	 * 清除一个值范围
	 */
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
			long range1 = x.ranges.get(index1 / 2);
			int value1 = (index1 % 2 == 0 ? getLeft(range1) : getRight(range1));
			long range2 = y.ranges.get(index2 / 2);
			int value2 = (index2 % 2 == 0 ? getLeft(range2) : getRight(range2));

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
				long range1 = x.ranges.get(index1 / 2);
				value1 = (index1 % 2 == 0 ? getLeft(range1) : getRight(range1));
			} else {
				value1 = Integer.MAX_VALUE;
			}

			if (index2 / 2 < y.ranges.size()) {
				long range2 = y.ranges.get(index2 / 2);
				value2 = (index2 % 2 == 0 ? getLeft(range2) : getRight(range2));
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
				long range1 = x.ranges.get(index1 / 2);
				value1 = (index1 % 2 == 0 ? getLeft(range1) : getRight(range1));
			} else {
				value1 = Integer.MAX_VALUE;
			}

			if (index2 / 2 < y.ranges.size()) {
				long range2 = y.ranges.get(index2 / 2);
				value2 = (index2 % 2 == 0 ? getLeft(range2) : getRight(range2));
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
			int nextLocation, rightOfRange;
			int nextValue;

			{
				currentValue = -1;
				nextLocation = 0;
				if (nextLocation < RangeList.this.ranges.size()) {
					long r = RangeList.this.ranges.get(nextLocation);
					nextValue = RangeList.getLeft(r);
					rightOfRange = RangeList.getRight(r);
				}
			}

			@Override
			public boolean hasNext() {
				return nextLocation < RangeList.this.ranges.size();
			}

			@Override
			public Integer next() {
				if (!hasNext())
					throw new IllegalStateException("There is NO next step!");

				currentValue = nextValue;

				++nextValue;
				if (nextValue > rightOfRange) {
					++nextLocation;
					if (nextLocation < RangeList.this.ranges.size()) {
						long range = RangeList.this.ranges.get(nextLocation);
						nextValue = RangeList.getLeft(range);
						rightOfRange = RangeList.getRight(range);
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
			long middleRange = ranges.get(middle);
			int first = getLeft(middleRange);
			int second = getRight(middleRange);
			if (firstValue < first) {
				right = middle;
			} else if (firstValue > second) {
				left = middle;
			} else {
				right = middle;
				break;
			}
		}
		final int startLocation = right;

		// 生成迭代器
		return new Iterator<Integer>() {

			int currentValue;
			int nextLocation, rightOfRange;
			int nextValue;

			{
				currentValue = -1;
				nextValue = lastValue + 1;
				nextLocation = startLocation;
				if (nextLocation < RangeList.this.ranges.size()) {
					long r = RangeList.this.ranges.get(nextLocation);
					int leftOfRange = RangeList.getLeft(r);
					rightOfRange = RangeList.getRight(r);
					nextValue = Math.max(leftOfRange, firstValue);
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
				if (nextValue > rightOfRange) {
					++nextLocation;
					nextValue = lastValue + 1;
					if (nextLocation < RangeList.this.ranges.size()) {
						long r = RangeList.this.ranges.get(nextLocation);
						int leftOfRange = RangeList.getLeft(r);
						rightOfRange = RangeList.getRight(r);
						nextValue = leftOfRange;
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
			long range = ranges.get(middle);
			int first = getLeft(range);
			int second = getRight(range);
			if (firstValue < first) {
				right = middle;
			} else if (firstValue > second) {
				left = middle;
			} else {
				right = middle + 1;
				break;
			}
		}
		final int startLocation = right;

		// 生成迭代器
		return new Iterator<Integer>() {

			int currentValue;
			int nextValue;
			int rightBoundLocation, rightBound, nextStart;

			{
				currentValue = -1;
				nextValue = lastValue + 1;
				rightBoundLocation = startLocation;
				if (rightBoundLocation - 1 >= 0) {
					long r = RangeList.this.ranges.get(rightBoundLocation - 1);
					int second = getRight(r);
					nextValue = Math.max(second + 1, firstValue);
				} else {
					nextValue = firstValue;
				}

				rightBound = lastValue + 1;
				nextStart = lastValue + 1;
				if (rightBoundLocation < RangeList.this.ranges.size()) {
					long r = RangeList.this.ranges.get(rightBoundLocation);
					rightBound = getLeft(r);
					nextStart = getRight(r);
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
					++rightBoundLocation;
					rightBound = lastValue + 1;
					nextStart = lastValue + 1;
					if (rightBoundLocation < RangeList.this.ranges.size()) {
						long r = RangeList.this.ranges.get(rightBoundLocation);
						rightBound = getLeft(r);
						nextStart = getRight(r);
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
	public RangeList clone() {
		RangeList l = new RangeList();
		l.ranges = ranges.clone();
		return l;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof RangeList))
			return false;
		RangeList x = (RangeList) o;
		return ranges.equals(x.ranges);
	}

	@Override
	public int hashCode() {
		return ranges.hashCode();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0, size = ranges.size(); i < size; ++i) {
			long r = ranges.get(i);
			int first = getLeft(r);
			int second = getRight(r);
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

package grape.container;

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
	private LongArrayList pairList = new LongArrayList();

	private static long makePair(int left, int right) {
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
	public int getFirst() {
		if (pairList.size() == 0)
			throw new IllegalStateException("list is empty");
		return getLeft(pairList.get(0));
	}

	/**
	 * 获取最后一个数值
	 */
	public int getLast() {
		if (pairList.size() == 0)
			throw new IllegalStateException("list is empty");
		return getRight(pairList.get(pairList.size() - 1));
	}

	/**
	 * 查找指定行号是否在该容器中
	 */
	public boolean contains(int value) {
		return positionOf(value) >= 0;
	}

	/**
	 * 查找指定的值的存储位置
	 *
	 * @return -1, 如果没有找到
	 */
	private int positionOf(int value) {
		// 二分查找
		int leftBound = -1, rightBound = pairList.size();
		while (leftBound + 1 < rightBound) {
			int middle = (leftBound + rightBound) / 2;
			long middlePair = pairList.get(middle);
			if (value < getLeft(middlePair))
				rightBound = middle;
			else if (value > getRight(middlePair))
				leftBound = middle;
			else
				return middle;
		}
		return -1;
	}

	/**
	 * 在末尾添加一个整数(用于优化 add()，该整数必须大于列表中已有的最的大数)
	 */
	public void append(int value) {
		appendRange(value, value);
	}

	/**
	 * 在末尾添加一个范围的行号(用于优化 add()，该整数范围必须大于列表中已有的最大的数)
	 */
	public void appendRange(int first, int last) {
		if (first > last)
			throw new IllegalArgumentException();

		if (pairList.size() == 0) {
			pairList.add(makePair(first, last));
			return;
		}

		long lastPair = pairList.get(pairList.size() - 1);
		int left = getLeft(lastPair);
		int right = getRight(lastPair);
		if (first <= right)
			throw new IllegalArgumentException("you should call add() to add this row");

		if (first == right + 1)
			pairList.set(pairList.size() - 1, makePair(left, last));
		else
			pairList.add(makePair(first, last));
	}

	/**
	 * 添加(取并集)
	 */
	public void add(int value) {
		addRange(value, value);
	}

	/**
	 * 添加行范围(取并集)
	 */
	public void addRange(int first, int last) {
		assert first <= last;
		if (pairList.size() == 0) {
			pairList.add(makePair(first, last));
			return;
		}

		// 对于末尾进行优化
		long lastPair = pairList.get(pairList.size() - 1);
		int left = getLeft(lastPair);
		int right = getRight(lastPair);
		if (first == right + 1)
			pairList.set(pairList.size() - 1, makePair(left, last));
		else if (first > right + 1)
			pairList.add(makePair(first, last));

		// 对于头部进行优化
		long firstPair = pairList.get(0);
		left = getLeft(firstPair);
		right = getRight(firstPair);
		if (last == left - 1)
			pairList.set(0, makePair(first, right));
		else if (last < left - 1)
			pairList.add(0, makePair(first, last));

		// 合并
		LongArrayList newList = new LongArrayList();
		int index1 = 0, index2 = 0;
		int state = 0;  // 0 for none; 1 for single x; 2 for single y; 3 for x and y
		int firstOfMerge = 0;
		while (index1 / 2 < pairList.size() || index2 < 2) {
			int value1, value2;
			if (index1 / 2 < pairList.size()) {
				long pair1 = pairList.get(index1 / 2);
				value1 = (index1 % 2 == 0 ? getLeft(pair1) : getRight(pair1));
			} else {
				value1 = Integer.MAX_VALUE;
			}
			if (index2 < 2)
				value2 = (index2 % 2 == 0 ? first : last);
			else
				value2 = Integer.MAX_VALUE;

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
					newList.add(makePair(firstOfMerge, value1));
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
					newList.add(makePair(firstOfMerge, value2));
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
		pairList = newList;
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
		while (index1 / 2 < x.pairList.size() && index2 / 2 < y.pairList.size()) {
			long pair1 = x.pairList.get(index1 / 2);
			int value1 = (index1 % 2 == 0 ? getLeft(pair1) : getRight(pair1));
			long pair2 = y.pairList.get(index2 / 2);
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
					ret.pairList.add(makePair(firstOfInteract, value1));
				} else {
					state = 1;
					++index2;
					ret.pairList.add(makePair(firstOfInteract, value2));
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
		while (index1 / 2 < x.pairList.size() || index2 / 2 < y.pairList.size()) {
			int value1, value2;
			if (index1 / 2 < x.pairList.size()) {
				long pair1 = x.pairList.get(index1 / 2);
				value1 = (index1 % 2 == 0 ? getLeft(pair1) : getRight(pair1));
			} else {
				value1 = Integer.MAX_VALUE;
			}

			if (index2 / 2 < y.pairList.size()) {
				long pair2 = y.pairList.get(index2 / 2);
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
					if (ret.pairList.size() > 0 && getRight(ret.pairList.get(ret.pairList.size() - 1)) + 1 == firstOfMerge)
						ret.pairList.set(ret.pairList.size() - 1, makePair(getLeft(ret.pairList.get(ret.pairList.size() - 1)), value1));
					else
						ret.pairList.add(makePair(firstOfMerge, value1));
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
					if (ret.pairList.size() > 0 && getRight(ret.pairList.get(ret.pairList.size() - 1)) + 1 == firstOfMerge)
						ret.pairList.set(ret.pairList.size() - 1, makePair(getLeft(ret.pairList.get(ret.pairList.size() - 1)), value2));
					else
						ret.pairList.add(makePair(firstOfMerge, value2));
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
		while (index1 / 2 < x.pairList.size() || index2 / 2 < y.pairList.size()) {
			int value1, value2;
			if (index1 / 2 < x.pairList.size()) {
				long pair1 = x.pairList.get(index1 / 2);
				value1 = (index1 % 2 == 0 ? getLeft(pair1) : getRight(pair1));
			} else {
				value1 = Integer.MAX_VALUE;
			}

			if (index2 / 2 < y.pairList.size()) {
				long pair2 = y.pairList.get(index2 / 2);
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
					ret.pairList.add(makePair(firstOfRemainder, value1));
				} else {
					state = 3;
					++index2;
					ret.pairList.add(makePair(firstOfRemainder, value2 - 1));
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
		pairList.clear();
	}

	/**
	 * 迭代数据
	 */
	public Iterator<Integer> iterator() {
		return new Iterator<Integer>() {

			int currentRow;
			int nextPairIndex, secondOfPair;
			int nextRow;

			{
				currentRow = -1;
				nextPairIndex = 0;
				if (nextPairIndex < RangeList.this.pairList.size()) {
					long pair = RangeList.this.pairList.get(nextPairIndex);
					nextRow = RangeList.getLeft(pair);
					secondOfPair = RangeList.getRight(pair);
				}
			}

			@Override
			public boolean hasNext() {
				return nextPairIndex < RangeList.this.pairList.size();
			}

			@Override
			public Integer next() {
				if (!hasNext())
					throw new IllegalStateException("There is NO next step!");

				currentRow = nextRow;

				++nextRow;
				if (nextRow > secondOfPair) {
					++nextPairIndex;
					if (nextPairIndex < RangeList.this.pairList.size()) {
						long pair = RangeList.this.pairList.get(nextPairIndex);
						nextRow = RangeList.getLeft(pair);
						secondOfPair = RangeList.getRight(pair);
					}
				}

				return currentRow;
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
	public Iterator<Integer> iterator(final int firstRow, final int lastRow) {
		// 用二分法找到迭代的起点
		int left = -1, right = pairList.size();
		while (left + 1 < right) {
			int middle = (left + right) / 2;
			long middlePair = pairList.get(middle);
			int first = getLeft(middlePair);
			int second = getRight(middlePair);
			if (firstRow < first) {
				right = middle;
			} else if (firstRow > second) {
				left = middle;
			} else {
				right = middle;
				break;
			}
		}
		final int startPairIndex = right;

		// 生成迭代器
		return new Iterator<Integer>() {

			int currentRow;
			int nextPairIndex, secondOfPair;
			int nextRow;

			{
				currentRow = -1;
				nextRow = lastRow + 1;
				nextPairIndex = startPairIndex;
				if (nextPairIndex < RangeList.this.pairList.size()) {
					long pair = RangeList.this.pairList.get(nextPairIndex);
					int firstOfpair = RangeList.getLeft(pair);
					secondOfPair = RangeList.getRight(pair);
					nextRow = Math.max(firstOfpair, firstRow);
				}
			}

			@Override
			public boolean hasNext() {
				return nextRow <= lastRow;
			}

			@Override
			public Integer next() {
				if (!hasNext())
					throw new IllegalStateException("There is NO next step!");

				currentRow = nextRow;
				++nextRow;
				if (nextRow > secondOfPair) {
					++nextPairIndex;
					nextRow = lastRow + 1;
					if (nextPairIndex < RangeList.this.pairList.size()) {
						long pair = RangeList.this.pairList.get(nextPairIndex);
						int firstOfpair = RangeList.getLeft(pair);
						secondOfPair = RangeList.getRight(pair);
						nextRow = firstOfpair;
					}
				}

				return currentRow;
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
	public Iterator<Integer> vacuum_iterator(final int firstRow, final int lastRow) {
		// 二插查找法找到迭代起点
		int left = -1, right = pairList.size();
		while (left + 1 < right) {
			int middle = (left + right) / 2;
			long pair = pairList.get(middle);
			int first = getLeft(pair);
			int second = getRight(pair);
			if (firstRow < first) {
				right = middle;
			} else if (firstRow > second) {
				left = middle;
			} else {
				right = middle + 1;
				break;
			}
		}
		final int startPairIndex = right;

		// 生成迭代器
		return new Iterator<Integer>() {

			int currentRow;
			int nextRow;
			int rightBoundPairIndex, rightBound, nextStart;

			{
				currentRow = -1;
				nextRow = lastRow + 1;
				rightBoundPairIndex = startPairIndex;
				if (rightBoundPairIndex - 1 >= 0) {
					long pair = RangeList.this.pairList.get(rightBoundPairIndex - 1);
					int second = getRight(pair);
					nextRow = Math.max(second + 1, firstRow);
				} else {
					nextRow = firstRow;
				}

				rightBound = lastRow + 1;
				nextStart = lastRow + 1;
				if (rightBoundPairIndex < RangeList.this.pairList.size()) {
					long pair = RangeList.this.pairList.get(rightBoundPairIndex);
					rightBound = getLeft(pair);
					nextStart = getRight(pair);
				}
			}

			@Override
			public boolean hasNext() {
				return nextRow <= lastRow;
			}

			@Override
			public Integer next() {
				if (!hasNext())
					throw new IllegalStateException("There is No next step!");

				currentRow = nextRow;

				++nextRow;
				if (nextRow >= rightBound) {
					nextRow = nextStart + 1;
					++rightBoundPairIndex;
					rightBound = lastRow + 1;
					nextStart = lastRow + 1;
					if (rightBoundPairIndex < RangeList.this.pairList.size()) {
						long pair = RangeList.this.pairList.get(rightBoundPairIndex);
						rightBound = getLeft(pair);
						nextStart = getRight(pair);
					}
				}

				return currentRow;
			}

			@Override
			public void remove() {
				throw new IllegalStateException("Don\'t call this method!");
			}
		};
	}

	@Override
	public RangeList clone() {
		RangeList l = new RangeList();
		l.pairList = pairList.clone();
		return l;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0, size = pairList.size(); i < size; ++i) {
			long pair = pairList.get(i);
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
package grape.container.rangelist;

import grape.container.deqlist.ArrayDequeList;
import grape.container.deqlist.DequeList;

import java.util.Iterator;

/**
 * 存储一系列有序的范围，例如
 * (1,3) (6,6) (8,9)
 *
 * @author jingqi
 *
 */
public class IndexedRangeList {

	private static class Range {
		int index; // 第一个 value 的索引
		int valueStart; // 第一个 value
		int valueCount; // value 数目

		public Range(int index, int valueStart, int valueCount) {
			this.index = index;
			this.valueStart = valueStart;
			this.valueCount = valueCount;
		}

		public int getFirstValue() {
			return valueStart;
		}

		public int getLastValue() {
			return valueStart + valueCount - 1;
		}

		@Override
		public Range clone() {
			return new Range(index, valueStart, valueCount);
		}

		@Override
		public String toString() {
			if (valueCount == 1)
				return Integer.toString(valueStart);
			return "(" + Integer.toString(getFirstValue()) + "," +
				Integer.toString(getLastValue()) + ")";
		}
	}

	private DequeList<Range> ranges = new ArrayDequeList<Range>();

	/**
	 * 获取第一个值
	 */
	public int getFirstValue() {
		if (ranges.size() == 0)
			throw new IllegalStateException("empty");
		return ranges.get(0).getFirstValue();
	}

	/**
	 * 获取最后一个值
	 */
	public int getLastValue() {
		if (ranges.size() == 0)
			throw new IllegalStateException("empty");
		Range r = ranges.get(ranges.size() - 1);
		return r.getLastValue();
	}

	/**
	 * 值的数目
	 */
	public int size() {
		if (ranges.size() == 0)
			return 0;
		Range r = ranges.get(ranges.size() - 1);
		return r.index + r.valueCount;
	}

	public boolean isEmpty() {
		return ranges.isEmpty();
	}

	/**
	 * 获取指定位置的值
	 */
	public int get(int index) {
		if (index < 0)
			throw new IndexOutOfBoundsException();

		// 二分查找
		int left = -1, right = ranges.size();
		while (left + 1 < right) {
			int middle = (left + right) / 2;
			Range r = ranges.get(middle);
			if (r.index + r.valueCount <= index)
				left = middle;
			else if (r.index > index)
				right = middle;
			else
				return r.valueStart + (index - r.index);
		}
		throw new IndexOutOfBoundsException();
	}

	/**
	 * 查找指定值是否在该容器中
	 */
	public boolean contains(int value) {
		return binaraySearch(value) >= 0;
	}

	public boolean contains(IndexedRangeList x) {
		return remainder(x, this).isEmpty();
	}

	/**
	 * 查找指定值的索引位置
	 */
	public int indexOf(int value) {
		int location = binaraySearch(value);
		if (location < 0)
			return -1;
		Range r = ranges.get(location);
		return r.index + (value - r.valueStart);
	}

	/**
	 * 添加值
	 */
	public void add(int value) {
		addRange(value, 1);
	}

	public void remove(int value) {
		removeRange(value, 1);
	}

	/**
	 * 更新指定范围的 index 缓存
	 */
	private void updateIndex(int locationStart, int count) {
		if (locationStart < 0 || count < 0 || locationStart + count > ranges.size())
			throw new IllegalArgumentException();

		for (int i = 0; i < count; ++i) {
			Range r = ranges.get(locationStart + i);
			if (locationStart + i == 0) {
				r.index = 0;
			} else {
				Range before = ranges.get(locationStart + i - 1);
				r.index = before.index + before.valueCount;
			}
		}
	}

	private void updateIndex(int locationStart) {
		updateIndex(locationStart, ranges.size() - locationStart);
	}

	/**
	 * 使用值做二分查找，如果找到则返回找到的位置(>=0)，否则返回 (-insertPoint-1)，
	 * insertPoint 是用来做插入的位置
	 *
	 * @return 找到则返回 >=0，否则 <0
	 */
	private int binaraySearch(int value) {
		int left = -1, right = ranges.size();
		while (left + 1 < right) {
			int middle = (left + right) / 2;
			Range r = ranges.get(middle);
			if (r.getLastValue() < value)
				left = middle;
			else if (r.getFirstValue() > value)
				right = middle;
			else
				return middle;
		}
		return -(right + 1);
	}

	/**
	 * 添加一个值范围
	 */
	public void addRange(int start, int count) {
		if (count < 0)
			throw new IllegalArgumentException();
		else if (count == 0)
			return;

		// 对空容器优化
		if (ranges.size() == 0) {
			ranges.add(new Range(0, start, count));
			return;
		}

		// 对头部插入优化
		Range r = ranges.get(0);
		int last = start + count - 1;
		if (last + 1 < r.getFirstValue()) {
			ranges.add(0, new Range(0, start, count));
			updateIndex(1);
			return;
		} else if (last <= r.getLastValue()) {
			if (start >= r.getFirstValue())
				return;
			int x = r.getFirstValue() - start;
			r.valueStart = start;
			r.valueCount += x;
			updateIndex(1);
			return;
		}

		// 对尾部插入优化
		r = ranges.get(ranges.size() - 1);
		if (start - 1 > r.getLastValue()) {
			ranges.add(new Range(r.index + r.valueCount, start, count));
			return;
		} else if (start >= r.getFirstValue()) {
			if (last <= r.getLastValue())
				return;
			int x = last - r.getLastValue();
			r.valueCount += x;
			return;
		}

		// 二分查找法确定可以合并的 range 范围
		int i1 = binaraySearch(start - 1), i2 = binaraySearch(start + count);
		if (i1 < 0)
			i1 = -i1 - 1;
		if (i2 < 0)
			i2 = -i2 - 2;

		if (i1 <= i2) {
			int min_start = Math.min(start, ranges.get(i1).getFirstValue());
			int max_count = Math.max(start + count, ranges.get(i2).getLastValue() + 1) - min_start;
			ranges.subList(i1, i2 + 1).clear();
			ranges.add(i1, new Range(0, min_start, max_count));
		} else {
			ranges.add(i1, new Range(0, start, count));
		}
		updateIndex(i1);
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
		int i1 = binaraySearch(start), i2 = binaraySearch(last);
		if (i1 < 0)
			i1 = -i1 - 1;
		if (i2 < 0)
			i2 = -i2 - 2;
		if (i1 < i2) {
			Range r = ranges.get(i1);
			if (r.valueStart < start) {
				r.valueCount = start - r.valueStart;
				++i1;
			}
			r = ranges.get(i2);
			if (r.getLastValue() > last) {
				r.valueCount = r.getLastValue() - last;
				r.valueStart = start + count;
				--i2;
			}
			if (i1 <= i2)
				ranges.subList(i1, i2 + 1).clear();
			updateIndex(i1);
			return;
		} else if (i1 == i2) {
			Range r = ranges.get(i1);
			if (start <= r.getFirstValue() && last >= r.getLastValue()) {
				ranges.remove(i1);
				updateIndex(i1);
				return;
			} else if (start > r.getFirstValue() && last < r.getLastValue()) {
				ranges.add(i1 + 1, new Range(0, last + 1, r.getLastValue() - last));
				r.valueCount = start - r.valueStart;
				updateIndex(i1);
				return;
			} else if (start <= r.getFirstValue()) {
				r.valueCount = r.getLastValue() - last;
				r.valueStart = last + 1;
				updateIndex(i1);
				return;
			} else {
				r.valueCount = start - r.valueStart;
				updateIndex(i1 + 1);
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
	public static IndexedRangeList intersectWith(IndexedRangeList x, IndexedRangeList y) {
		IndexedRangeList ret = new IndexedRangeList();
		int index1 = 0, index2 = 0;
		int state = 0; // 0 for none; 1 for single x; 2 for single y; 3 for x and y
		int firstOfInteract = 0;
		while (index1 / 2 < x.ranges.size() && index2 / 2 < y.ranges.size()) {
			Range pair1 = x.ranges.get(index1 / 2);
			int value1 = (index1 % 2 == 0 ? pair1.getFirstValue() : pair1.getLastValue());
			Range pair2 = y.ranges.get(index2 / 2);
			int value2 = (index2 % 2 == 0 ? pair2.getFirstValue() : pair2.getLastValue());

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
					ret.ranges.add(new Range(0, firstOfInteract, value1 - firstOfInteract + 1));
				} else {
					state = 1;
					++index2;
					ret.ranges.add(new Range(0, firstOfInteract, value2 - firstOfInteract + 1));
				}
				break;

			default:
				throw new IllegalStateException("Illegal value of state");
			}
		}
		ret.updateIndex(0);
		return ret;
	}

	/**
	 * 两个容器做并集
	 */
	public static IndexedRangeList mergeWith(IndexedRangeList x, IndexedRangeList y) {
		IndexedRangeList ret = new IndexedRangeList();
		// 状态机基本上和intersectWith()方法中一样
		int index1 = 0, index2 = 0;
		int state = 0; // 0 for none; 1 for single x; 2 for single y; 3 for x and y
		int firstOfMerge = 0;
		while (index1 / 2 < x.ranges.size() || index2 / 2 < y.ranges.size()) {
			int value1, value2;
			if (index1 / 2 < x.ranges.size()) {
				Range pair1 = x.ranges.get(index1 / 2);
				value1 = (index1 % 2 == 0 ? pair1.getFirstValue() : pair1.getLastValue());
			} else {
				value1 = Integer.MAX_VALUE;
			}

			if (index2 / 2 < y.ranges.size()) {
				Range pair2 = y.ranges.get(index2 / 2);
				value2 = (index2 % 2 == 0 ? pair2.getFirstValue() : pair2.getLastValue());
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
					if (ret.ranges.size() > 0 && ret.ranges.get(ret.ranges.size() - 1).getLastValue() + 1 == firstOfMerge)
						ret.ranges.set(ret.ranges.size() - 1, new Range(0, ret.ranges.get(ret.ranges.size() - 1).getFirstValue(), value1));
					else
						ret.ranges.add(new Range(0, firstOfMerge, value1));
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
					if (ret.ranges.size() > 0 && ret.ranges.get(ret.ranges.size() - 1).getLastValue() + 1 == firstOfMerge)
						ret.ranges.set(ret.ranges.size() - 1, new Range(0, ret.ranges.get(ret.ranges.size() - 1).getFirstValue(), value2));
					else
						ret.ranges.add(new Range(0, firstOfMerge, value2));
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
		ret.updateIndex(0);
		return ret;
	}

	/**
	 * 两个容器做补集
	 * {x} - {y}
	 */
	public static IndexedRangeList remainder(IndexedRangeList x, IndexedRangeList y) {
		IndexedRangeList ret = new IndexedRangeList();
		int index1 = 0, index2 = 0;
		int state = 0; // 0 for none; 1 for single x; 2 for single y; 3 for x and y
		int firstOfRemainder = 0;
		while (index1 / 2 < x.ranges.size()) {
			int value1, value2;
			if (index1 / 2 < x.ranges.size()) {
				Range pair1 = x.ranges.get(index1 / 2);
				value1 = (index1 % 2 == 0 ? pair1.getFirstValue() : pair1.getLastValue());
			} else {
				value1 = Integer.MAX_VALUE;
			}

			if (index2 / 2 < y.ranges.size()) {
				Range pair2 = y.ranges.get(index2 / 2);
				value2 = (index2 % 2 == 0 ? pair2.getFirstValue() : pair2.getLastValue());
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
					ret.ranges.add(new Range(0, firstOfRemainder, value1 - firstOfRemainder + 1));
				} else {
					state = 3;
					++index2;
					ret.ranges.add(new Range(0, firstOfRemainder, value2 - 1 - firstOfRemainder + 1));
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
		ret.updateIndex(0);
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
				if (nextPairIndex < IndexedRangeList.this.ranges.size()) {
					Range pair = IndexedRangeList.this.ranges.get(nextPairIndex);
					nextValue = pair.getFirstValue();
					secondOfPair = pair.getLastValue();
				}
			}

			@Override
			public boolean hasNext() {
				return nextPairIndex < IndexedRangeList.this.ranges.size();
			}

			@Override
			public Integer next() {
				if (!hasNext())
					throw new IllegalStateException("There is NO next step!");

				currentValue = nextValue;

				++nextValue;
				if (nextValue > secondOfPair) {
					++nextPairIndex;
					if (nextPairIndex < IndexedRangeList.this.ranges.size()) {
						Range pair = IndexedRangeList.this.ranges.get(nextPairIndex);
						nextValue = pair.getFirstValue();
						secondOfPair = pair.getLastValue();
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
			Range middlePair = ranges.get(middle);
			int first = middlePair.getFirstValue();
			int second = middlePair.getLastValue();
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
				if (nextPairIndex < IndexedRangeList.this.ranges.size()) {
					Range pair = IndexedRangeList.this.ranges.get(nextPairIndex);
					int firstOfpair = pair.getFirstValue();
					secondOfPair = pair.getLastValue();
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
					if (nextPairIndex < IndexedRangeList.this.ranges.size()) {
						Range pair = IndexedRangeList.this.ranges.get(nextPairIndex);
						int firstOfpair = pair.getFirstValue();
						secondOfPair = pair.getLastValue();
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
			Range pair = ranges.get(middle);
			int first = pair.getFirstValue();
			int second = pair.getLastValue();
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
					Range pair = IndexedRangeList.this.ranges.get(rightBoundPairIndex - 1);
					int second = pair.getLastValue();
					nextValue = Math.max(second + 1, firstValue);
				} else {
					nextValue = firstValue;
				}

				rightBound = lastValue + 1;
				nextStart = lastValue + 1;
				if (rightBoundPairIndex < IndexedRangeList.this.ranges.size()) {
					Range pair = IndexedRangeList.this.ranges.get(rightBoundPairIndex);
					rightBound = pair.getFirstValue();
					nextStart = pair.getLastValue();
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
					if (rightBoundPairIndex < IndexedRangeList.this.ranges.size()) {
						Range pair = IndexedRangeList.this.ranges.get(rightBoundPairIndex);
						rightBound = pair.getFirstValue();
						nextStart = pair.getLastValue();
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
	 * 检测容器状态是否是一致的，不一致则说明有错误
	 */
	public boolean isValid() {
		for (int i = 0, size = ranges.size(); i < size; ++i) {
			Range r = ranges.get(i);
			if (r.valueCount <= 0)
				return false;

			if (i == 0) {
				if (r.index != 0)
					return false;
			} else {
				Range rr = ranges.get(i - 1);
				if (r.index != rr.index + rr.valueCount)
					return false;
				if (r.valueStart <= rr.valueStart + rr.valueCount)
					return false;
			}
		}
		return true;
	}

	@Override
	public IndexedRangeList clone() {
		IndexedRangeList l = new IndexedRangeList();
		for (int i = 0, s = ranges.size(); i < s; ++i)
			l.ranges.add(ranges.get(i).clone());
		return l;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0, size = ranges.size(); i < size; ++i) {
			Range pair = ranges.get(i);
			int first = pair.getFirstValue();
			int second = pair.getLastValue();
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

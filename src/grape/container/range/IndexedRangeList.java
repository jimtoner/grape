package grape.container.range;

import grape.container.deqlist.ArrayDequeList;
import grape.container.deqlist.DequeList;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 存储一系列有序的范围，例如
 * (1,3) (6,6) (8,9)
 *
 * @author jingqi
 *
 */
public class IndexedRangeList extends AbstractRangeContainer implements IndexedRangeContainer, Cloneable, Serializable {

	private static final long serialVersionUID = -3699871549717518344L;

	private static class IndexedRange extends Range {
		private int firstIndex; // 第一个 value 的索引

		public IndexedRange(int index, int first, int last) {
			super(first, last);
			this.firstIndex = index;
		}

		public int getFirstIndex() {
			return firstIndex;
		}

		public void setFirstIndex(int index) {
			this.firstIndex = index;
		}

		public int getLastIndex() {
			return firstIndex + (getLastValue() - getFirstValue());
		}

		@Override
		public IndexedRange clone() {
			return new IndexedRange(firstIndex, getFirstValue(), getLastValue());
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof IndexedRange))
				return false;
			IndexedRange x = (IndexedRange) o;
			return x.firstIndex == firstIndex && super.equals(o);
		}

		@Override
		public int hashCode() {
			return super.hashCode() * 31 + firstIndex;
		}
	}

	private DequeList<IndexedRange> ranges = new ArrayDequeList<IndexedRange>();

	@Override
	public int getFirstValue() {
		if (ranges.size() == 0)
			throw new IllegalStateException("empty");
		return ranges.get(0).getFirstValue();
	}

	@Override
	public int getLastValue() {
		if (ranges.size() == 0)
			throw new IllegalStateException("empty");
		IndexedRange r = ranges.get(ranges.size() - 1);
		return r.getLastValue();
	}

	@Override
	public int size() {
		if (ranges.size() == 0)
			return 0;
		return ranges.get(ranges.size() - 1).getLastIndex() + 1;
	}

	@Override
	public boolean isEmpty() {
		return ranges.isEmpty();
	}

	@Override
	public int getValue(int index) {
		if (index < 0)
			throw new IndexOutOfBoundsException();

		// 二分查找
		int left = -1, right = ranges.size();
		while (left + 1 < right) {
			int middle = (left + right) / 2;
			IndexedRange r = ranges.get(middle);
			if (r.getLastIndex() < index)
				left = middle;
			else if (r.getFirstIndex() > index)
				right = middle;
			else
				return r.getFirstValue() + (index - r.getFirstIndex());
		}
		throw new IndexOutOfBoundsException();
	}

	/**
	 * 查找指定值是否在该容器中
	 */
	@Override
	public boolean contains(int value) {
		return binarySearch(value) >= 0;
	}

	@Override
	public int indexOfValue(int value) {
		int location = binarySearch(value);
		if (location < 0)
			return -1;
		IndexedRange r = ranges.get(location);
		return r.getFirstIndex() + (value - r.getFirstValue());
	}

	/**
	 * 更新指定范围的 index 缓存
	 */
	private void updateIndex(int locationStart, int count) {
		if (locationStart < 0 || count < 0 || locationStart + count > ranges.size())
			throw new IllegalArgumentException();

		for (int i = 0; i < count; ++i) {
			IndexedRange r = ranges.get(locationStart + i);
			if (locationStart + i == 0) {
				r.setFirstIndex(0);
			} else {
				IndexedRange before = ranges.get(locationStart + i - 1);
				r.setFirstIndex(before.getLastIndex() + 1);
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
	private int binarySearch(int value) {
		int left = -1, right = ranges.size();
		while (left + 1 < right) {
			int middle = (left + right) / 2;
			IndexedRange r = ranges.get(middle);
			if (r.getLastValue() < value)
				left = middle;
			else if (r.getFirstValue() > value)
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
			ranges.add(new IndexedRange(0, firstValue, lastValue));
			return;
		}

		// 对头部插入优化
		IndexedRange r = ranges.get(0);
		if (lastValue + 1 < r.getFirstValue()) {
			ranges.add(0, new IndexedRange(0, firstValue, lastValue));
			updateIndex(1);
			return;
		} else if (lastValue <= r.getLastValue()) {
			if (firstValue >= r.getFirstValue())
				return;
			r.setFirstValue(firstValue);
			updateIndex(1);
			return;
		}

		// 对尾部插入优化
		r = ranges.get(ranges.size() - 1);
		if (firstValue - 1 > r.getLastValue()) {
			ranges.add(new IndexedRange(r.getLastIndex() + 1, firstValue, lastValue));
			return;
		} else if (firstValue >= r.getFirstValue()) {
			if (lastValue <= r.getLastValue())
				return;
			r.setLastValue(lastValue);
			return;
		}

		// 二分查找法确定可以合并的 range 范围
		int i1 = binarySearch(firstValue - 1), i2 = binarySearch(lastValue + 1);
		if (i1 < 0)
			i1 = -i1 - 1;
		if (i2 < 0)
			i2 = -i2 - 2;

		if (i1 <= i2) {
			int min_first = Math.min(firstValue, ranges.get(i1).getFirstValue());
			int max_last = Math.max(lastValue, ranges.get(i2).getLastValue());
			ranges.subList(i1, i2 + 1).clear();
			ranges.add(i1, new IndexedRange(0, min_first, max_last));
		} else {
			ranges.add(i1, new IndexedRange(0, firstValue, lastValue));
		}
		updateIndex(i1);
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
			IndexedRange r = ranges.get(i1);
			if (r.getFirstValue() < firstValue) {
				r.setLastValue(firstValue - 1);
				++i1;
			}
			r = ranges.get(i2);
			if (r.getLastValue() > lastValue) {
				r.setFirstValue(lastValue + 1);
				--i2;
			}
			if (i1 <= i2)
				ranges.subList(i1, i2 + 1).clear();
			updateIndex(i1);
			return;
		} else if (i1 == i2) {
			IndexedRange r = ranges.get(i1);
			if (firstValue <= r.getFirstValue() && lastValue >= r.getLastValue()) {
				ranges.remove(i1);
				updateIndex(i1);
				return;
			} else if (firstValue > r.getFirstValue() && lastValue < r.getLastValue()) {
				ranges.add(i1 + 1, new IndexedRange(0, lastValue + 1, r.getLastValue()));
				r.setLastValue(firstValue - 1);
				updateIndex(i1);
				return;
			} else if (firstValue <= r.getFirstValue()) {
				r.setFirstValue(lastValue + 1);
				updateIndex(i1);
				return;
			} else {
				r.setLastValue(firstValue - 1);
				updateIndex(i1 + 1);
				return;
			}
		}
	}

	@Override
	public IndexedRangeList intersectWith(RangeContainer x) {
		IndexedRangeList ret = new IndexedRangeList();
		intersectWith(this, x, ret);
		return ret;
	}

	@Override
	public IndexedRangeList mergeWith(RangeContainer x) {
		IndexedRangeList ret = new IndexedRangeList();
		mergeWith(this, x, ret);
		return ret;
	}

	@Override
	public IndexedRangeList remainder(RangeContainer x) {
		IndexedRangeList ret = new IndexedRangeList();
		remainder(this, x, ret);
		return ret;
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
				return ranges.get(location++);
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
				return ranges.get(location++);
			}

			@Override
			public void remove() {
				ranges.remove(location--);
				--lastLocation;
			}
		};
	}

	/**
	 * 检测容器状态是否是一致的，不一致则说明有错误
	 */
	public boolean isValid() {
		for (int i = 0, size = ranges.size(); i < size; ++i) {
			IndexedRange r = ranges.get(i);
			if (r.getFirstValue() > r.getLastValue())
				return false;

			if (i == 0) {
				if (r.getFirstIndex() != 0)
					return false;
			} else {
				IndexedRange rr = ranges.get(i - 1);
				if (r.getFirstIndex() != rr.getFirstIndex() + (rr.getLastValue() - rr.getFirstValue() + 1))
					return false;
				if (r.getFirstValue() <= rr.getLastValue() + 1)
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
}

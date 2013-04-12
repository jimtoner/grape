package grape.container.range;

/**
 * 带索引的整数范围
 */
public class Range {
	private int firstValue, lastValue; // 第一个和最后一个元素，闭区间

	public Range(int first, int last) {
		this.firstValue = first;
		this.lastValue = last;
	}

	public int getFirstValue() {
		return firstValue;
	}

	public void setFirstValue(int first) {
		firstValue = first;
	}

	public int getLastValue() {
		return lastValue;
	}

	public void setLastValue(int last) {
		lastValue = last;
	}

	public int length() {
		return lastValue - firstValue + 1;
	}

	/**
	 * 取交集
	 *
	 * @return 如果没有交集，返回 null
	 */
	public Range intersectWith(Range x) {
		if (firstValue > x.lastValue || lastValue < x.firstValue)
			return null;
		return new Range(Math.max(firstValue, x.firstValue), Math.min(lastValue, x.lastValue));
	}

	/**
	 * 取交集
	 *
	 * @return 如果区域不相接，返回null
	 */
	public Range mergeWith(Range x) {
		if (firstValue - 1 > x.lastValue || lastValue + 1 < x.firstValue)
			return null;
		return new Range(Math.min(firstValue, x.firstValue), Math.max(lastValue, x.lastValue));
	}

	@Override
	public Range clone() {
		return new Range(firstValue, lastValue);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Range))
			return false;
		Range x = (Range) o;
		return x.firstValue == firstValue && x.lastValue == lastValue;
	}

	@Override
	public int hashCode() {
		int hash = firstValue;
		hash = hash * 31 + lastValue;
		return hash;
	}

	@Override
	public String toString() {
		if (firstValue == lastValue)
			return Integer.toString(firstValue);
		return "(" + Integer.toString(firstValue) + "," +
			Integer.toString(lastValue) + ")";
	}
}

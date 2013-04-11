package grape.container.rangelist;

/**
 * 带索引的整数范围
 */
class Range {
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

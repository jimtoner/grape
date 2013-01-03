package grape.container;

/**
 * 浮点数栈
 */
public class DoubleStack {

	private static final int DEFAULT_INITICAL_CAPACITY = 16;

	private int size;
	private double[] buffer;

	public DoubleStack() {
		this(DEFAULT_INITICAL_CAPACITY);
	}

	public DoubleStack(int initialCapacity) {
		if (initialCapacity <= 0)
			throw new IllegalArgumentException("Illegal capacity:"
					+ initialCapacity);
		buffer = new double[initialCapacity];
		size = 0;
	}

	private void ensureCap(int new_size) {
		if (new_size <= buffer.length)
			return;

		int new_cap = buffer.length * 3 / 2;
		if (new_cap < new_size)
			new_cap = new_size;

		double[] new_buf = new double[new_cap];
		System.arraycopy(buffer, 0, new_buf, 0, size);
		buffer = new_buf;
	}

	public void push(double v) {
		ensureCap(size + 1);
		buffer[size++] = v;
	}

	public double pop() {
		if (size <= 0)
			throw new IndexOutOfBoundsException("Empty stack");

		return buffer[--size];
	}

	/**
	 * 相当于 get(-1) 或者 get(size() - 1)
	 */
	public double top() {
		if (size <= 0)
			throw new IndexOutOfBoundsException("Empty stack");
		return buffer[size - 1];
	}

	/**
	 * 正索引[0,size)，栈底为0，栈顶为size-1 负索引[-size, -1]，栈底为-size，栈顶为-1
	 */
	public double get(int index) {
		if (index < -size || index >= size)
			throw new IndexOutOfBoundsException("Illegal index " + index
					+ " with size " + size);

		if (index >= 0)
			return buffer[index];
		return buffer[index + size];
	}

	public void clear() {
		size = 0;
	}

	public int size() {
		return size;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof DoubleStack))
			return false;

		DoubleStack ds = (DoubleStack) o;
		if (ds.size != size)
			return false;
		for (int i = 0; i < size; ++i)
			if (ds.buffer[i] != buffer[i])
				return false;
		return true;
	}

	@Override
	public int hashCode() {
		int h = 0;
		for (int i = 0; i < size; ++i) {
			long v = Double.doubleToRawLongBits(buffer[i]);
			h = (h >>> 1) ^ (int) (v ^ (v >>> 32));
		}
		return h;
	}

	@Override
	public DoubleStack clone() {
		DoubleStack ret = new DoubleStack(size);
		System.arraycopy(buffer, 0, ret.buffer, 0, size);
		ret.size = size;
		return ret;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for (int i = 0; i < size; ++i) {
			if (i != 0)
				sb.append(", ");
			sb.append(Double.toString(buffer[i]));
		}
		sb.append(']');
		return sb.toString();
	}
}

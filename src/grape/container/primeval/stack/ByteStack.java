package grape.container.primeval.stack;

public class ByteStack {

	private static final int DEFAULT_INITICAL_CAPACITY = 16;

	private int size;
	private byte[] buffer;

	public ByteStack() {
		this(DEFAULT_INITICAL_CAPACITY);
	}

	public ByteStack(int initialCapacity) {
		if (initialCapacity <= 0)
			throw new IllegalArgumentException("Illegal capacity:"
					+ initialCapacity);
		buffer = new byte[initialCapacity];
		size = 0;
	}

	private void ensureCap(int new_size) {
		if (new_size <= buffer.length)
			return;

		int new_cap = buffer.length * 3 / 2;
		if (new_cap < new_size)
			new_cap = new_size;

		byte[] new_buf = new byte[new_cap];
		System.arraycopy(buffer, 0, new_buf, 0, size);
		buffer = new_buf;
	}

	public void push(byte v) {
		ensureCap(size + 1);
		buffer[size++] = v;
	}

	public byte pop() {
		if (size <= 0)
			throw new IndexOutOfBoundsException("Empty stack");

		return buffer[--size];
	}

	/**
	 * 相当于 get(-1) 或者 get(size() - 1)
	 */
	public byte top() {
		if (size <= 0)
			throw new IndexOutOfBoundsException("Empty stack");
		return buffer[size - 1];
	}

	/**
	 * 正索引[0,size)，栈底为0，栈顶为size-1 负索引[-size, -1]，栈底为-size，栈顶为-1
	 */
	public byte get(int index) {
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
		if (!(o instanceof ByteStack))
			return false;

		ByteStack ds = (ByteStack) o;
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
                h = h * 31 + buffer[i];
		}
		return h;
	}

	@Override
	public ByteStack clone() {
		ByteStack ret = new ByteStack(size);
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
			sb.append(Byte.toString(buffer[i]));
		}
		sb.append(']');
		return sb.toString();
	}
}

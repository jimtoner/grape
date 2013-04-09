package grape.container.primeval.list;

import java.io.Serializable;
import java.util.Collection;
import java.util.RandomAccess;

public class LongArrayList implements RandomAccess, Cloneable, Serializable {

	private static final long serialVersionUID = 1L;

	private static final int DEFAULT_INITICAL_CAPACITY = 16;

	private int size;
	private long[] buffer;

	public LongArrayList() {
		this(DEFAULT_INITICAL_CAPACITY);
	}

	public LongArrayList(int initialCapacity) {
		if (initialCapacity <= 0)
			throw new IllegalArgumentException("Illegal capacity:"
					+ initialCapacity);
		buffer = new long[initialCapacity];
		size = 0;
	}

	public LongArrayList(Collection<Long> c) {
		buffer = new long[c.size()];
		size = 0;
		for (Long s : c)
			buffer[size++] = s;
	}

	public LongArrayList(LongArrayList c) {
		buffer = new long[c.size];
		System.arraycopy(c.buffer, 0, buffer, 0, c.size);
		size = c.size;
	}

	public LongArrayList(long[] values) {
		this(values, 0, values.length);
	}

    public LongArrayList(long[] values, int value_begin, int len) {
		if (value_begin < 0 || len < 0 || value_begin + len > values.length)
			throw new IllegalArgumentException();

		buffer = new long[len];
		System.arraycopy(values, value_begin, buffer, 0, len);
		size = len;
	}

	private void ensureCap(int new_size) {
		if (new_size <= buffer.length)
			return;

		int new_cap = buffer.length * 3 / 2;
		if (new_cap < new_size)
			new_cap = new_size;

		long[] new_buf = new long[new_cap];
		System.arraycopy(buffer, 0, new_buf, 0, size);
		buffer = new_buf;
	}

	public void add(long v) {
		ensureCap(size + 1);
		buffer[size++] = v;
	}

	public void addAll(Collection<Long> c) {
		for (Long s : c)
			add(s);
	}

	public void addAll(LongArrayList c) {
		ensureCap(size + c.size);
		System.arraycopy(c.buffer, 0, buffer, size, c.size);
		size += c.size;
	}

	public void addAll(long[] values) {
		addAll(values, 0, values.length);
	}

	public void addAll(long[] values, int value_begin, int len) {
		if (value_begin < 0 || len < 0 || value_begin + len > values.length)
			throw new IllegalArgumentException();

		ensureCap(size + len);
		System.arraycopy(values, value_begin, buffer, size, len);
		size += len;
	}

	public void add(int index, long value) {
		if (index < 0 || index > size)
			throw new IndexOutOfBoundsException("Index:" + index + " size:"
					+ size);

		ensureCap(size + 1);
		System.arraycopy(buffer, index, buffer, index + 1, size - index);
		buffer[index] = value;
		++size;
	}

	public void add(int index, Collection<Long> c) {
		if (index < 0 || index > size)
			throw new IndexOutOfBoundsException("Index:" + index + " size:"
					+ size);

		ensureCap(size + c.size());
		System.arraycopy(buffer, index, buffer, index + c.size(), size - index);
		for (Long s : c) {
			buffer[index++] = s;
		}
		size += c.size();
	}

	public void add(int index, LongArrayList c) {
		if (index < 0 || index > size)
			throw new IndexOutOfBoundsException("Index:" + index + " size:"
					+ size);

		ensureCap(size + c.size());
		System.arraycopy(buffer, index, buffer, index + c.size(), size - index);
		System.arraycopy(c.buffer, 0, buffer, index, c.size);
		size += c.size();
	}

	public void add(int index, long[] values) {
		add(index, values, 0, values.length);
	}

	public void add(int index, long[] values, int value_begin, int len) {
		if (index < 0 || index > size || value_begin < 0 || len < 0
				|| value_begin + len > values.length)
			throw new IllegalArgumentException();

		ensureCap(size + len);
		System.arraycopy(buffer, index, buffer, index + len, size - index);
		System.arraycopy(values, value_begin, buffer, index, len);
		size += len;
	}

	public long remove(int index) {
		if (index < 0 || index >= size)
			throw new IndexOutOfBoundsException();

		long ret = buffer[index];
		System.arraycopy(buffer, index + 1, buffer, index, size - index - 1);
		--size;
		return ret;
	}

	public void removeRange(int from, int to) {
        if (from < 0 || from > to || to > size)
			throw new IllegalArgumentException();

		System.arraycopy(buffer, to, buffer, from, size - to);
		size -= to - from;
	}

	public boolean removeAll(long value) {
		int new_size = 0;
		for (int i = 0; i < size; ++i) {
			if (buffer[i] != value) {
				buffer[new_size++] = buffer[i];
			}
		}
		boolean changed = (new_size != size);
		size = new_size;
		return changed;
	}

	public boolean removeAll(Collection<Long> c) {
		int old_size = size;
		for (Long s : c)
			removeAll(s);
		return old_size != size;
	}

	public boolean removeAll(LongArrayList c) {
		int old_size = size;
		for (int i = 0; i < c.size; ++i)
			removeAll(c.buffer[i]);
		return old_size != size;
	}

	public boolean removeAll(long[] values) {
		return removeAll(values, 0, values.length);
	}

	public boolean removeAll(long[] values, int value_begin, int len) {
		if (value_begin < 0 || len < 0 || value_begin + len > values.length)
			throw new IllegalArgumentException();

		int old_size = size;
		for (int i = 0; i < len; ++i)
			removeAll(values[value_begin + i]);
		return old_size != size;
	}

	public boolean retainAll(Collection<Long> c) {
		int new_size = 0;
		for (int i = 0; i < size; ++i) {
			if (c.contains(buffer[i])) {
				buffer[new_size++] = buffer[i];
			}
		}
		boolean changed = (new_size != size);
		size = new_size;
		return changed;
	}

	public boolean retainAll(LongArrayList c) {
		int new_size = 0;
		for (int i = 0; i < size; ++i) {
			if (c.contains(buffer[i])) {
				buffer[new_size++] = buffer[i];
			}
		}
		boolean changed = (new_size != size);
		size = new_size;
		return changed;
	}

	public void clear() {
		size = 0;
	}

	public long get(int index) {
		if (index < 0 || index >= size)
			throw new IndexOutOfBoundsException();

		return buffer[index];
	}

	public long set(int index, long value) {
		if (index < 0 || index >= size)
			throw new IndexOutOfBoundsException();

		long ret = buffer[index];
		buffer[index] = value;
		return ret;
	}

	/**
	 * 没有找到则返�?-1
	 */
	public int indexOf(long v) {
		return indexOf(v, 0);
	}

	public int indexOf(long v, int search_begin) {
		if (search_begin < 0 || search_begin > size)
			throw new IndexOutOfBoundsException();
		for (int i = search_begin; i < size; ++i)
			if (buffer[i] == v)
				return i;
		return -1;
	}

	public int lastIndexOf(long v) {
		return lastIndexOf(v, size - 1);
	}

	public int lastIndexOf(long v, int search_begin) {
		if (search_begin < -1 || search_begin >= size)
			throw new IndexOutOfBoundsException();
		for (int i = search_begin; i >= 0; --i)
			if (buffer[i] == v)
				return i;
		return -1;
	}

	public boolean contains(long v) {
		for (int i = 0; i < size; ++i)
			if (buffer[i] == v)
				return true;
		return false;
	}

	public boolean contailsAll(Collection<Long> c) {
		for (Long s : c)
			if (!contains(s))
				return false;
		return true;
	}

	public boolean containsAll(LongArrayList c) {
		for (int i = 0; i < c.size; ++i)
			if (!contains(c.buffer[i]))
				return false;
		return true;
	}

	public boolean conatainsAll(long[] values) {
		return containsAll(values, 0, values.length);
	}

	public boolean containsAll(long[] values, int value_begin, int len) {
		if (value_begin < 0 || len < 0 || value_begin + len > values.length)
			throw new IllegalArgumentException();

		for (int i = 0; i < len; ++i)
			if (!contains(values[value_begin + i]))
				return false;
		return true;
	}

	public LongArrayList subList(int begin, int len) {
		if (begin < 0 || len < 0 || begin + len > size)
			throw new IllegalArgumentException();

		return new LongArrayList(buffer, begin, len);
	}

	public int size() {
		return size;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public long[] toArray() {
		long[] ret = new long[size];
		System.arraycopy(buffer, 0, ret, 0, size);
		return ret;
	}

	public void toArray(long[] arr, int begin) {
		if (begin < 0 || begin + size > arr.length)
			throw new IllegalArgumentException();
		System.arraycopy(buffer, 0, arr, begin, size);
	}

	@Override
	public LongArrayList clone() {
		return new LongArrayList(buffer, 0, size);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof LongArrayList))
			return false;

		LongArrayList x = (LongArrayList) o;
		if (x.size != size)
			return false;
		for (int i = 0; i < size; ++i)
			if (x.buffer[i] != buffer[i])
				return false;
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 0;

		for (int i = 0; i < size; ++i) {
                hash = (31 * hash) + (int)(buffer[i] ^ (buffer[i] >>> 32));
        }
		return hash;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for (int i = 0; i < size; ++i) {
			if (i != 0)
				sb.append(", ");
			sb.append(Long.toString(buffer[i]));
		}
		sb.append(']');
		return sb.toString();
	}
}
package grape.container.primeval.list;

import java.util.Collection;

public class CharList {

	private static final int DEFAULT_INITICAL_CAPACITY = 16;

	private int size;
	private char[] buffer;

	public CharList() {
		this(DEFAULT_INITICAL_CAPACITY);
	}

	public CharList(int initialCapacity) {
		if (initialCapacity <= 0)
			throw new IllegalArgumentException("Illegal capacity:"
					+ initialCapacity);
		buffer = new char[initialCapacity];
		size = 0;
	}

	public CharList(Collection<Character> c) {
		buffer = new char[c.size()];
		size = 0;
		for (Character s : c)
			buffer[size++] = s;
	}

	public CharList(CharList c) {
		buffer = new char[c.size];
		System.arraycopy(c.buffer, 0, buffer, 0, c.size);
		size = c.size;
	}

	public CharList(char[] values) {
		this(values, 0, values.length);
	}

    public CharList(char[] values, int value_begin, int len) {
		if (value_begin < 0 || len < 0 || value_begin + len > values.length)
			throw new IllegalArgumentException();

		buffer = new char[len];
		System.arraycopy(values, value_begin, buffer, 0, len);
		size = len;
	}

	private void ensureCap(int new_size) {
		if (new_size <= buffer.length)
			return;

		int new_cap = buffer.length * 3 / 2;
		if (new_cap < new_size)
			new_cap = new_size;

		char[] new_buf = new char[new_cap];
		System.arraycopy(buffer, 0, new_buf, 0, size);
		buffer = new_buf;
	}

	public void add(char v) {
		ensureCap(size + 1);
		buffer[size++] = v;
	}

	public void addAll(Collection<Character> c) {
		for (Character s : c) {
			add(s);
		}
	}

	public void addAll(CharList c) {
		ensureCap(size + c.size);
		System.arraycopy(c.buffer, 0, buffer, size, c.size);
		size += c.size;
	}

	public void addAll(char[] values) {
		addAll(values, 0, values.length);
	}

	public void addAll(char[] values, int value_begin, int len) {
		if (value_begin < 0 || len < 0 || value_begin + len > values.length)
			throw new IllegalArgumentException();

		ensureCap(size + len);
		System.arraycopy(values, value_begin, buffer, size, len);
		size += len;
	}

	public void insert(int index, char value) {
		if (index < 0 || index > size)
			throw new IndexOutOfBoundsException("Index:" + index + " size:"
					+ size);

		ensureCap(size + 1);
		System.arraycopy(buffer, index, buffer, index + 1, size - index);
		buffer[index] = value;
		++size;
	}

	public void insert(int index, Collection<Character> c) {
		if (index < 0 || index > size)
			throw new IndexOutOfBoundsException("Index:" + index + " size:"
					+ size);

		ensureCap(size + c.size());
		System.arraycopy(buffer, index, buffer, index + c.size(), size - index);
		for (Character s : c) {
			buffer[index++] = s;
		}
		size += c.size();
	}

	public void insert(int index, CharList c) {
		if (index < 0 || index > size)
			throw new IndexOutOfBoundsException("Index:" + index + " size:"
					+ size);

		ensureCap(size + c.size());
		System.arraycopy(buffer, index, buffer, index + c.size(), size - index);
		System.arraycopy(c.buffer, 0, buffer, index, c.size);
		size += c.size();
	}

	public void insert(int index, byte[] values) {
		insert(index, values, 0, values.length);
	}

	public void insert(int index, byte[] values, int value_begin, int len) {
		if (index < 0 || index > size || value_begin < 0 || len < 0
				|| value_begin + len > values.length)
			throw new IllegalArgumentException();

		ensureCap(size + len);
		System.arraycopy(buffer, index, buffer, index + len, size - index);
		System.arraycopy(values, value_begin, buffer, index, len);
		size += len;
	}

	public char remove(int index) {
		if (index < 0 || index >= size)
			throw new IndexOutOfBoundsException();

		char ret = buffer[index];
		System.arraycopy(buffer, index + 1, buffer, index, size - index - 1);
		--size;
		return ret;
	}

	public void remove(int index, int len) {
		if (index < 0 || len < 0 || index + len > size)
			throw new IllegalArgumentException();

		System.arraycopy(buffer, index + len, buffer, index, size - index - len);
		size -= len;
	}

	public boolean removeAll(char value) {
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

	public boolean removeAll(Collection<Character> c) {
		int old_size = size;
		for (Character s : c)
			removeAll(s);
		return old_size != size;
	}

	public boolean removeAll(CharList c) {
		int old_size = size;
		for (int i = 0; i < c.size; ++i)
			removeAll(c.buffer[i]);
		return old_size != size;
	}

	public boolean removeAll(char[] values) {
		return removeAll(values, 0, values.length);
	}

	public boolean removeAll(char[] values, int value_begin, int len) {
		if (value_begin < 0 || len < 0 || value_begin + len > values.length)
			throw new IllegalArgumentException();

		int old_size = size;
		for (int i = 0; i < len; ++i)
			removeAll(values[value_begin + i]);
		return old_size != size;
	}

	public boolean retainAll(Collection<Character> c) {
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

	public boolean retainAll(CharList c) {
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

	public char get(int index) {
		if (index < 0 || index >= size)
			throw new IndexOutOfBoundsException();

		return buffer[index];
	}

	public char set(int index, char value) {
		if (index < 0 || index >= size)
			throw new IndexOutOfBoundsException();

		char ret = buffer[index];
		buffer[index] = value;
		return ret;
	}

	/**
	 * 没有找到则返�?-1
	 */
	public int indexOf(char v) {
		return indexOf(v, 0);
	}

	public int indexOf(char v, int search_begin) {
		if (search_begin < 0 || search_begin > size)
			throw new IndexOutOfBoundsException();
		for (int i = search_begin; i < size; ++i)
			if (buffer[i] == v)
				return i;
		return -1;
	}

	public int lastIndexOf(char v) {
		return lastIndexOf(v, size - 1);
	}

	public int lastIndexOf(char v, int search_begin) {
		if (search_begin < -1 || search_begin >= size)
			throw new IndexOutOfBoundsException();
		for (int i = search_begin; i >= 0; --i)
			if (buffer[i] == v)
				return i;
		return -1;
	}

	public boolean contains(char v) {
		for (int i = 0; i < size; ++i)
			if (buffer[i] == v)
				return true;
		return false;
	}

	public boolean contailsAll(Collection<Character> c) {
		for (Character s : c)
			if (!contains(s))
				return false;
		return true;
	}

	public boolean containsAll(CharList c) {
		for (int i = 0; i < c.size; ++i)
			if (!contains(c.buffer[i]))
				return false;
		return true;
	}

	public boolean conatainsAll(char[] values) {
		return containsAll(values, 0, values.length);
	}

	public boolean containsAll(char[] values, int value_begin, int len) {
		if (value_begin < 0 || len < 0 || value_begin + len > values.length)
			throw new IllegalArgumentException();

		for (int i = 0; i < len; ++i)
			if (!contains(values[value_begin + i]))
				return false;
		return true;
	}

	public CharList subList(int begin, int len) {
		if (begin < 0 || len < 0 || begin + len > size)
			throw new IllegalArgumentException();

		return new CharList(buffer, begin, len);
	}

	public int size() {
		return size;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public char[] toArray() {
		char[] ret = new char[size];
		System.arraycopy(buffer, 0, ret, 0, size);
		return ret;
	}

	public void toArray(char[] arr, int begin) {
		if (begin < 0 || begin + size > arr.length)
			throw new IllegalArgumentException();
		System.arraycopy(buffer, 0, arr, begin, size);
	}

	@Override
	public CharList clone() {
		return new CharList(buffer, 0, size);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof CharList))
			return false;

		CharList x = (CharList) o;
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
                hash = (31 * hash) + buffer[i];
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
			sb.append(Character.toString(buffer[i]));
		}
		sb.append(']');
		return sb.toString();
	}
}

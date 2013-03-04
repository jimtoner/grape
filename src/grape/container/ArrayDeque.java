package grape.container;

import java.io.Serializable;
import java.util.*;

import com.sun.jmx.remote.internal.ArrayQueue;

/**
 * 实现逻辑类似于 {@link ArrayQueue}，但存储空间动态分配
 */
public class ArrayDeque<E> extends AbstractList<E>
		implements List<E>, RandomAccess, Cloneable, Serializable {

	private static final long serialVersionUID = -5299717963451646695L;

	// 存储方式为环形存储，超出末端要保留一个null哨兵
	private Object[] buffer;
	private int begin = 0, end = 0;

	public ArrayDeque() {
		this(10);
	}

	public ArrayDeque(int initialCapacity) {
		super();
		if (initialCapacity < 0)
			throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
		buffer = new Object[initialCapacity + 1];
	}

	public ArrayDeque(Collection<? extends E> c) {
		this(c.size());
		for (E e : c)
			add(e);
	}

	// 空间不够是生成新的空间，否则返回原有buffer
	private Object[] newBuffer(int minCap) {
		if (minCap <= buffer.length - 1)
			return buffer;

		int newCap = buffer.length * 3 / 2;
		if (newCap < minCap)
			newCap = minCap;
		return new Object[newCap + 1];
	}

	@SuppressWarnings("unchecked")
	@Override
	public E get(int index) {
		if (index < 0 || index >= size())
			throw new IndexOutOfBoundsException("index: " + index + ", size: " + size());

		return (E) buffer[(begin + index) % buffer.length];
	}

	@SuppressWarnings("unchecked")
	@Override
	public E set(int index, E e) {
		if (index < 0 || index >= size())
			throw new IndexOutOfBoundsException("index: " + index + ", size: " + size());

		int i = (begin + index) % buffer.length;
		E ret = (E) buffer[i];
		buffer[i] = e;
		return ret;
	}

	public void addFirst(E e) {
		add(0, e);
	}

	public void addLast(E e) {
		add(size(), e);
	}

	@Override
	public void add(int index, E e) {
		int size = size();
		if (index < 0 || index > size)
			throw new IndexOutOfBoundsException("index: " + index + ", size: " + size);

		Object[] newBuffer = newBuffer(size + 1);
		if (newBuffer == buffer) {
			if (index < size - index) {
				// 左半部分左移一个位置
				int src = begin;
				int dst = (begin + buffer.length - 1) % buffer.length;
				for (int i = 0; i < index; ++i) {
					buffer[dst] = buffer[src];

					src = (src + 1) % buffer.length;
					dst = (dst + 1) % buffer.length;
				}

				// 插入数据，更改游标位置
				buffer[src] = e;
				begin = (begin + buffer.length - 1) % buffer.length;
			} else {
				// 右半部分右移一个位置
				int src = (end + buffer.length - 1) % buffer.length;
				int dst = end;
				for (int i = size - index; i > 0; --i) {
					buffer[dst] = buffer[src];

					src = (src + buffer.length - 1) % buffer.length;
					dst = (dst + buffer.length - 1) % buffer.length;
				}

				// 插入数据，更改游标位置
				buffer[src] = e;
				end = (end + 1) % buffer.length;
			}
		} else {
			// 复制左半部分
			int src = begin;
			for (int i = 0; i < index; ++i) {
				newBuffer[i] = buffer[src];
				src = (src + 1) % buffer.length;
			}

			// 插入元素
			newBuffer[index] = e;

			// 复制右半部分
			src = (begin + index) % buffer.length;
			for (int i = index; i < size; ++i) {
				newBuffer[i + 1] = buffer[src];
				src = (src + 1) % buffer.length;
			}

			begin = 0;
			end = size + 1;
			buffer = newBuffer;
		}
		++modCount;
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		int size = size();
		if (index < 0 || index > size)
			throw new IndexOutOfBoundsException("index: " + index + ", size: " + size);
		if (c.size() == 0)
			return false;

		Object[] newBuffer = newBuffer(size + c.size());
		if (newBuffer == buffer) {
			if (index < size - index) {
				// 左半部分左移
				int src = begin;
				int dst = (begin + buffer.length - c.size()) % buffer.length;
				for (int i = 0; i < index; ++i) {
					buffer[dst] = buffer[src];

					src = (src + 1) % buffer.length;
					dst = (dst + 1) % buffer.length;
				}

				// 插入数据，更改游标位置
				for (E e : c) {
					buffer[dst] = e;
					dst = (dst + 1) % buffer.length;
				}
				begin = (begin + buffer.length - c.size()) % buffer.length;
			} else {
				// 右半部分右移
				int src = (end + buffer.length - 1) % buffer.length;
				int dst = (end + c.size() - 1) % buffer.length;
				for (int i = size - index; i > 0; --i) {
					buffer[dst] = buffer[src];

					src = (src + buffer.length - 1) % buffer.length;
					dst = (dst + buffer.length - 1) % buffer.length;
				}

				// 插入数据，更改游标位置
				dst = (begin + index) % buffer.length;
				for (E e : c) {
					buffer[dst] = e;
					dst = (dst + 1) % buffer.length;
				}
				end = (end + c.size()) % buffer.length;
			}
		} else {
			// 复制左半部分
			int src = begin;
			for (int i = 0; i < index; ++i) {
				newBuffer[i] = buffer[src];
				src = (src + 1) % buffer.length;
			}

			// 插入元素
			int dst = index;
			for (E e : c) {
				newBuffer[dst] = e;
				++dst;
			}

			// 复制右半部分
			src = (begin + index) % buffer.length;
			for (int i = index; i < size; ++i) {
				newBuffer[i + c.size()] = buffer[src];
				src = (src + 1) % buffer.length;
			}

			begin = 0;
			end = size + c.size();
			buffer = newBuffer;
		}
		++modCount;
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public E remove(int index) {
		int size = size();
		if (index < 0 || index >= size)
			throw new IndexOutOfBoundsException("index: " + index + ", size: " + size);

		E ret = (E) buffer[(begin + index) % buffer.length];
		if (index < size - index - 1) {
			// 左半部分右移一个位置
			int src = (begin + index + buffer.length - 1) % buffer.length;
			int dst = (begin + index) % buffer.length;
			for (int i = 0; i < index; ++i) {
				buffer[dst] = buffer[src];

				src = (src + buffer.length - 1) % buffer.length;
				dst = (dst + buffer.length - 1) % buffer.length;
			}

			// 更改游标位置
			begin = (begin + 1) % buffer.length;
		} else {
			// 右半部分左移一个位置
			int src = (begin + index) % buffer.length;
			int dst = (begin + index + 1) % buffer.length;
			for (int i = size - index - 1; i > 0; --i) {
				buffer[dst] = buffer[src];

				src = (src + 1) % buffer.length;
				dst = (dst + 1) % buffer.length;
			}

			// 更改游标位置
			end = (end + buffer.length - 1) % buffer.length;
		}
		++modCount;
		return ret;
	}

	@Override
	protected void removeRange(int from, int to) {
		int size = size();
		if (from < 0 || from > to || to > size)
			throw new IndexOutOfBoundsException("from: " + from + ", to: " + to + ", size: " + size);

		// 先删除数据
		int src = (begin + from) % buffer.length;
		for (int i = from; i < to; ++i) {
			buffer[src] = null;
			src = (src + 1) % buffer.length;
		}

		if (from < size - to) {
			// 左半部分右移
			src = (begin + from + buffer.length - 1) % buffer.length;
			int dst = (begin + to + buffer.length - 1) % buffer.length;
			for (int i = 0; i < from; ++i) {
				buffer[dst] = buffer[src];

				src = (src + buffer.length - 1) % buffer.length;
				dst = (dst + buffer.length - 1) % buffer.length;
			}

			// 更改游标位置
			begin = (begin + to - from) % buffer.length;
		} else {
			// 右半部分左移
			src = (begin + to) % buffer.length;
			int dst = (begin + from) % buffer.length;
			for (int i = size - to; i > 0; --i) {
				buffer[dst] = buffer[src];

				src = (src + 1) % buffer.length;
				dst = (dst + 1) % buffer.length;
			}

			// 更改游标位置
			end = (end + buffer.length - (to - from)) % buffer.length;
		}
		++modCount;
	}

	@Override
	public int size() {
		return (end + buffer.length - begin) % buffer.length;
	}
};

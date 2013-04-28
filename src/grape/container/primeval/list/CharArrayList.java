package grape.container.primeval.list;

import java.io.Serializable;
import java.util.Collection;
import java.util.RandomAccess;

public class CharArrayList implements RandomAccess, Cloneable, Serializable {

    private static final long serialVersionUID = 1L;

    private int size = 0;
    private char[] buffer = null;

    public CharArrayList() {}

    public CharArrayList(int initialCapacity) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal capacity:" + initialCapacity);
        else if (initialCapacity == 0)
            return;

        buffer = new char[initialCapacity];
    }

    public CharArrayList(Collection<Character> c) {
        buffer = new char[c.size()];
        for (Character s : c)
            buffer[size++] = s;
    }

    public CharArrayList(CharArrayList c) {
        size = c.size;
        if (size > 0) {
            // XXX System.arraycopy() 比 Arrays.copyOfRange() 快
            buffer = new char[size];
            System.arraycopy(c.buffer, 0, buffer, 0, size);
        }
    }

    public CharArrayList(char[] values) {
        this(values, 0, values.length);
    }

    public CharArrayList(char[] values, int from, int to) {
        if (from < 0 || from > to || to > values.length)
            throw new IllegalArgumentException();

        size = to - from;
        if (size > 0) {
            buffer = new char[size];
            System.arraycopy(values, from, buffer, 0, size);
        }
    }

    /**
     * 准备插入动作
     */
    private void prepareInserting(int index, int len) {
        if (buffer != null && buffer.length >= size + len) {
            System.arraycopy(buffer, index, buffer, index + len, size - index);
            size += len;
            return;
        }

        int new_cap;
        if (buffer == null) {
            new_cap = size + len;
        } else {
            new_cap = buffer.length * 3 / 2;
            if (new_cap < size + len)
                new_cap = size + len;
        }
        
        char[] new_buf = new char[new_cap];
        if (buffer != null && size > 0) {
            System.arraycopy(buffer, 0, new_buf, 0, index);
            System.arraycopy(buffer, index, new_buf, index + len, size - index);
        }
        size += len;
        buffer = new_buf;
    }

    public void add(char v) {
        add(size, v);
    }

    public void addAll(Collection<Character> c) {
        addAll(size, c);
    }

    public void addAll(CharArrayList c) {
        addAll(size, c);
    }

    public void addAll(char[] values) {
        addAll(size, values, 0, values.length);
    }

    public void addAll(char[] values, int from, int to) {
        addAll(size, values, from, to);
    }

    public void add(int index, char value) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException("Index:" + index + " size:" + size);

        prepareInserting(index, 1);
        buffer[index] = value;
    }

    public void addAll(int index, Collection<Character> c) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException("Index:" + index + " size:" + size);

        prepareInserting(index, c.size());
        for (Character s : c)
            buffer[index++] = s;
    }

    public void addAll(int index, CharArrayList c) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException("Index:" + index + " size:" + size);

        prepareInserting(index, c.size);
        System.arraycopy(c.buffer, 0, buffer, index, c.size);
    }

    public void addAll(int index, char[] values) {
        addAll(index, values, 0, values.length);
    }

    public void addAll(int index, char[] values, int from, int to) {
        if (index < 0 || index > size || from < 0 || from > to || to > values.length)
            throw new IllegalArgumentException();

        prepareInserting(index, to - from);
        System.arraycopy(values, from, buffer, index, to - from);
    }

    public char remove(int index) {
        char ret = buffer[index];
        removeRange(index, index + 1);
        return ret;
    }

    public void removeRange(int from, int to) {
        if (from < 0 || from > to || to > size)
            throw new IllegalArgumentException();

        if (buffer != null)
            System.arraycopy(buffer, to, buffer, from, size - to);
        size -= to - from;
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

    public boolean removeAll(CharArrayList c) {
        int old_size = size;
        for (int i = 0; i < c.size; ++i)
            removeAll(c.buffer[i]);
        return old_size != size;
    }

    public boolean removeAll(char[] values) {
        return removeAll(values, 0, values.length);
    }

    /**
     * @param from 在 values 数组中的起始位置
     * @param to 在 values 数组中的终止位置
     */
    public boolean removeAll(char[] values, int from, int to) {
        if (from < 0 || from > to || to > values.length)
            throw new IllegalArgumentException();

        int old_size = size;
        for (int i = from; i < to; ++i)
            removeAll(values[i]);
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

    public boolean retainAll(CharArrayList c) {
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
     * 没有找到则返回 -1
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

    public boolean containsAll(CharArrayList c) {
        for (int i = 0; i < c.size; ++i)
            if (!contains(c.buffer[i]))
                return false;
        return true;
    }

    public boolean conatainsAll(char[] values) {
        return containsAll(values, 0, values.length);
    }

    public boolean containsAll(char[] values, int from, int to) {
        if (from < 0 || from > to || to > values.length)
            throw new IllegalArgumentException();

        for (int i = from; i < to; ++i)
            if (!contains(values[i]))
                return false;
        return true;
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
    public CharArrayList clone() {
        return new CharArrayList(buffer, 0, size);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CharArrayList))
            return false;

        CharArrayList x = (CharArrayList) o;
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

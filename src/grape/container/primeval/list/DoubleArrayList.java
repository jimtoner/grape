package grape.container.primeval.list;

import java.io.Serializable;
import java.util.Collection;
import java.util.RandomAccess;

public class DoubleArrayList implements RandomAccess, Cloneable, Serializable {

    private static final long serialVersionUID = 1L;

    private int size = 0;
    private double[] buffer = null;

    public DoubleArrayList() {}

    public DoubleArrayList(int initialCapacity) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal capacity:" + initialCapacity);
        else if (initialCapacity == 0)
            return;

        buffer = new double[initialCapacity];
    }

    public DoubleArrayList(Collection<Double> c) {
        buffer = new double[c.size()];
        for (Double s : c)
            buffer[size++] = s;
    }

    public DoubleArrayList(DoubleArrayList c) {
        size = c.size;
        if (size > 0) {
            // XXX System.arraycopy() 比 Arrays.copyOfRange() 快
            buffer = new double[size];
            System.arraycopy(c.buffer, 0, buffer, 0, size);
        }
    }

    public DoubleArrayList(double[] values) {
        this(values, 0, values.length);
    }

    public DoubleArrayList(double[] values, int from, int to) {
        if (from < 0 || from > to || to > values.length)
            throw new IllegalArgumentException();

        size = to - from;
        if (size > 0) {
            buffer = new double[size];
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
        
        double[] new_buf = new double[new_cap];
        if (buffer != null && size > 0) {
            System.arraycopy(buffer, 0, new_buf, 0, index);
            System.arraycopy(buffer, index, new_buf, index + len, size - index);
        }
        size += len;
        buffer = new_buf;
    }

    public void add(double v) {
        add(size, v);
    }

    public void addAll(Collection<Double> c) {
        addAll(size, c);
    }

    public void addAll(DoubleArrayList c) {
        addAll(size, c);
    }

    public void addAll(double[] values) {
        addAll(size, values, 0, values.length);
    }

    public void addAll(double[] values, int from, int to) {
        addAll(size, values, from, to);
    }

    public void add(int index, double value) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException("Index:" + index + " size:" + size);

        prepareInserting(index, 1);
        buffer[index] = value;
    }

    public void addAll(int index, Collection<Double> c) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException("Index:" + index + " size:" + size);

        prepareInserting(index, c.size());
        for (Double s : c)
            buffer[index++] = s;
    }

    public void addAll(int index, DoubleArrayList c) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException("Index:" + index + " size:" + size);

        prepareInserting(index, c.size);
        System.arraycopy(c.buffer, 0, buffer, index, c.size);
    }

    public void addAll(int index, double[] values) {
        addAll(index, values, 0, values.length);
    }

    public void addAll(int index, double[] values, int from, int to) {
        if (index < 0 || index > size || from < 0 || from > to || to > values.length)
            throw new IllegalArgumentException();

        prepareInserting(index, to - from);
        System.arraycopy(values, from, buffer, index, to - from);
    }

    public double remove(int index) {
        double ret = buffer[index];
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

    public boolean removeAll(double value) {
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

    public boolean removeAll(Collection<Double> c) {
        int old_size = size;
        for (Double s : c)
            removeAll(s);
        return old_size != size;
    }

    public boolean removeAll(DoubleArrayList c) {
        int old_size = size;
        for (int i = 0; i < c.size; ++i)
            removeAll(c.buffer[i]);
        return old_size != size;
    }

    public boolean removeAll(double[] values) {
        return removeAll(values, 0, values.length);
    }

    /**
     * @param from 在 values 数组中的起始位置
     * @param to 在 values 数组中的终止位置
     */
    public boolean removeAll(double[] values, int from, int to) {
        if (from < 0 || from > to || to > values.length)
            throw new IllegalArgumentException();

        int old_size = size;
        for (int i = from; i < to; ++i)
            removeAll(values[i]);
        return old_size != size;
    }

    public boolean retainAll(Collection<Double> c) {
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

    public boolean retainAll(DoubleArrayList c) {
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

    public double get(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException();

        return buffer[index];
    }

    public double set(int index, double value) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException();

        double ret = buffer[index];
        buffer[index] = value;
        return ret;
    }

    /**
     * 没有找到则返回 -1
     */
    public int indexOf(double v) {
        return indexOf(v, 0);
    }

    public int indexOf(double v, int search_begin) {
        if (search_begin < 0 || search_begin > size)
            throw new IndexOutOfBoundsException();
        for (int i = search_begin; i < size; ++i)
            if (buffer[i] == v)
                return i;
        return -1;
    }

    public int lastIndexOf(double v) {
        return lastIndexOf(v, size - 1);
    }

    public int lastIndexOf(double v, int search_begin) {
        if (search_begin < -1 || search_begin >= size)
            throw new IndexOutOfBoundsException();
        for (int i = search_begin; i >= 0; --i)
            if (buffer[i] == v)
                return i;
        return -1;
    }

    public boolean contains(double v) {
        for (int i = 0; i < size; ++i)
            if (buffer[i] == v)
                return true;
        return false;
    }

    public boolean contailsAll(Collection<Double> c) {
        for (Double s : c)
            if (!contains(s))
                return false;
        return true;
    }

    public boolean containsAll(DoubleArrayList c) {
        for (int i = 0; i < c.size; ++i)
            if (!contains(c.buffer[i]))
                return false;
        return true;
    }

    public boolean conatainsAll(double[] values) {
        return containsAll(values, 0, values.length);
    }

    public boolean containsAll(double[] values, int from, int to) {
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

    public double[] toArray() {
        double[] ret = new double[size];
        System.arraycopy(buffer, 0, ret, 0, size);
        return ret;
    }

    public void toArray(double[] arr, int begin) {
        if (begin < 0 || begin + size > arr.length)
            throw new IllegalArgumentException();
        System.arraycopy(buffer, 0, arr, begin, size);
    }

    @Override
    public DoubleArrayList clone() {
        return new DoubleArrayList(buffer, 0, size);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof DoubleArrayList))
            return false;

        DoubleArrayList x = (DoubleArrayList) o;
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
                long bits = Double.doubleToLongBits(buffer[i]);
                hash = (31 * hash) + (int)(bits ^ (bits >>> 32));
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
            sb.append(Double.toString(buffer[i]));
        }
        sb.append(']');
        return sb.toString();
    }
}

package grape.container.primeval.objectmap;

import java.io.Serializable;
import java.util.*;

public class ShortObjectHashMap <V> implements Cloneable, Serializable {

	private static final long serialVersionUID = 1L;

    /**
     * Min capacity (other than zero) for a HashMap. Must be a power of two
     * greater than 1 (and less than 1 << 30).
     */
    private static final int MINIMUM_CAPACITY = 4;

    /**
     * Max capacity for a HashMap. Must be a power of two >= MINIMUM_CAPACITY.
     */
    private static final int MAXIMUM_CAPACITY = 1 << 30;

    public static class Entry <V> {
        short key;
        V value;
        Entry<V> next;
        
        Entry(short k, V v, Entry<V> n) {
            this.key = k;
            this.value = v;
            this.next = n;
        }
        
        public short getKey() {
            return key;
        }
        
        public V getValue() {
            return value;
        }
        
        @Override
        public String toString() {
            return Short.toString(key) + ":" + value;
        }
    }

    private Entry<V>[] table;
    private int size;
    
    /**
     * The table is rehashed when its size exceeds this threshold.
     * The value of this field is generally .75 * capacity, except when
     * the capacity is zero, as described in the EMPTY_TABLE declaration
     * above.
     */
    private transient int threshold;
    
    public ShortObjectHashMap() {
        this(MINIMUM_CAPACITY);
    }
    
    @SuppressWarnings("unchecked")
    public ShortObjectHashMap(int capacity) {
        if (capacity < MINIMUM_CAPACITY)
            capacity = MINIMUM_CAPACITY;
        else if (capacity > MAXIMUM_CAPACITY)
            capacity = MAXIMUM_CAPACITY;
        else
            capacity = roundUpToPowerOfTwo(capacity);
        table = (Entry<V>[]) new Entry[capacity];
        threshold = (capacity >> 1) + (capacity >> 2); // 3/4 capacity
    }
    
    public V put(short key, V value) {
        int h = hash(key);

        Entry<V>[] t = table;
        int index = h & (t.length - 1);
        for (Entry<V> e = t[index]; e != null; e = e.next) {
            if (e.key == key) {
                V oldValue = e.value;
                e.value = value;
                return oldValue;
            }
        }
        
        if (++size > threshold) {
            doubleCapacity();
            t = table;
            index = h & (t.length - 1);
        }
        t[index] = new Entry<V>(key, value, t[index]); // add new entry
        return null;
    }
    
    public void putAll(ShortObjectHashMap<? extends V> m) {
        if (m.size == 0)
            return;

        Entry<? extends V>[] t = m.table;
        for (int i = 0, len = t.length; i < len; ++i) {
            for (Entry<? extends V> e = t[i]; e != null; e = e.next) {
                put(e.key, e.value);
            }
        }
    }
    
    public V get(short key) {
        int h = hash(key);
        Entry<V>[] t = table;
        for (Entry<V> e = t[h & (t.length - 1)]; e != null; e = e.next) {
            if (e.key == key)
                return e.value;
        }
        return null;
    }

    public V remove(short key) {
        int h = hash(key);
        Entry<V>[] t = table;
        int index = h & (t.length - 1);
        for (Entry<V> e = t[index], pre = null; e != null; pre = e, e = e.next) {
            if (e.key == key) {
                if (pre == null)
                    t[index] = e.next;
                else
                    pre.next = e.next;
                --size;
                return e.value;
            }
        }
        return null;
    }
    
    public void clear() {
        Arrays.fill(table, null);
        size = 0;
    }

    public Iterator<Entry<V> > iterator() {
    	return new Iterator<Entry<V>>() {
    		int nextIndex;
    		Entry<V> currentEntry, nextEntry;

    		{
    			Entry<V>[] t = ShortObjectHashMap.this.table;
    			for (int i = 0, len = t.length; i < len; ++i) {
    				if (t[i] != null) {
    					nextIndex = i;
    					nextEntry = t[i];
    					break;
    				}
    			}
    		}

    		private void advance() {
    			currentEntry = nextEntry;
    			nextEntry = nextEntry.next;
    			if (nextEntry == null) {
    				Entry<V>[] t = ShortObjectHashMap.this.table;
    				while (++nextIndex < t.length) {
    					if (t[nextIndex] != null) {
    						nextEntry = t[nextIndex];
    						break;
    					}
    				}
    			}
    		}

			@Override
			public boolean hasNext() {
				return nextEntry != null;
			}

			@Override
			public Entry<V> next() {
				if (!hasNext())
					throw new NoSuchElementException();

				advance();
				return currentEntry;
			}

			@Override
			public void remove() {
				ShortObjectHashMap.this.remove(currentEntry.key);
			}
		};
    }

    public boolean containsKey(short key) {
        int h = hash(key);
        Entry<V>[] t = table;
        for (Entry<V> e = t[h & (t.length - 1)]; e != null; e = e.next) {
            if (e.key == key)
                return true;
        }
        return false;
    }

    public boolean containsValue(V value) {
        Entry<V>[] t = table;
        for (int i = 0, len = t.length; i < len; ++i)
            for (Entry<V> e = t[i]; e != null; e = e.next)
                if (e.value == value || (value != null && value.equals(e.value)))
                    return true;
        return false;
    }
    
    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }
    
    private static int hash(short v) {
        // hash
        int h = v;
		// secondary hash
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }

    /**
     * Returns the smallest power of two >= its argument, with several caveats:
     * If the argument is negative but not Integer.MIN_VALUE, the method returns
     * zero. If the argument is > 2^30 or equal to Integer.MIN_VALUE, the method
     * returns Integer.MIN_VALUE. If the argument is zero, the method returns
     * zero.
     */
    private static int roundUpToPowerOfTwo(int i) {
        i--; // If input is a power of two, shift its high-order bit right

        // "Smear" the high-order bit all the way to the right
        i |= i >>>  1;
        i |= i >>>  2;
        i |= i >>>  4;
        i |= i >>>  8;
        i |= i >>> 16;

        return i + 1;
    }
    
    @SuppressWarnings("unchecked")
    private void doubleCapacity() {
        if (table.length == MAXIMUM_CAPACITY)
            return;
            
        int newCapacity = table.length * 2;
        Entry<V>[] newTable = (Entry<V>[]) new Entry[newCapacity];
        threshold = (newCapacity >> 1) + (newCapacity >> 2); // 3/4 capacity
        if (size == 0) {
            table = newTable;
            return;
        }
        
        for (int i = 0, len = table.length; i < len; ++i) {
            Entry<V> e = table[i];
            while (e != null) {
                Entry<V> next = e.next;
                int index = hash(e.key) & (newTable.length - 1);
                e.next = newTable[index];
                newTable[index] = e;
                e = next;
            }
        }
        table = newTable;
    }
    
    @Override
	public String toString() {
    	StringBuilder sb = new StringBuilder("{");
    	Iterator<Entry<V> > iter = iterator();
    	while (iter.hasNext()) {
    		Entry<V> e = iter.next();
    		if (sb.length() != 1)
    			sb.append(", ");
    		sb.append(Short.toString(e.key));
    		sb.append(":");
    		sb.append(e.value);
    	}
    	sb.append("}");
    	return sb.toString();
    }
}


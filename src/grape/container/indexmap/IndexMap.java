package grape.container.indexmap;

/**
 * 管理 [0, maxIndex) 范围内的整数映射
 *
 * 支持快速的顺序迭代其中的值
 *
 * @author jingqi
 */
public class IndexMap <T> implements Iterable<T> {

	protected final int BLOCK_SHIFT;
	protected final int BLOCK_MASK;
	protected final int BLOCK_SIZE;

	protected int firstIndex;
	protected int lastIndex;

	protected final Object[][] blocks;

	/**
	 * @param maxIndex 设置了之后，index取值范围只能在 [0, maxIndex) 区间内(左闭右开区间)
	 */
	public IndexMap(int maxIndex) {
		int bitLength = 32 - Integer.numberOfLeadingZeros(maxIndex - 1);
		BLOCK_SHIFT = bitLength / 2;
		BLOCK_MASK = ~((~0) << BLOCK_SHIFT);
		BLOCK_SIZE = 1 << BLOCK_SHIFT;
		int blockCount = (maxIndex + BLOCK_SIZE - 1) / BLOCK_SIZE;
		blocks = new Object[blockCount][];

		firstIndex = -1;
		lastIndex = -2;
	}

	@SuppressWarnings("unchecked")
	public T get(int index) {
		int pdi = index >> BLOCK_SHIFT;
		if (blocks[pdi] == null)
			return null;
		int pei = index & BLOCK_MASK;
		return (T) blocks[pdi][pei];
	}

	@SuppressWarnings("unchecked")
	public T put(int index, T data) {
		if (data == null)
			throw new IllegalArgumentException("null is not expected");

		// 维护 row range
		if (firstIndex < 0 || index < firstIndex)
			firstIndex = index;
		if (lastIndex < 0 || index > lastIndex)
			lastIndex = index;

		int pdi = index >> BLOCK_SHIFT;
		if (blocks[pdi] == null)
			blocks[pdi] = new Object[BLOCK_SIZE];
		int pei = index & BLOCK_MASK;
		Object ret = blocks[pdi][pei];
		blocks[pdi][pei] = data;
		return (T) ret;
	}

	@SuppressWarnings("unchecked")
	public T remove(int index) {
		int pdi = index >> BLOCK_SHIFT;
		if (blocks[pdi] == null)
			return null;
		int pei = index & BLOCK_MASK;
		Object ret = blocks[pdi][pei];
		blocks[pdi][pei] = null;
		return (T) ret;
	}

	public int getFirstIndex() {
		return firstIndex;
	}

	public int getLastIndex() {
		return lastIndex;
	}

	@Override
	public IndexMapIterator<T> iterator() {
		return iterator(firstIndex, lastIndex);
	}

	public IndexMapIterator<T> iterator(int first, final int last) {
		return new IndexMapIterator<T>(this, first, last);
	}
}

package grape.container.indexmap;

import java.util.Arrays;

/**
 * 管理 [0, maxIndex) 范围内的<b>稀疏索引</b>映射
 *
 * 支持快速的顺序迭代其中的值
 *
 * @author jingqi
 */
@SuppressWarnings("unchecked")
public class ExtraSparseIndexMap <T> implements Iterable<T> {

	protected int firstIndex = -1;
	protected int lastIndex = -2;

	protected int OFFSET_MASK;

	protected int PAGE_SHIFT;
	protected int PAGE_MASK;

	protected int PAGE_ENTRY_SHIFT;


	protected final Object[][][] blocks;

	/**
	 * @param maxIndex 设置了之后，index取值范围只能在 [0, maxIndex) 区间内(左闭右开区间)
	 */
	public ExtraSparseIndexMap(int maxIndex) {
		if (maxIndex <= 0)
			throw new IllegalArgumentException();

		if (maxIndex < 0x100) {
			// 退化成一维数组
			OFFSET_MASK = 0xff;

			PAGE_SHIFT = 8;
			PAGE_MASK = 0;

			PAGE_ENTRY_SHIFT = 8;
			blocks = new Object[1][][];
		} else if (maxIndex < 0x10000) {
			// 退化为二维数组
			OFFSET_MASK = 0xff;

			PAGE_SHIFT = 8;
			PAGE_MASK = 0xff;

			PAGE_ENTRY_SHIFT = 16;
			blocks = new Object[1][][];
		} else {
			int bitLength = 32 - Integer.numberOfLeadingZeros(maxIndex - 1);
			PAGE_SHIFT = bitLength / 3;
			OFFSET_MASK = ~((~0) << PAGE_SHIFT);

			PAGE_ENTRY_SHIFT = PAGE_SHIFT * 2;
			PAGE_MASK = OFFSET_MASK;

			blocks = new Object[((maxIndex - 1) >> PAGE_ENTRY_SHIFT) + 1][][];
		}
	}

	public T get(int index) {
		if (index < 0)
			throw new IllegalArgumentException();

		int page_entry = index >> PAGE_ENTRY_SHIFT;
		if (blocks[page_entry] == null)
			return null;
		int page_index = (index >> PAGE_SHIFT) & PAGE_MASK;
		if (blocks[page_entry][page_index] == null)
			return null;
		int offset = index & OFFSET_MASK;
		return (T) blocks[page_entry][page_index][offset];
	}

	public T put(int index, T data) {
		if (index < 0 || data == null) // null 是不允许的
			throw new IllegalArgumentException();

		// 维护上下边界
		if (firstIndex < 0 || index < firstIndex)
			firstIndex = index;
		if (lastIndex < 0 || index > lastIndex)
			lastIndex = index;

		int page_entry = index >> PAGE_ENTRY_SHIFT;
		if (blocks[page_entry] == null)
			blocks[page_entry] = new Object[PAGE_MASK + 1][];
		int page_index = (index >> PAGE_SHIFT) & PAGE_MASK;
		if (blocks[page_entry][page_index] == null)
			blocks[page_entry][page_index] = new Object[OFFSET_MASK + 1];
		int offset = index & OFFSET_MASK;
		Object ret = blocks[page_entry][page_index][offset];
		blocks[page_entry][page_index][offset] = data;
		return (T) ret;
	}

	public T remove(int index) {
		if (index < 0)
			throw new IllegalArgumentException();

		int page_entry = index >> PAGE_ENTRY_SHIFT;
		if (blocks[page_entry] == null)
			return null;
		int page_index = (index >> PAGE_SHIFT) & PAGE_MASK;
		if (blocks[page_entry][page_index] == null)
			return null;
		int offset = index & OFFSET_MASK;
		Object ret = blocks[page_entry][page_index][offset];
		blocks[page_entry][page_index][offset] = null;
		return (T) ret;
	}

	public void clear() {
		Arrays.fill(blocks, null);
		firstIndex = -1;
		lastIndex = -2;
	}

	public int getFirstIndex() {
		return firstIndex;
	}

	public int getLastIndex() {
		return lastIndex;
	}

	@Override
	public ExtraSparseIndexMapIterator<T> iterator() {
		return iterator(firstIndex, lastIndex);
	}

	public ExtraSparseIndexMapIterator<T> iterator(int first, int last) {
		return new ExtraSparseIndexMapIterator<T>(this, first, last);
	}
}

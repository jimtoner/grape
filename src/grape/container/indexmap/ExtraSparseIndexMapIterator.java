package grape.container.indexmap;

import java.util.Iterator;

/**
 * {@link ExtraSparseIndexMap} 的迭代器
 */
public class ExtraSparseIndexMapIterator <T> implements Iterator<T> {

	final ExtraSparseIndexMap<T> map;
	final int last;

	int nextIndex;
	int currentIndex = -1;

	public ExtraSparseIndexMapIterator(ExtraSparseIndexMap<T> map, int first, int last) {
		this.map = map;
		this.last = last;

		nextIndex = first - 1;
		advance();
	}

	private void advance() {
		++nextIndex;

		while (true) {
			// check bound
			if (nextIndex < map.firstIndex)
				nextIndex = map.firstIndex;
			if (nextIndex > map.lastIndex || nextIndex > last)
				break; // the end

			// check block
			int page_entry = nextIndex >> map.PAGE_ENTRY_SHIFT;
			if (map.blocks[page_entry] == null) {
				nextIndex = (page_entry + 1) << map.PAGE_ENTRY_SHIFT;
				continue;
			}
			int page_index = (nextIndex >> map.PAGE_SHIFT) & map.PAGE_MASK;
			if (map.blocks[page_entry][page_index] == null) {
				nextIndex = ((nextIndex >> map.PAGE_SHIFT) + 1) << map.PAGE_SHIFT;
				continue;
			}
			int offset = nextIndex & map.OFFSET_MASK;
			if (map.blocks[page_entry][page_index][offset] == null) {
				++nextIndex;
				continue;
			}

			// done
			break;
		}
	}

	@Override
	public boolean hasNext() {
		return nextIndex <= map.lastIndex && nextIndex <= last;
	}

	@Override
	public T next() {
		if (!hasNext())
			throw new IllegalStateException();

		currentIndex = nextIndex;
		advance();
		return map.get(currentIndex);
	}

	public int index() {
		return currentIndex;
	}

	@Override
	public void remove() {
		map.remove(currentIndex);
	}
}

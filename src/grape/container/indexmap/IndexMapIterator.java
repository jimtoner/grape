package grape.container.indexmap;

import java.util.Iterator;

/**
 * {@link SparseIndexMap} 的迭代器
 */
public class IndexMapIterator <T> implements Iterator<T> {

	final SparseIndexMap<T> map;
	final int last;

	int nextIndex;
	int currentIndex = -1;

	public IndexMapIterator(SparseIndexMap<T> map, int first, int last) {
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
			int pdi = nextIndex >> map.BLOCK_SHIFT;
			if (map.blocks[pdi] == null) {
				nextIndex = (pdi + 1) << map.BLOCK_SHIFT;
				continue;
			}

			// check row
			int pei = nextIndex & map.BLOCK_MASK;
			if (map.blocks[pdi][pei] == null) {
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

package grape.container.indexmap;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SparseIndexMapTest {

	@Test
	public void test() {
		SparseIndexMap<Integer> s = new SparseIndexMap<Integer>(0x100);
		s.put(0xff, 12);
		assertTrue(s.get(0xff) == 12);

		s = new SparseIndexMap<Integer>(0x1000);
		s.put(0x121, 12);
		assertTrue(s.get(0x121) == 12);
	}
}

package grape.container.indexmap;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ExtraSparseIndexMapIteratorTest {

	@Test
	public void test() {
		ExtraSparseIndexMap<Integer> s = new ExtraSparseIndexMap<Integer>(0x100);
		s.put(0xff, 12);
		assertTrue(s.get(0xff) == 12);

		s = new ExtraSparseIndexMap<Integer>(0x1000);
		s.put(0x121, 12);
		assertTrue(s.get(0x121) == 12);

		s = new ExtraSparseIndexMap<Integer>(0xffffff);
		s.put(0x121, 12);
		assertTrue(s.get(0x121) == 12);
		s.put(0xfffff, 13);
		assertTrue(s.get(0xfffff) == 13);
	}
}

package grape.container.primeval.objectmap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Test;

public class LongObjectHashMapTest {

	@Test
	public void test() {
		LongObjectHashMap<String> m = new LongObjectHashMap<String>();
		m.put(1, "1");
		m.put(100, "100");
		m.put(58, "58");
		m.put(33, "33");
		m.put(99, "99");
		assertEquals(5, m.size());
		assertEquals("58", m.get(58));

		assertEquals("99", m.put(99, "99.0"));
		assertEquals("99.0", m.put(99, "99.14"));
		assertEquals(5, m.size());

		assertEquals("100", m.remove(100));
		assertEquals(4, m.size());
	}

	@Test
	public void testIter() {
		LongObjectHashMap<String> m = new LongObjectHashMap<String>();
		m.put(1, "1");
		m.put(100, "100");
		m.put(58, "58");
		m.put(33, "33");
		m.put(99, "99");

		Iterator<LongObjectHashMap.Entry<String> > iter = m.iterator();
		int c = 0;
//		System.out.println(m);
		while (iter.hasNext()) {
			LongObjectHashMap.Entry<String> e = iter.next();
			++c;
//			System.out.println(e.key + ":" + e.value);
			if (e.key == 58)
				iter.remove();
		}
		assertEquals(5, c);
		assertEquals(4, m.size());
		assertTrue(!m.containsKey(58));
//		System.out.println(m);
	}

	@Test
	public void testAddAll() {
		LongObjectHashMap<String> m1 = new LongObjectHashMap<String>();
		m1.put(1, "1");
		m1.put(100, "100");
		m1.put(58, "58");
		m1.put(33, "33");
		m1.put(99, "99");

		LongObjectHashMap<String> m2 = new LongObjectHashMap<String>();
		m2.put(1, "1");
		m2.put(100, "100");
		m2.put(45, "45");

		assertEquals(5, m1.size());
		m1.putAll(m2);
		assertEquals(6, m1.size());
//		System.out.println(m1);
	}
}

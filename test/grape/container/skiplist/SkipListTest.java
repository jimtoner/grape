package grape.container.skiplist;

import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;

public class SkipListTest {

	@Test
	public void test() {
		SkipList<Integer, String> sl = new SkipList<Integer, String>();
		sl.put(2, "2");
		sl.put(4, "4");
		sl.put(3, "3");
		sl.put(6, "6");
		sl.put(5, "5");
		assertTrue(sl.containsKey(2));
	}

	@Test
	public void testRandom() {
		Random r = new Random();
		SkipList<Integer, Integer> sl = new SkipList<Integer, Integer>();
		int max = 1000;
		for (int i = 0; i < max; ++i) {
			sl.put(i, i);
		}
		for (int i = 8; i < 44; ++i) {
			sl.remove(i);
		}
		System.out.println(sl.toString());
	}
}

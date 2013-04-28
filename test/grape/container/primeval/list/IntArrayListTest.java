package grape.container.primeval.list;

import static org.junit.Assert.*;

import org.junit.Test;

public class IntArrayListTest {

	static void check(IntArrayList l, int[] arr) {
		assertEquals(arr.length, l.size());
		for (int i = 0; i < arr.length; ++i)
			assertEquals(arr[i], l.get(i));
	}

	@Test
	public void testSmoke() {
		IntArrayList l = new IntArrayList();
		assertEquals(0, l.size());
		assertTrue(l.isEmpty());
	}

	@Test
	public void testEquals() {
		IntArrayList l1 = new IntArrayList(), l2 = new IntArrayList(5);
		l1.add(2);
		l2.add(2);
		l1.add(8);
		l2.add(8);
		assertTrue(l1.equals(l2));

		l2.add(5);
		assertFalse(l1.equals(l2));
	}

	@Test
	public void testAddEle() {
		IntArrayList l = new IntArrayList();
		l.add(12);
		l.add(13);
		check(l, new int[]{12,13});

		l.add(1,4);
		check(l, new int[]{12,4,13});
	}

	@Test
	public void testAddAll() {
		IntArrayList l = new IntArrayList();
		l.addAll(new int[]{1,2,3,4}, 1, 3);
		check(l, new int[]{2,3});

		l.addAll(1, new int[]{6,7,8,9,10}, 2, 4);
		check(l, new int[]{2,8,9,3});
	}
}

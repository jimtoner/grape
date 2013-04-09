package grape.container.deqlist;

import static org.junit.Assert.assertEquals;
import grape.container.deqlist.ArrayDequeList;
import grape.container.deqlist.DequeList;

import org.junit.Test;

public class ArrayDequeListTest {

	private static ArrayDequeList<Integer> build(int[] c) {
		ArrayDequeList<Integer> ret = new ArrayDequeList<Integer>();
		for (int i = 0; i < c.length; ++i)
			ret.add(c[i]);
		return ret;
	}

	private static void check(DequeList<Integer> l, int[] c) {
		assertEquals(c.length, l.size());
		for (int i = 0; i < c.length; ++i)
			assertEquals(c[i], (int)l.get(i));
	}

	@Test
	public void testClone() {
		ArrayDequeList<Integer> l = build(new int[] {1,2,3});
		l.add(0, -1);
		check(l.clone(), new int[]{-1, 1, 2, 3});
	}

	@Test
	public void testAdd() {
		DequeList<Integer> l = new ArrayDequeList<Integer>();
		l.add(1);
		l.add(2);
		check(l, new int[] {1,2});

		l.add(0, -1);
		check(l, new int[] {-1, 1,2});

		l.add(1, -2);
		check(l, new int[] {-1, -2, 1, 2});

		l.add(3, -3);
		check(l, new int[] {-1, -2, 1, -3, 2});
	}

	@Test
	public void testAddAll() {
		ArrayDequeList<Integer> l = build(new int[] {-2, -1, 0, 1, 2}),
				ll = build(new int[] {8, 9});

		ArrayDequeList<Integer> t = l.clone();
		t.addAll(1, ll);
		check(t, new int[] {-2,8,9,-1,0,1,2});

		t = l.clone();
		t.addAll(4, ll);
		check(t, new int[] {-2,-1,0,1,8,9,2});
	}

	@Test
	public void testRemove() {
		DequeList<Integer> l = build(new int[] {-1, -2, 1, -3, 2});

		assertEquals(1, (int)l.remove(2));
		check(l, new int[] {-1, -2, -3, 2});

		assertEquals(-2, (int)l.remove(1));
		check(l, new int[] {-1, -3, 2});

		assertEquals(2, (int)l.remove(2));
		check(l, new int[] {-1, -3});
	}

	@Test
	public void testRemoveRange() {
		ArrayDequeList<Integer> l = build(new int[] {-1, -2, 1, -3, 2});

		ArrayDequeList<Integer> t = l.clone();
		t.removeRange(1, 3);
		check(t, new int[] {-1, -3, 2});

		t = l.clone();
		t.removeRange(2, 4);
		check(t, new int[] {-1, -2, 2});
	}
}

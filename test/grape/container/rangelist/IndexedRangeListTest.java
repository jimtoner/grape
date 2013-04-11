package grape.container.rangelist;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Test;

public class IndexedRangeListTest {

	private void checkAddOneRange(int first, int last, String expected) {
		IndexedRangeList l = new IndexedRangeList();
		l.addValueRange(4, 6);
		l.addValueRange(12, 15);
		assertTrue(l.isValid());
		assertEquals("[(4,6),(12,15)]", l.toString());

		l.addValueRange(first, last);
		assertTrue(l.isValid());
		assertEquals(expected, l.toString());
	}

	@Test
	public void testAddRange() {
		// 在头部添加
		// 		原始： [(4,6),(12,15)]
		checkAddOneRange(1, 2, "[(1,2),(4,6),(12,15)]");
		checkAddOneRange(1, 3, "[(1,6),(12,15)]");
		checkAddOneRange(1, 5, "[(1,6),(12,15)]");
		checkAddOneRange(1, 7, "[(1,7),(12,15)]");
		checkAddOneRange(5, 5, "[(4,6),(12,15)]");
		checkAddOneRange(5, 7, "[(4,7),(12,15)]");
		checkAddOneRange(5, 11, "[(4,15)]");
		checkAddOneRange(5, 12, "[(4,15)]");
		checkAddOneRange(5, 15, "[(4,15)]");
		checkAddOneRange(5, 16, "[(4,16)]");
		checkAddOneRange(4, 15, "[(4,15)]");

		// 在空隙添加
		checkAddOneRange(8, 10, "[(4,6),(8,10),(12,15)]");
		checkAddOneRange(8, 11, "[(4,6),(8,15)]");

		// 在尾部添加
		checkAddOneRange(13, 16, "[(4,6),(12,16)]");
		checkAddOneRange(13, 15, "[(4,6),(12,15)]");
		checkAddOneRange(16, 17, "[(4,6),(12,17)]");
		checkAddOneRange(17, 18, "[(4,6),(12,15),(17,18)]");
		checkAddOneRange(1, 20, "[(1,20)]");
	}

	@Test
	public void testSize() {
		IndexedRangeList l = new IndexedRangeList();
		l.addValueRange(1,2);
		l.addValueRange(12, 26); // [(1,2),(12,26)]
		assertTrue(l.isValid());
		assertEquals(1, l.getFirstValue());
		assertEquals(26, l.getLastValue());
		assertEquals(17, l.size());
	}

	@Test
	public void testGetAndIndexOf() {
		IndexedRangeList l = new IndexedRangeList();
		l.addValueRange(4,6);
		l.addValueRange(16, 23); // [(4,6),(16,23)]

		// get()
		assertEquals(4, l.get(0));
		assertEquals(5, l.get(1));
		assertEquals(6, l.get(2));
		assertEquals(16, l.get(3));
		assertEquals(17, l.get(4));
		assertEquals(23, l.get(10));

		// indexOf()
		assertEquals(0, l.indexOf(4));
		assertEquals(1, l.indexOf(5));
		assertEquals(4, l.indexOf(17));
		assertEquals(-1, l.indexOf(24));

		// contains()
		assertFalse(l.contains(3));
		assertTrue(l.contains(4));
		assertTrue(l.contains(5));
		assertFalse(l.contains(7));
		assertTrue(l.contains(17));
	}

	private void checkRemoveOneRange(int first, int last, String expected) {
		IndexedRangeList l = new IndexedRangeList();
		l.addValueRange(4, 6);
		l.addValueRange(11, 16);
		l.addValueRange(26, 28);
		l.addValueRange(32, 34);
		assertTrue(l.isValid());
		assertEquals("[(4,6),(11,16),(26,28),(32,34)]", l.toString());

		l.removeValueRange(first, last);
		assertTrue(l.isValid());
		assertEquals(expected, l.toString());
	}

	@Test
	public void testRemoveRange() {
		// 初始 [(4,6),(11,16),(26,28),(32,34)]
		checkRemoveOneRange(1, 2, "[(4,6),(11,16),(26,28),(32,34)]");
		checkRemoveOneRange(1, 4, "[(5,6),(11,16),(26,28),(32,34)]");
		checkRemoveOneRange(1, 6, "[(11,16),(26,28),(32,34)]");
		checkRemoveOneRange(5, 6, "[4,(11,16),(26,28),(32,34)]");
		checkRemoveOneRange(5, 5, "[4,6,(11,16),(26,28),(32,34)]");
		checkRemoveOneRange(5, 12, "[4,(13,16),(26,28),(32,34)]");
		checkRemoveOneRange(7, 8, "[(4,6),(11,16),(26,28),(32,34)]");
		checkRemoveOneRange(7, 16, "[(4,6),(26,28),(32,34)]");
		checkRemoveOneRange(7, 32, "[(4,6),(33,34)]");
		checkRemoveOneRange(11, 31, "[(4,6),(32,34)]");
		checkRemoveOneRange(12, 13, "[(4,6),11,(14,16),(26,28),(32,34)]");
		checkRemoveOneRange(13, 16, "[(4,6),(11,12),(26,28),(32,34)]");
	}

	@Test
	public void testIntersect() {
		IndexedRangeList x = new IndexedRangeList();
		x.addValueRange(1, 3);
		x.addValueRange(5, 10);
		x.addValueRange(13, 24); // [(1,3),(5,10),(13,24)]

		IndexedRangeList y = new IndexedRangeList();
		y.addValueRange(2, 13);
		y.addValueRange(15, 100); // [(2,13),(15,100)]

		IndexedRangeList rs = IndexedRangeList.intersectWith(x, y);
		assertTrue(rs.isValid());
		assertEquals("[(2,3),(5,10),13,(15,24)]", rs.toString());
	}

	@Test
	public void testMerge() {
		IndexedRangeList x = new IndexedRangeList();
		x.addValueRange(1, 3);
		x.addValueRange(5, 10);
		x.addValueRange(13, 24); // [(1,3),(5,10),(13,24)]

		IndexedRangeList y = new IndexedRangeList();
		y.addValueRange(2, 13);
		y.addValueRange(15, 100); // [(2,13),(15,100)]

		IndexedRangeList rs = IndexedRangeList.mergeWith(x, y);
		assertTrue(rs.isValid());
		assertEquals("[(1,100)]", rs.toString());
	}

	@Test
	public void testRemainder() {
		IndexedRangeList x = new IndexedRangeList();
		x.addValueRange(1, 3);
		x.addValueRange(5, 10);
		x.addValueRange(13, 24); // [(1,3),(5,10),(13,24)]

		IndexedRangeList y = new IndexedRangeList();
		y.addValueRange(2, 13);
		y.addValueRange(15, 100); // [(2,13),(15,100)]

		IndexedRangeList rs = IndexedRangeList.remainder(x, y);
		assertTrue(rs.isValid());
		assertEquals("[1,14]", rs.toString());
	}

	private void checkIter(Iterator<Integer> iter, int[] expected) {
		int i = 0;
		while (iter.hasNext()) {
			int v = iter.next();
			assertEquals(expected[i++], v);
		}
		assertEquals(expected.length, i);
	}

	@Test
	public void testIterator() {
		IndexedRangeList x = new IndexedRangeList();
		x.addValueRange(1, 3);
		x.addValueRange(5, 10);
		x.addValueRange(13, 15);

		Iterator<Integer> iter = x.iterator();
		checkIter(iter, new int[]{1,2,3, 5,6,7,8,9,10, 13,14,15});
	}

	@Test
	public void testRangeIterator() {
		IndexedRangeList x = new IndexedRangeList();
		x.addValueRange(1, 3);
		x.addValueRange(5, 10);
		x.addValueRange(13, 15);

		Iterator<Integer> iter = x.iterator(2, 12);
		checkIter(iter, new int[]{2,3, 5,6,7,8,9,10});
	}

	@Test
	public void testVacuumIter() {
		IndexedRangeList x = new IndexedRangeList();
		x.addValueRange(1, 3);
		x.addValueRange(5, 10);
		x.addValueRange(13, 15);

		Iterator<Integer> iter = x.vacuum_iterator(4, 14);
		checkIter(iter, new int[]{4,11,12});
	}
}

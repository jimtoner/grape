package grape.container.rangelist;

import static org.junit.Assert.*;
import grape.container.rangelist.IndexedRangeList;

import org.junit.Test;

public class IndexedRangeListTest {

	private void checkAddOneRange(int start, int count, String expected) {
		IndexedRangeList l = new IndexedRangeList();
		l.addRange(4, 3);
		l.addRange(12, 4);
		assertTrue(l.isValid());
		assertEquals("[(4,6),(12,15)]", l.toString());

		l.addRange(start, count);
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
		checkAddOneRange(5, 1, "[(4,6),(12,15)]");
		checkAddOneRange(5, 3, "[(4,7),(12,15)]");
		checkAddOneRange(5, 7, "[(4,15)]");
		checkAddOneRange(5, 8, "[(4,15)]");
		checkAddOneRange(5, 11, "[(4,15)]");
		checkAddOneRange(5, 12, "[(4,16)]");
		checkAddOneRange(4, 12, "[(4,15)]");

		// 在空隙添加
		checkAddOneRange(8, 3, "[(4,6),(8,10),(12,15)]");
		checkAddOneRange(8, 4, "[(4,6),(8,15)]");

		// 在尾部添加
		checkAddOneRange(13, 4, "[(4,6),(12,16)]");
		checkAddOneRange(13, 3, "[(4,6),(12,15)]");
		checkAddOneRange(16, 2, "[(4,6),(12,17)]");
		checkAddOneRange(17, 2, "[(4,6),(12,15),(17,18)]");
		checkAddOneRange(1, 20, "[(1,20)]");
	}

	@Test
	public void testSize() {
		IndexedRangeList l = new IndexedRangeList();
		l.addRange(1,2);
		l.addRange(12, 15); // [(1,2),(12,26)]
		assertTrue(l.isValid());
		assertEquals(1, l.getFirstValue());
		assertEquals(26, l.getLastValue());
		assertEquals(17, l.size());
	}

	@Test
	public void testGetAndIndexOf() {
		IndexedRangeList l = new IndexedRangeList();
		l.addRange(4,3);
		l.addRange(16, 8); // [(4,6),(16,23)]

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

	private void checkRemoveOneRange(int start, int count, String expected) {
		IndexedRangeList l = new IndexedRangeList();
		l.addRange(4, 3);
		l.addRange(11, 6);
		l.addRange(26, 3);
		l.addRange(32, 3);
		assertTrue(l.isValid());
		assertEquals("[(4,6),(11,16),(26,28),(32,34)]", l.toString());

		l.removeRange(start, count);
		assertTrue(l.isValid());
		assertEquals(expected, l.toString());
	}

	@Test
	public void testRemoveRange() {
		// 初始 [(4,6),(11,16),(26,28),(32,34)]
		checkRemoveOneRange(1, 2, "[(4,6),(11,16),(26,28),(32,34)]");
		checkRemoveOneRange(1, 4, "[(5,6),(11,16),(26,28),(32,34)]");
		checkRemoveOneRange(1, 6, "[(11,16),(26,28),(32,34)]");
		checkRemoveOneRange(5, 2, "[4,(11,16),(26,28),(32,34)]");
		checkRemoveOneRange(5, 1, "[4,6,(11,16),(26,28),(32,34)]");
		checkRemoveOneRange(5, 8, "[4,(13,16),(26,28),(32,34)]");
		checkRemoveOneRange(7, 2, "[(4,6),(11,16),(26,28),(32,34)]");
		checkRemoveOneRange(7, 10, "[(4,6),(26,28),(32,34)]");
		checkRemoveOneRange(7, 26, "[(4,6),(33,34)]");
		checkRemoveOneRange(11, 21, "[(4,6),(32,34)]");
		checkRemoveOneRange(12, 2, "[(4,6),11,(14,16),(26,28),(32,34)]");
		checkRemoveOneRange(13, 4, "[(4,6),(11,12),(26,28),(32,34)]");
	}

	@Test
	public void testIntersect() {
		IndexedRangeList x = new IndexedRangeList();
		x.addRange(1, 3);
		x.addRange(5, 6);
		x.addRange(13, 12); // [(1,3),(5,10),(13,24)]

		IndexedRangeList y = new IndexedRangeList();
		y.addRange(2, 12);
		y.addRange(15, 86); // [(2,13),(15,100)]

		IndexedRangeList rs = IndexedRangeList.intersectWith(x, y);
		assertTrue(rs.isValid());
		assertEquals("[(2,3),(5,10),13,(15,24)]", rs.toString());
	}

	@Test
	public void testMerge() {
		IndexedRangeList x = new IndexedRangeList();
		x.addRange(1, 3);
		x.addRange(5, 6);
		x.addRange(13, 12); // [(1,3),(5,10),(13,24)]

		IndexedRangeList y = new IndexedRangeList();
		y.addRange(2, 12);
		y.addRange(15, 86); // [(2,13),(15,100)]

		IndexedRangeList rs = IndexedRangeList.mergeWith(x, y);
		assertTrue(rs.isValid());
		assertEquals("[(1,100)]", rs.toString());
	}

	@Test
	public void testRemainder() {
		IndexedRangeList x = new IndexedRangeList();
		x.addRange(1, 3);
		x.addRange(5, 6);
		x.addRange(13, 12); // [(1,3),(5,10),(13,24)]

		IndexedRangeList y = new IndexedRangeList();
		y.addRange(2, 12);
		y.addRange(15, 86); // [(2,13),(15,100)]

		IndexedRangeList rs = IndexedRangeList.remainder(x, y);
		assertTrue(rs.isValid());
		assertEquals("[1,14]", rs.toString());
	}
}

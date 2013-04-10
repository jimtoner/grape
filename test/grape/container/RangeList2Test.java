package grape.container;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RangeList2Test {

	private void checkAddOneRange(int start, int count, String expected) {
		RangeList2 l = new RangeList2();
		l.addRange(4, 3);
		l.addRange(12, 4);
		assertEquals("[(4,6),(12,15)]", l.toString());

		l.addRange(start, count);
		assertTrue(l.isValid());
		assertEquals(expected, l.toString());
	}

	@Test
	public void testAddRange1() {
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
	}
}

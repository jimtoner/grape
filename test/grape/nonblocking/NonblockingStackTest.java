package grape.nonblocking;

import static org.junit.Assert.*;

import java.util.LinkedList;

import org.junit.Test;

public class NonblockingStackTest {

	@Test
	public void testSmoke() {
		NonblockingStack<Integer> q = new NonblockingStack<Integer>();
		assertEquals(0, q.size());
		assertTrue(q.isEmpty());

		q.push(1);
		q.push(2);
		assertEquals(2, q.size());
		assertFalse(q.isEmpty());

		assertEquals(Integer.valueOf(2), q.pop());
		assertEquals(1, q.size());

		assertEquals(Integer.valueOf(1), q.pop());
		assertEquals(0, q.size());
		assertEquals(null, q.pop());
	}

	// 对比性能测试
	@Test
	public void testSample() {
		LinkedList<Integer> q = new LinkedList<Integer>();
		assertEquals(0, q.size());
		assertTrue(q.isEmpty());

		q.push(1);
		q.push(2);
		assertEquals(2, q.size());
		assertFalse(q.isEmpty());

		assertEquals(Integer.valueOf(2), q.pop());
		assertEquals(1, q.size());

		assertEquals(Integer.valueOf(1), q.pop());
		assertEquals(0, q.size());
	}
}

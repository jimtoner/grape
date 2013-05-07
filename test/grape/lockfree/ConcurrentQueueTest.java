package grape.lockfree;

import static org.junit.Assert.*;
import grape.lockfree.ConcurrentQueue;

import org.junit.Test;

public class ConcurrentQueueTest {

	@Test
	public void testSmoke() {
		ConcurrentQueue<Integer> q = new ConcurrentQueue<Integer>();
		assertEquals(0, q.size());
		assertTrue(q.isEmpty());

		q.push(1);
		q.push(2);
		assertEquals(2, q.size());
		assertFalse(q.isEmpty());

		assertEquals(Integer.valueOf(1), q.pop());
		assertEquals(1, q.size());

		assertEquals(Integer.valueOf(2), q.pop());
		assertEquals(0, q.size());
		assertEquals(null, q.pop());
	}
}

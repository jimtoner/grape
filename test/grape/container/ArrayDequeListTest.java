package grape.container;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ArrayDequeListTest {

	@Test
	public void test() {
		DequeList<Integer> l = new ArrayDequeList<Integer>();
		l.add(1);
		l.add(2);
		assertEquals(1, (int)l.get(0));
		assertEquals(2, (int)l.get(1));

	}
}

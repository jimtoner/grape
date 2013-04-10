package grape.container;

import org.junit.Test;

public class RangeList2Test {

	@Test
	public void testSmoke() {
		RangeList2 rl = new RangeList2();
		rl.addRange(1, 3);
		rl.addRange(9, 15);
		rl.addRange(20, 20);
		rl.addRange(25, 29);
		System.out.println(rl);
	}

}

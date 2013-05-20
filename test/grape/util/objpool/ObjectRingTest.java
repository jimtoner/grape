package grape.util.objpool;

import org.junit.Test;

public class ObjectRingTest {

	static final ObjectRing<ObjectRingTest> pool = new ObjectRing<ObjectRingTest>(new PoolableObjectFactory<ObjectRingTest>() {

		@Override
		public ObjectRingTest newObject() {
			return new ObjectRingTest();
		}

		@Override
		public void passivateObject(ObjectRingTest obj) {
		}
	});

	static final ThreadLocal<ObjectRingTest> local = new ThreadLocal<ObjectRingTest>() {
		@Override
		protected ObjectRingTest initialValue() {
			return new ObjectRingTest();
		}
	};

	@Test
	public void test() {
		int count = 100000;
		long start = System.currentTimeMillis();
		for (int i = 0; i < count; ++i) {
			ObjectRingTest t = local.get();
		}
		long time1 = System.currentTimeMillis();
		for (int i = 0; i < count; ++i) {
			ObjectRingTest t = pool.borrowObject();
			t = pool.returnObject(t);
		}
		long end = System.currentTimeMillis();
		System.out.println("ThreadLocal: " + (time1 - start));
		System.out.println("ObjectPool: " + (end - time1));
	}

}

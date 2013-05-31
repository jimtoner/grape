package grape.lockfree;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 参考：
 * http://www.codeproject.com/Articles/153898/Yet-another-implementation-of-a-lock-free-circular
 *
 * @author jingqi
 */
public class ConcurrentArrayQueue <T> {

	// 环状缓存
	private final Object[] ring;
	private final AtomicInteger maximumReadIndex = new AtomicInteger(0);
	private final AtomicInteger readIndex = new AtomicInteger(0);
	private final AtomicInteger writeIndex = new AtomicInteger(0);

	public ConcurrentArrayQueue(int capacity) {
		if (capacity < 0)
			throw new IllegalArgumentException("Illegal capacity " + capacity);
		ring = new Object[capacity + 1];
	}

	public boolean push(T e) {
		int currentReadIndex, currentWriteIndex;

		do {
			currentReadIndex = readIndex.get();
			currentWriteIndex = writeIndex.get();

			// check if queue is full
			if (((currentWriteIndex + 1) % ring.length) ==
					(currentReadIndex % ring.length))
				return false;
		} while (!writeIndex.compareAndSet(currentWriteIndex, currentWriteIndex + 1));

		// We know now that this index is reserved for us. Use it to save the data
		ring[currentWriteIndex % ring.length] = e;

		// update the maximum read index after saving the data.
		// It might fail if there are more than 1 producer threads because this
        // operation has to be done in the same order as the previous CAS
		while (!maximumReadIndex.compareAndSet(currentWriteIndex, currentWriteIndex + 1)) {
            // this is a good place to yield the thread in case there are more
            // software threads than hardware processors and you have more
            // than 1 producer thread
            // have a look at sched_yield() (POSIX.1b)
			Thread.yield();
		}

		return true;
	}

	public T pop() {
		int currentMaximumReadIndex;
		int currentReadIndex;

		while (true) {
			currentReadIndex = readIndex.get();
			currentMaximumReadIndex = maximumReadIndex.get();

			// The queue is empty or a producer thread has allocate space in the queue
			// but is waiting to commit the data into it
			if ((currentReadIndex % ring.length) ==
					(currentMaximumReadIndex % ring.length))
				return null;

			// retrieve the data from the queue
			@SuppressWarnings("unchecked")
			T ret = (T) ring[currentReadIndex % ring.length];

			if (readIndex.compareAndSet(currentReadIndex, currentReadIndex + 1))
				return ret; // 这里没有办法清理残余的引用，可能导致内存泄露
		}
	}

	public int size() {
		int ret = writeIndex.get() - readIndex.get();
		if (ret < 0)
			return 0;
		return ret;
	}

	public void clear() {
		while (size() > 0)
			pop();
	}

	public int capactity() {
		return ring.length - 1;
	}
}

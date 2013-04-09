package grape.container.deqlist;


import java.util.*;

/**
 * Base for DequeList
 *
 * @author jingqi
 *
 * @param <E>
 */
public abstract class AbstractDequeList<E> extends AbstractList<E>
	implements DequeList<E> {

	protected AbstractDequeList() {}

	@Override
	public E getFirst() {
		if (isEmpty())
			throw new NoSuchElementException();
		return get(0);
	}

	@Override
	public E getLast() {
		if (isEmpty())
			throw new NoSuchElementException();
		return get(size() - 1);
	}

	@Override
	public E element() {
		return getFirst();
	}

	@Override
	public E peekFirst() {
		if (isEmpty())
			return null;
		return get(0);
	}

	@Override
	public E peekLast() {
		if (isEmpty())
			return null;
		return get(size() - 1);
	}

	@Override
	public E peek() {
		return peekFirst();
	}

	@Override
	public void addFirst(E e) {
		add(0, e);
	}

	@Override
	public void addLast(E e) {
		add(size(), e);
	}

	@Override
	public boolean add(E e) {
		add(size(), e);
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		return addAll(size(), c);
	}

	@Override
	public boolean offerFirst(E e) {
		add(0, e);
		return true;
	}

	@Override
	public boolean offerLast(E e) {
		add(size(), e);
		return true;
	}

	@Override
	public boolean offer(E e) {
		add(size(), e);
		return true;
	}

	@Override
	public E removeFirst() {
		if (isEmpty())
			throw new NoSuchElementException();
		return remove(0);
	}

	@Override
	public E removeLast() {
		if (isEmpty())
			throw new NoSuchElementException();
		return remove(size() - 1);
	}

	@Override
	public E remove() {
		return removeFirst();
	}

	@Override
	public boolean removeFirstOccurrence(Object o) {
		int i = indexOf(o);
		if (i < 0)
			return false;
		remove(i);
		return true;
	}

	@Override
	public boolean removeLastOccurrence(Object o) {
		int i = lastIndexOf(0);
		if (i < 0)
			return false;
		remove(i);
		return true;
	}

	@Override
	public E pollFirst() {
		if (isEmpty())
			return null;
		return removeFirst();
	}

	@Override
	public E pollLast() {
		if (isEmpty())
			return null;
		return removeLast();
	}

	@Override
	public E poll() {
		return pollFirst();
	}

	@Override
	public void clear() {
		while (isEmpty())
			poll();
	}

	@Override
	public void push(E e) {
		addFirst(e);
	}

	@Override
	public E pop() {
		return removeFirst();
	}

	@Override
	public Iterator<E> descendingIterator() {
		return new Iterator<E>() {

			int expectedModCount = AbstractDequeList.this.modCount;
			int cursor = AbstractDequeList.this.size() - 1;

			@Override
			public boolean hasNext() {
				return cursor >= 0;
			}

			@Override
			public E next() {
				checkForComodification();
				if (!hasNext())
					throw new NoSuchElementException();
				return AbstractDequeList.this.get(cursor--);
			}

			@Override
			public void remove() {
				checkForComodification();
				if (cursor + 1 >= AbstractDequeList.this.size())
					throw new IllegalStateException();
				AbstractDequeList.this.remove(cursor + 1);
				expectedModCount = AbstractDequeList.this.modCount;
			}

	        private void checkForComodification() {
	            if (AbstractDequeList.this.modCount != expectedModCount)
	                throw new ConcurrentModificationException();
	        }
		};
	}
}

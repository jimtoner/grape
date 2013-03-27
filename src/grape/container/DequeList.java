package grape.container;

import java.util.Deque;
import java.util.List;

/**
 * interface for both {@link Deque} and {@link List}
 *
 * @author jingqi
 *
 * @param <E>
 */
public interface DequeList<E> extends Deque<E>, List<E> {
	// nothing to do
}

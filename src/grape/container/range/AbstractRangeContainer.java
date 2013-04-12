package grape.container.range;

import java.util.Iterator;


/**
 * RangeContainer 基类
 *
 * @author jingqi
 */
public abstract class AbstractRangeContainer implements RangeContainer {

	@Override
	public boolean contains(RangeContainer x) {
		return x.remainder(this).isEmpty();
	}

	@Override
	public void addValue(int value) {
		addValueRange(value, value);
	}

	@Override
	public void addValueRange(Range r) {
		addValueRange(r.getFirstValue(), r.getLastValue());
	}

	@Override
	public void removeValue(int value) {
		removeValueRange(value, value);
	}

	@Override
	public void removeValueRange(Range r) {
		removeValueRange(r.getFirstValue(), r.getLastValue());
	}

	/**
	 * 容器做交集， out = x ∩ y
	 */
	public static void intersectWith(RangeContainer x, RangeContainer y, RangeContainer out) {
		final Iterator<Range> iter1 = x.rangeIterator(), iter2 = y.rangeIterator();
		Range r1 = null, r2 = null;
		while ((r1 != null || iter1.hasNext()) &&
				(r2 != null || iter2.hasNext())) {
			if (r1 == null)
				r1 = iter1.next();
			if (r2 == null)
				r2 = iter2.next();

			Range i = r1.intersectWith(r2);
			if (i != null)
				out.addValueRange(i);
			if (r1.getLastValue() > r2.getLastValue())
				r2 = null;
			else
				r1 = null;
		}
	}

	/**
	 * 容器做并集，out = x ∪ y
	 */
	protected static void mergeWith(RangeContainer x, RangeContainer y, RangeContainer out) {
		final Iterator<Range> iter1 = x.rangeIterator(), iter2 = y.rangeIterator();
		Range m = null, r1 = null, r2 = null;
		while (r1 != null || iter1.hasNext() ||
				r2 != null || iter2.hasNext()) {
			if (r1 == null && iter1.hasNext())
				r1 = iter1.next();
			if (r2 == null && iter2.hasNext())
				r2 = iter2.next();

			if (m == null) {
				if (r1 == null) {
					m = r2;
					r2 = null;
					continue;
				} else if (r2 == null) {
					m = r1;
					r1 = null;
					continue;
				} else if (r1.getFirstValue() < r2.getFirstValue()) {
					m = r1;
					r1 = null;
					continue;
				} else {
					m = r2;
					r2 = null;
					continue;
				}
			} else {
				if (r1 == null) {
					Range i = m.mergeWith(r2);
					if (i == null) {
						out.addValueRange(m);
						m = null;
						continue;
					} else {
						m = i;
						r2 = null;
						continue;
					}
				} else if (r2 == null) {
					Range i = m.mergeWith(r1);
					if (i == null) {
						out.addValueRange(m);
						m = null;
						continue;
					} else {
						m = i;
						r1 = null;
						continue;
					}
				} else if (r1.getFirstValue() < r2.getFirstValue()) {
					Range i = m.mergeWith(r1);
					if (i == null) {
						out.addValueRange(m);
						m = null;
						continue;
					} else {
						m = i;
						r1 = null;
						continue;
					}
				} else {
					Range i = m.mergeWith(r2);
					if (i == null) {
						out.addValueRange(m);
						m = null;
						continue;
					} else {
						m = i;
						r2 = null;
						continue;
					}
				}
			}
		}
		if (m != null)
			out.addValueRange(m);
	}

	/**
	 * 容器做补集，out = x - y
	 */
	protected static void remainder(RangeContainer x, RangeContainer y, RangeContainer out) {
		final Iterator<Range> iter1 = x.rangeIterator(), iter2 = y.rangeIterator();
		Range r1 = null, r2 = null;
		while (r1 != null || iter1.hasNext()) {
			if (r1 == null)
				r1 = iter1.next();
			if (r2 == null && iter2.hasNext())
				r2 = iter2.next();

			if (r2 == null) {
				out.addValueRange(r1);
				r1 = null;
				continue;
			} else if (r1.getLastValue() < r2.getFirstValue()) {
				out.addValueRange(r1);
				r1 = null;
				continue;
			} else if (r1.getFirstValue() > r2.getLastValue()) {
				r2 = null;
				continue;
			} else if (r1.getLastValue() <= r2.getLastValue()) {
				if (r1.getFirstValue() < r2.getFirstValue()) {
					out.addValueRange(r1.getFirstValue(), r2.getFirstValue() - 1);
				}
				r1 = null;
				continue;
			} else {
				if (r1.getFirstValue() <= r2.getFirstValue()) {
					out.addValueRange(r1.getFirstValue(), r2.getFirstValue() - 1);
				}
				r1 = new Range(r2.getLastValue() + 1, r1.getLastValue());
			}
		}
	}

	@Override
	public Iterator<Integer> iterator(Range r) {
		return iterator(r.getFirstValue(), r.getLastValue());
	}

	@Override
	public Iterator<Integer> vacuumIterator(Range r) {
		return vacuumIterator(r.getFirstValue(), r.getLastValue());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof AbstractRangeContainer))
			return false;
		Iterator<Range> iter1 = rangeIterator();
		Iterator<Range> iter2 = ((AbstractRangeContainer) o).rangeIterator();
		while (iter1.hasNext()) {
			Range r1 = iter1.next();
			if (!iter2.hasNext())
				return false;
			Range r2 = iter2.next();
			if (!r1.equals(r2))
				return false;
		}
		if (iter2.hasNext())
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 17;
		Iterator<Range> iter = rangeIterator();
		while (iter.hasNext()) {
			Range r = iter.next();
			hash = hash * 31 + r.hashCode();
		}
		return hash;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[");
		Iterator<Range> iter = rangeIterator();
		boolean first = true;
		while (iter.hasNext()) {
			Range r = iter.next();
			if (!first)
				sb.append(",");
			first = false;
			sb.append(r.toString());
		}
		sb.append("]");
		return sb.toString();
	}
}

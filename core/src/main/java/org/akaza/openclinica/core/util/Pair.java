package org.akaza.openclinica.core.util;

/**
 * Immutable pair of objects. 
 *
 * @author Leonel Gayard, &lt;lgayard@openclinica.com&gt; 
 */
public class Pair<T,U> {
	private T first;
	private U second;
	
	public Pair(T t, U u) { 
		this.first  = t;
		this.second = u;
	}

	public T getFirst() {
		return first;
	}

	public U getSecond() {
		return second;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (!(obj instanceof Pair)) return false;
		Pair<?,?> that = (Pair<?,?>) obj;

		return (first == null && that.first == null
			||  first.equals(that.first))
			&& (second == null && that.second == null
			|| second.equals(that.second));
	}

	@Override
	public int hashCode() {
		int hash = 0;
		if (first != null) {
			hash |= (first.hashCode() << 16);
		}
		if (second != null) {
			hash |= second.hashCode();
		}
		return hash;
	}
}

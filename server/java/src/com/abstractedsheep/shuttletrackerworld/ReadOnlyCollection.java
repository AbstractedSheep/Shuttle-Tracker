package com.abstractedsheep.shuttletrackerworld;

import java.util.Collection;
import java.util.Iterator;

public class ReadOnlyCollection<T> implements Collection<T>{
	private Collection<T> source;
	
	public ReadOnlyCollection(Collection<T> source) {
		this.source = source;
	}
	
	public boolean add(T object) {
		throw new UnsupportedOperationException();
	}
	
	public boolean addAll(Collection<? extends T> collection) {
		throw new UnsupportedOperationException();
	}
	
	public boolean remove(Object object) {
		throw new UnsupportedOperationException();
	}
	
	public boolean removeAll(Collection<?> collection) {
		throw new UnsupportedOperationException();
	}
	
	public boolean retainAll(Collection<?> collection) {
		throw new UnsupportedOperationException();
	}
	
	public void clear() {
		throw new UnsupportedOperationException();
	}
	
	public Iterator<T> iterator() {
		return source.iterator();
	}

	public int size() {
		return source.size();
	}

	public boolean contains(Object object) {
		return source.contains(object);
	}

	public boolean containsAll(Collection<?> arg0) {
		return source.containsAll(arg0);
	}

	public boolean isEmpty() {
		return source.isEmpty();
	}

	public Object[] toArray() {
		return source.toArray();
	}

	@SuppressWarnings("hiding")
	public <T> T[] toArray(T[] array) {
		return source.toArray(array);
	}

}

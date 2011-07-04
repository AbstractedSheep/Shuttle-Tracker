package com.abstractedsheep.shuttletrackerworld;

import java.util.Collection;
import java.util.Map;

public class ReadOnlyMap<T, K> implements Map<T, K>{

	private Map<T, K> source;
	
	public ReadOnlyMap(Map<T, K> source) {
		this.source = source;
	}

	public void clear() {
		throw new UnsupportedOperationException();
	}

	public boolean containsKey(Object key) {
		return source.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return source.containsValue(value);
	}

	public ReadOnlySet<java.util.Map.Entry<T, K>> entrySet() {
		return new ReadOnlySet<Entry<T, K>>(source.entrySet());
	}

	public K get(Object key) {
		return source.get(key);
	}

	public boolean isEmpty() {
		return source.isEmpty();
	}

	public ReadOnlySet<T> keySet() {
		return new ReadOnlySet<T>(source.keySet());
	}

	public K put(T key, K value) {
		throw new UnsupportedOperationException();
	}

	public void putAll(Map<? extends T, ? extends K> arg0) {
		throw new UnsupportedOperationException();
	}

	public K remove(Object key) {
		throw new UnsupportedOperationException();
	}

	public int size() {
		return source.size();
	}

	public Collection<K> values() {
		return new ReadOnlyCollection<K>(source.values());
	}


}

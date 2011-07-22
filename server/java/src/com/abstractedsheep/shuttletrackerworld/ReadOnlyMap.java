/*
 * Copyright 2011
 *
 *   This file is part of Mobile Shuttle Tracker.
 *
 *   Mobile Shuttle Tracker is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Mobile Shuttle Tracker is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Mobile Shuttle Tracker.  If not, see <http://www.gnu.org/licenses/>.
 */

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

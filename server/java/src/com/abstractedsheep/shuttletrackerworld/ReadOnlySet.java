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
import java.util.Iterator;
import java.util.Set;

public class ReadOnlySet<T> implements Set<T>{

	private Set<T> source;
	
	public ReadOnlySet(Set<T> source) {
		this.source = source;
	}
	
	public boolean add(T object) {
		throw new UnsupportedOperationException();
	}

	public boolean addAll(Collection<? extends T> arg0) {
		throw new UnsupportedOperationException();
	}

	public void clear() {
		throw new UnsupportedOperationException();
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

	public Iterator<T> iterator() {
		return source.iterator();
	}

	public boolean remove(Object object) {
		throw new UnsupportedOperationException();
	}

	public boolean removeAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	}

	public boolean retainAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	}

	public int size() {
		return source.size();
	}

	public Object[] toArray() {
		return source.toArray();
	}

	@SuppressWarnings("hiding")
	public <T> T[] toArray(T[] array) {
		return source.toArray(array);
	}

}

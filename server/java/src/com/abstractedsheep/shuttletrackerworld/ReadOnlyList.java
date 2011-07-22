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
import java.util.List;
import java.util.ListIterator;

public class ReadOnlyList<T> implements List<T> {
	private List<T> source;

	public ReadOnlyList(List<T> source) {
		this.source = source;
	}

	public boolean add(T object) {
		throw new UnsupportedOperationException();
	}

	public void add(int location, T object) {
		throw new UnsupportedOperationException();
	}

	public boolean addAll(Collection<? extends T> arg0) {
		throw new UnsupportedOperationException();
	}

	public boolean addAll(int arg0, Collection<? extends T> arg1) {
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

	public T get(int location) {
		return source.get(location);
	}

	public int indexOf(Object object) {
		return source.indexOf(object);
	}

	public boolean isEmpty() {
		return source.isEmpty();
	}

	public Iterator<T> iterator() {
		return source.iterator();
	}

	public int lastIndexOf(Object object) {
		return source.lastIndexOf(object);
	}

	public ListIterator<T> listIterator() {
		return source.listIterator();
	}

	public ListIterator<T> listIterator(int location) {
		return source.listIterator(location);
	}

	public T remove(int location) {
		throw new UnsupportedOperationException();
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

	public T set(int location, T object) {
		throw new UnsupportedOperationException();
	}

	public int size() {
		return source.size();
	}

	public List<T> subList(int start, int end) {
		return source.subList(start, end);
	}

	public Object[] toArray() {
		return source.toArray();
	}

	@SuppressWarnings("hiding")
	public <T> T[] toArray(T[] array) {
		return source.toArray(array);
	}
}

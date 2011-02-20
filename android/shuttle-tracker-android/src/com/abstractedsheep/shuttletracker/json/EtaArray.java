package com.abstractedsheep.shuttletracker.json;

import java.util.ArrayList;
import java.util.Collection;

public class EtaArray extends ArrayList<EtaJson>{
	private static final long serialVersionUID = -9141730833214472843L;

	public EtaArray() {
	}

	public EtaArray(int capacity) {
		super(capacity);
	}

	public EtaArray(Collection<? extends EtaJson> collection) {
		super(collection);
	}
}

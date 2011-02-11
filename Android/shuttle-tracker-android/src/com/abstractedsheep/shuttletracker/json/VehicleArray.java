package com.abstractedsheep.shuttletracker.json;

import java.util.ArrayList;
import java.util.Collection;

public class VehicleArray extends ArrayList<VehicleJson> {

	public VehicleArray() {
	}

	public VehicleArray(int capacity) {
		super(capacity);
	}

	public VehicleArray(Collection<? extends VehicleJson> collection) {
		super(collection);
	}

}

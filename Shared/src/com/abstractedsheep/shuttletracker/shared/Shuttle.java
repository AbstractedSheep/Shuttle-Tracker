package com.abstractedsheep.shuttletracker.shared;

public class Shuttle {
	private int shuttleId;
	private int routeId;
	
	public Shuttle(int shuttleId, int routeId) {
		this.shuttleId = shuttleId;
	}
	
	public int getShuttleId() {
		return this.shuttleId;
	}
	
	public void setShuttleId(int shuttleId) {
		this.shuttleId = shuttleId;
	}
	
	public void setRouteId(int routeId) {
		this.routeId = routeId;
	}
	
	public int getRouteId() {
		return routeId;
	}
}

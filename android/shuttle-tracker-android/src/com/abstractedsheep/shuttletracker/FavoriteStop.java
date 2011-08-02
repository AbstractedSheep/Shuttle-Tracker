package com.abstractedsheep.shuttletracker;

public class FavoriteStop {
	public final String stopId;
	public final int routeId;
	
	public FavoriteStop(int routeId, String stopId) {
		this.routeId = routeId;
		this.stopId = stopId;
	}
	
	public String getUniqueId() {
		return stopId + String.valueOf(routeId);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null)
            return false;

        try {
        	FavoriteStop s = (FavoriteStop) o;
        	return this.stopId.equals(s.stopId) && this.routeId == s.routeId;
        } catch (ClassCastException e) {
        	return false;
        }    
	}
	
	@Override
	public int hashCode() {
		return stopId.hashCode() ^ routeId;
	}
}

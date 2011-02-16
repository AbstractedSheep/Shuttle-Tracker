package com.abstractedsheep.shuttletracker.json;

public class EtaJson {
	private String stop_id;
	private String stop_name;
	private int shuttle_id;
	private String shuttle_name;
	private String eta;
	public String getStop_id() {
		return stop_id;
	}
	public void setStop_id(String stop_id) {
		this.stop_id = stop_id;
	}
	public String getStop_name() {
		return stop_name;
	}
	public void setStop_name(String stop_name) {
		this.stop_name = stop_name;
	}
	public int getShuttle_id() {
		return shuttle_id;
	}
	public void setShuttle_id(int shuttle_id) {
		this.shuttle_id = shuttle_id;
	}
	public String getShuttle_name() {
		return shuttle_name;
	}
	public void setShuttle_name(String shuttle_name) {
		this.shuttle_name = shuttle_name;
	}
	public String getEta() {
		return eta;
	}
	public void setEta(String eta) {
		this.eta = eta;
	}
}
	

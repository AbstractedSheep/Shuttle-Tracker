package com.abstractedsheep.shuttletracker.json;

import java.util.ArrayList;

public class ExtraEtaJson {
	private String name;
	private ArrayList<Integer> eta;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ArrayList<Integer> getEta() {
		return eta;
	}
	public void setEta(ArrayList<Integer> eta) {
		this.eta = eta;
	}
}

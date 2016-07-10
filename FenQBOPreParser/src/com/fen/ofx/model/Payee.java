package com.fen.ofx.model;

public class Payee {
	private String	name;

	public Payee() {

	}

	public Payee(String n) {
		setName(n);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Payee: " + getName());
		return sb.toString();
	}

}
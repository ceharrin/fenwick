package com.fen.ofx.model;

public class Check {
	private String	number	= null;
	private String	name	= null;

	public Check() {
	}

	public Check(String nbr, String n) {
		number = nbr;
		name = n;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Check: " + getNumber() + " paid to: " + getName());
		return sb.toString();
	}

	public String asXML() {
		StringBuffer sb = new StringBuffer();
		String newLine = System.getProperty("line.separator");
		sb.append("<check>" + newLine);
		sb.append("<number>" + getNumber() + "</number>"
				+ System.getProperty("line.separator"));
		sb.append("<paidTo>" + getName() + "</paidTo>"
				+ System.getProperty("line.separator"));
		sb.append("</check>" + newLine);
		return sb.toString();
	}
}

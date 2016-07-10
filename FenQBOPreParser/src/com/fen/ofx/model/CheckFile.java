package com.fen.ofx.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CheckFile {
	// Map<String, String> checks = null;
	List<Check>	checks	= new ArrayList<Check>();

	public CheckFile(String checksFile) {
		File f = new File(checksFile);
		// checks = new HashMap<String, String>();
		try {
			BufferedReader input = new BufferedReader(new FileReader(f));
			try {
				String line = null;
				while ((line = input.readLine()) != null) {
					String[] t = line.split("\t");
					// checks.put(t[0], t[1]);
					Check c = new Check(t[0], t[1]);
					addCheck(c);
					System.out.println("Added check number: " + t[0]
							+ " paid to: " + t[1]);
				}
			} finally {
				input.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public CheckFile(NodeList checksList) {
		for (int i = 0; i < checksList.getLength(); i++) {
			Node nNode = checksList.item(i);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				String num = eElement.getElementsByTagName("number").item(0)
						.getTextContent();
				String pay = eElement.getElementsByTagName("paidTo").item(0)
						.getTextContent();
				addCheck(new Check(num, pay));
			}
		}
	}

	public String getPayee(String checkNum) {
		for (Iterator<Check> iterator = checks.iterator(); iterator.hasNext();) {
			Check c = (Check) iterator.next();
			if (c.getNumber().equalsIgnoreCase(checkNum)) return c.getName();
		}
		return null;
	}

	public void addCheck(Check c) {
		checks.add(c);
	}

	public List<Check> getChecks() {
		return checks;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		String newLine = System.getProperty("line.seperator");
		sb.append("There are " + checks.size() + newLine);
		for (Iterator<Check> iterator = checks.iterator(); iterator.hasNext();) {
			Check c = (Check) iterator.next();
			sb.append(c + newLine);
		}
		return sb.toString();
	}

	public String asXML() {
		StringBuffer sb = new StringBuffer();
		String newLine = System.getProperty("line.separator");
		sb.append("<checks>" + newLine);
		for (Iterator<Check> iterator = checks.iterator(); iterator.hasNext();) {
			Check c = (Check) iterator.next();
			sb.append(c.asXML());
		}
		sb.append("</checks>" + newLine);
		return sb.toString();
	}
}

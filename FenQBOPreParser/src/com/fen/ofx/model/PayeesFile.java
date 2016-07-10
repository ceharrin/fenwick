package com.fen.ofx.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PayeesFile {
	List<String>	payees	= new ArrayList<String>();

	public PayeesFile(String payeesFile) {
		File f = new File(payeesFile);
		try {
			BufferedReader input = new BufferedReader(new FileReader(f));
			try {
				String line = null;
				while ((line = input.readLine()) != null) {
					payees.add(line);
					System.out.println("Added payee: " + line);
				}
			} finally {
				input.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public PayeesFile(NodeList payeesList) {
		for (int i = 0; i < payeesList.getLength(); i++) {
			String pay = payeesList.item(i).getTextContent();
			payees.add(pay);
		}
	}

	public List<String> getPayees() {
		return payees;
	}

	public String getPayee(String payeeString) {
		for (String payee : payees) {
			if (payeeString.contains(payee)) return payee;
		}
		if ( payeeString.equals("ACCOUNT ANLYSIS CHARGE"))
			return "Lead Bank";
		// Not found - just return the string
		return payeeString;
	}

	public String asXML() {
		StringBuffer sb = new StringBuffer();
		String newLine = System.getProperty("line.separator");
		sb.append("<payees>" + newLine);
		for (Iterator<String> iterator = payees.iterator(); iterator.hasNext();) {
			String type = (String) iterator.next();
			String escapedPayee = StringEscapeUtils.escapeXml(type);
			sb.append("<payee>" + escapedPayee + "</payee>" + newLine);
		}
		sb.append("</payees>" + newLine);
		return sb.toString();
	}
}

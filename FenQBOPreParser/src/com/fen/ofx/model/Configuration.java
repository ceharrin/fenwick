package com.fen.ofx.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Configuration {
	Map<String, String>	keyVals		= null;
	private int			version;
	private boolean		insertPrivateTag;
	private String		outputFile	= null;

	public Configuration() {
	}

	public Configuration(String configFile) {
		keyVals = new HashMap<String, String>();
		try {
			BufferedReader input = new BufferedReader(
					new FileReader(configFile));
			try {
				String line = null;
				while ((line = input.readLine()) != null) {
					String[] t = line.split("\t");
					keyVals.put(t[0], t[1]);
					System.out.println("Added config key: " + t[0] + " value: "
							+ t[1]);
				}
			} finally {
				input.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public Configuration(NodeList configList) {
		for (int i = 0; i < configList.getLength(); i++) {
			Node nNode = configList.item(i);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				String s = eElement.getElementsByTagName("ofxVersion").item(0)
						.getTextContent();
				setVersion(Integer.valueOf(s));
				String p = eElement.getElementsByTagName("privateTag").item(0)
						.getTextContent();
				if (p.equalsIgnoreCase("y"))
					setInsertPrivateTag(true);
				else
					setInsertPrivateTag(false);
				String f = eElement.getElementsByTagName("outputFilename").item(0)
						.getTextContent();
				setOutputFile(f);
			}
		}
	}

	public String getConfigValue(String key) {
		Set<String> keys = keyVals.keySet();
		for (Iterator<String> iterator = keys.iterator(); iterator.hasNext();) {
			String cn = (String) iterator.next();
			if (cn.equalsIgnoreCase(key)) return keyVals.get(key);
		}
		return null;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	public boolean isInsertPrivateTag() {
		return insertPrivateTag;
	}

	public void setInsertPrivateTag(boolean insertPrivateTag) {
		this.insertPrivateTag = insertPrivateTag;
	}

	public String asXML() {
		StringBuffer sb = new StringBuffer();
		String newLine = System.getProperty("line.separator");
		sb.append("<configuration>" + newLine);
		sb.append("<ofxVersion>" + version + "</ofxVersion>" + newLine);
		sb.append("<privateTag>" + insertPrivateTag + "</privateTag>" + newLine);
		sb.append("<outputFilename>" + outputFile + "</outputFilename>"
				+ newLine);
		sb.append("</configuration>" + newLine);
		return sb.toString();
	}
}

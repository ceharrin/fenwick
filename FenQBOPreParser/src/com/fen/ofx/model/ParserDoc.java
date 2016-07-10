package com.fen.ofx.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ParserDoc {
	private Configuration	config		= null;
	private CheckFile		checkFile	= null;
	private PayeesFile		payeeFile	= null;
	private String			qboFile		= null;
	private String			outputFile	= null;
	private TransactionMemo	memo		= null;

	public ParserDoc() {
	}

	public Configuration getConfig() {
		return config;
	}

	public void setConfig(Configuration config) {
		this.config = config;
	}

	public CheckFile getCheckFile() {
		return checkFile;
	}

	public void setCheckFile(CheckFile checkFile) {
		this.checkFile = checkFile;
	}

	public PayeesFile getPayeeFile() {
		return payeeFile;
	}

	public void setPayeeFile(PayeesFile payeeFile) {
		this.payeeFile = payeeFile;
	}

	public String getInputFile() {
		return qboFile;
	}

	public void setInputFile(String qboFile) {
		this.qboFile = qboFile;
	}

	public String getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	public String getOutputFilePathAndName() {
		// String outputFileName = null;
		String outputFilePath = null;
		int i = qboFile.lastIndexOf("/");
		// outputFileName = qboFile.substring(i + 1);
		outputFilePath = qboFile.substring(0, i);
		// int j = outputFileName.lastIndexOf('.');
		// String fName = outputFileName.substring(0, j);
		// String ext = outputFileName.substring(j + 1);
		// outputFileName = fName + "_x." + ext;
		return outputFilePath + "/" + config.getOutputFile() + ".qbo";
	}

	public TransactionMemo getMemo() {
		return memo;
	}

	public void setMemo(TransactionMemo memo) {
		this.memo = memo;
	}

	public boolean saveTo(String fileName) throws IOException {
		boolean saved = false;
		FileOutputStream fos = new FileOutputStream(fileName);
		fos.write(asXML().getBytes());
		fos.close();
		saved = true;
		return saved;
	}

	public String asXML() {
		StringBuffer sb = new StringBuffer();
		String newLine = System.getProperty("line.separator");
		sb.append("<preProcessingInstructions>" + newLine);
		String escapedFileName = StringEscapeUtils.escapeXml(getInputFile());
		sb.append("<qboFile>" + escapedFileName + "</qboFile>" + newLine);
		sb.append("<lastModified>" + Calendar.getInstance().getTime()
				+ "</lastModified>" + newLine);
		sb.append(config.asXML());
		sb.append(checkFile.asXML());
		sb.append(payeeFile.asXML());
		sb.append("</preProcessingInstructions>" + newLine);
		return sb.toString();
	}

	public ParserDoc(String preFile) throws ParserConfigurationException,
			SAXException, IOException {
		File f = new File(preFile);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(f);
		doc.getDocumentElement().normalize();
		NodeList nList = doc.getElementsByTagName("qboFile");
		setInputFile(nList.item(0).getTextContent());
		NodeList configList = doc.getElementsByTagName("config");
		setConfig(new Configuration(configList));
		NodeList checksList = doc.getElementsByTagName("check");
		setCheckFile(new CheckFile(checksList));
		NodeList payeesList = doc.getElementsByTagName("payee");
		setPayeeFile(new PayeesFile(payeesList));
	}
}

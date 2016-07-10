package com.fen.ofx;

/**
 * This code pre-processes a QBO file down loaded from your financial institution.  One of the issues we ran across 
 * when importing a qbo file  is that the name tag only contained partial names or contained dates in the name.
 * This caused QuickBooks to require we create an alias for just about every transaction thus adding to the time
 * needed to import a file.
 * 
 * The code takes the value from the memo tag and parses it so that a name matching one in the payees file
 * is used; or not finding one will use the parsed value out of the memo tag.
 * 
 * You can create a checks file with check numbers and payees; it makes importing check transactions into QB 
 * much simpler.
 * 
 * Finally a couple of config values are supported and are stored in key/value pairs in the config file.
 *
 * This class is for use in the GUI implementation; it does the same thing that the CommandLineParser does.
 * 
 * @Author Chris Harrington
 * 
 */
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

import com.fen.ofx.model.CheckFile;
import com.fen.ofx.model.Configuration;
import com.fen.ofx.model.ParserDoc;
import com.fen.ofx.model.PayeesFile;
import com.fen.ofx.model.TransactionMemo;

import net.sf.ofx4j.domain.data.MessageSetType;
import net.sf.ofx4j.domain.data.ResponseEnvelope;
import net.sf.ofx4j.domain.data.banking.BankStatementResponse;
import net.sf.ofx4j.domain.data.banking.BankStatementResponseTransaction;
import net.sf.ofx4j.domain.data.banking.BankingResponseMessageSet;
import net.sf.ofx4j.domain.data.common.Transaction;
import net.sf.ofx4j.domain.data.common.TransactionList;
import net.sf.ofx4j.domain.data.common.TransactionType;
import net.sf.ofx4j.io.AggregateMarshaller;
import net.sf.ofx4j.io.AggregateUnmarshaller;
import net.sf.ofx4j.io.OFXParseException;
import net.sf.ofx4j.io.v1.OFXV1Writer;
import net.sf.ofx4j.io.v2.OFXV2Writer;

public class Parser {
	private int				ofxOutputVersion;
	private CheckFile		c			= null;
	private PayeesFile		p			= null;
	private TransactionMemo	tt			= null;
	private Configuration	config		= null;
	private ParserDoc		pd			= null;
	private StringBuffer	logBuffer	= new StringBuffer(1500);

	public Parser(ParserDoc parserDoc) {
		pd = parserDoc;
		c = pd.getCheckFile();
		p = pd.getPayeeFile();
		tt = pd.getMemo();
		config = pd.getConfig();
	}

	public void parse() {
		AggregateUnmarshaller<ResponseEnvelope> unmarshaller = new AggregateUnmarshaller<ResponseEnvelope>(
				ResponseEnvelope.class);
		FileInputStream inputFile = null;
		FileOutputStream outputFile = null;
		try {
			inputFile = new FileInputStream(pd.getInputFile());
			outputFile = new FileOutputStream(pd.getOutputFilePathAndName());
			log("Writing output to: " + pd.getOutputFilePathAndName());
			ResponseEnvelope envelope = unmarshaller.unmarshal(inputFile);
			BankingResponseMessageSet messageSet = (BankingResponseMessageSet) envelope
					.getMessageSet(MessageSetType.banking);
			if (config.getVersion() == 0)
				ofxOutputVersion = Integer.parseInt(messageSet.getVersion());
			List<BankStatementResponseTransaction> responses = messageSet
					.getStatementResponses();
			for (BankStatementResponseTransaction response : responses) {
				BankStatementResponse message = response.getMessage();
				TransactionList tList = message.getTransactionList();
				List<?> transactions = tList.getTransactions();
				log("There are " + transactions.size()
						+ " transactions to process");
				for (Iterator<?> iterator = transactions.iterator(); iterator
						.hasNext();) {
					Transaction transaction = (Transaction) iterator.next();
					printTransaction(transaction);
					if (transaction.getTransactionType() == TransactionType.DEBIT) {
						preProcessTransaction(transaction);
					} else if (transaction.getTransactionType() == TransactionType.CHECK) {
						preProcessCheck(transaction);
					} else {
						log("Transaction unprocessed");
					}
				}
			}
			if (ofxOutputVersion == 2)
				writeOFX2(outputFile, envelope);
			else if (ofxOutputVersion == 1) writeOFX1(outputFile, envelope);
			log("Process complete");
		} catch (OFXParseException e) {
			log("Error: " + e.getMessage());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				inputFile.close();
				outputFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void printTransaction(Transaction transaction) {
		log("Transaction:");
		log("*****" + transaction.getTransactionType());
		log("*****" + transaction.getName());
		log("*****" + transaction.getMemo());
		log("*****" + transaction.getAmount());
		log("");
	}

	// Add the payee name from the payees file
	// Don't exceed 32 chars in the name
	private void preProcessCheck(Transaction transaction) {
		System.out.println("Check found - Number: "
				+ transaction.getCheckNumber());
		String payee = c.getPayee(transaction.getCheckNumber());
		if (payee != null) {
			log("Changing <NAME>: " + transaction.getName() + " to: " + payee);
			int len = payee.length();
			int endIdx = (len >= 32) ? 31 : len;
			transaction.setName(payee.substring(0, endIdx));
		} else
			log("No payee found for check number: "
					+ transaction.getCheckNumber());
	}

	private void writeOFX2(FileOutputStream outputFile,
			ResponseEnvelope envelope) throws IOException {
		StringWriter marshalled = new StringWriter();
		OFXV2Writer writer = new OFXV2Writer(marshalled);
		new AggregateMarshaller().marshal(envelope, writer);
		writer.close();
		if (config.isInsertPrivateTag()) {
			String finalOutput = addPrivateTag(marshalled, 2);
			outputFile.write(finalOutput.getBytes());
		} else {
			outputFile.write(marshalled.toString().getBytes());
		}
		outputFile.write(marshalled.toString().getBytes());
		outputFile.flush();
		outputFile.close();
	}

	private void writeOFX1(FileOutputStream outputFile,
			ResponseEnvelope envelope) throws IOException {
		StringWriter marshalled = new StringWriter();
		OFXV1Writer writer = new OFXV1Writer(marshalled);
		writer.setWriteAttributesOnNewLine(true);
		new AggregateMarshaller().marshal(envelope, writer);
		writer.close();
		if (config.isInsertPrivateTag()) {
			String finalOutput = addPrivateTag(marshalled, 1);
			outputFile.write(finalOutput.getBytes());
		} else {
			outputFile.write(marshalled.toString().getBytes());
		}
		outputFile.flush();
		outputFile.close();
	}

	// The version of the OFX jar does not support the tag <INTU.BID>. One of
	// the banks we use includes the tag and one doesn't. So add the tag as
	// needed
	private String addPrivateTag(StringWriter marshalled, int version) {
		String out = marshalled.toString();
		String NEW_LINE = System.getProperty("line.separator");
		String bidOpen = "<INTU.BID>430";
		String userIdOpen = "<INTU.USERID>ceharrin";
		String bidClosed = "</INTU.BID>";
		String userIdClosed = "</INTU.USERID>";
		int idx = out.indexOf("</FI>");
		String a = out.substring(0, idx + 5);
		String b = out.substring(idx + 5);
		String newS = null;
		if (version == 2)
			newS = a + NEW_LINE + bidOpen + bidClosed + NEW_LINE + userIdOpen
					+ userIdClosed + b;
		else
			newS = a + NEW_LINE + bidOpen + NEW_LINE + userIdOpen + b;
		return newS;
	}

	// Add the payee name to the transaction if the name matches one of the
	// strings above
	// Don't exceed 32 chars in the name
	private void preProcessTransaction(Transaction transaction) {
		String n = null;
		if (null != transaction.getMemo()) {
			n = p.getPayee(transaction.getMemo());
		}
		if (null == n) n = p.getPayee(transaction.getName());
		int len = n.length();
		int endIdx = (len >= 32) ? 31 : len;
		log("Changing <NAME>: " + transaction.getName() + " to: " + n);
		transaction.setName(n.substring(0, endIdx));
		// String m = tm.getTransactionRenamed(transaction);
	}

	public String getLogEntries() {
		return logBuffer.toString();
	} 
	
	public StringBuffer getLogBuffer() {
		return logBuffer;
	}

	public void setLogBuffer(StringBuffer logBuffer) {
		this.logBuffer = logBuffer;
	}

	public void log(String s) {
		System.out.println(s);
		logBuffer.append(s + System.getProperty("line.separator"));
	}
}

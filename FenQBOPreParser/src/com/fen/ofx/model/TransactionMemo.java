package com.fen.ofx.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.ofx4j.domain.data.common.Transaction;

public class TransactionMemo {
	public List<String> getTransactionTypes() {
		return transactionTypes;
	}

	public void setTransactionTypes(List<String> transactionTypes) {
		this.transactionTypes = transactionTypes;
	}

	List<String>	transactionTypes	= null;

	public TransactionMemo(String tTypesFile) {
		File f = new File(tTypesFile);
		transactionTypes = new ArrayList<String>();
		try {
			BufferedReader input = new BufferedReader(new FileReader(f));
			try {
				String line = null;
				while ((line = input.readLine()) != null) {
					transactionTypes.add(line);
					System.out.println("Added transaction type: " + line);
				}
			} finally {
				input.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public String getTransactionRenamed(Transaction transaction) {
		String s = null;
		for (String type : transactionTypes) {
			if (transaction.getName().contains(type)) {
				s = transaction.getMemo().substring(type.length()).trim();
			}
		}
		return s;
	}
}

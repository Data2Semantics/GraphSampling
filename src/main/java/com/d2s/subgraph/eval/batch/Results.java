package com.d2s.subgraph.eval.batch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.d2s.subgraph.helpers.Helper;

import au.com.bytecode.opencsv.CSVWriter;



public class Results {
	private ArrayList<Result> results = new ArrayList<Result>();
	
	public void add(Result result) {
		results.add(result);
	}
	
	public void writeAsCsv(String path, boolean overwrite) throws IOException {
		File csvFile = new File(path);
		if (csvFile.exists() == false || overwrite) {
			csvFile.createNewFile();
		}
		CSVWriter writer = new CSVWriter(new FileWriter(csvFile), ';');
		writer.writeNext(new String[]{"query", "isAggregation", "isAsk", "isOnlyDbo", "isSelect", "precision", "recall"});
		for (Result result: results) {
			ArrayList<String> columns = new ArrayList<String>();
			columns.add(result.getQuery().getQuery());
			columns.add(Helper.boolAsString(result.getQuery().isAggregation()));
			columns.add(Helper.boolAsString(result.getQuery().isAsk()));
			columns.add(Helper.boolAsString(result.getQuery().isOnlyDbo()));
			columns.add(Helper.boolAsString(result.getQuery().isSelect()));
			columns.add(Double.toString(result.getPrecision()));
			columns.add(Double.toString(result.getRecall()));
			writer.writeNext(columns.toArray(new String[columns.size()]));
		}
	    
		writer.close();
	}
	
}
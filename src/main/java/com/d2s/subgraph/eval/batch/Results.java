package com.d2s.subgraph.eval.batch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.d2s.subgraph.helpers.Helper;

import au.com.bytecode.opencsv.CSVWriter;



public class Results {
	private HashMap<Integer, Result> results = new HashMap<Integer, Result>();
	private String graphName;
	public void add(Result result) {
		results.put(result.getQuery().getQueryId(), result);
	}
	
	public Result get(int queryId) {
		return results.get(results.get(queryId));
	}
	
	public boolean queryIdExists(int queryId) {
		return results.containsKey(queryId);
	}
	
	public void writeAsCsv(String path, boolean overwrite) throws IOException {
		File csvFile = new File(path);
		if (csvFile.exists() == false || overwrite) {
			csvFile.createNewFile();
		}
		CSVWriter writer = new CSVWriter(new FileWriter(csvFile), ';');
//		writer.writeNext(new String[]{"query", "constructQuery", "isAggregation", "isAsk", "isOnlyDbo", "isSelect", "precision", "recall"});
		writer.writeNext(new String[]{"queryId", "isAggregation", "isAsk", "isOnlyDbo", "isSelect", "recall", "query"});
		for (Result result: results.values()) {
			ArrayList<String> columns = new ArrayList<String>();
			columns.add(Integer.toString(result.getQuery().getQueryId()));
//			columns.add(result.getQuery().getAsConstructQuery());
			columns.add(Helper.boolAsString(result.getQuery().isAggregation()));
			columns.add(Helper.boolAsString(result.getQuery().isAsk()));
			columns.add(Helper.boolAsString(result.getQuery().isOnlyDbo()));
			columns.add(Helper.boolAsString(result.getQuery().isSelect()));
//			columns.add(Double.toString(result.getPrecision()));
			columns.add(Double.toString(result.getRecall()));
			columns.add(result.getQuery().toString());
			writer.writeNext(columns.toArray(new String[columns.size()]));
		}
	    
		writer.close();
	}
	
	public double getAveragePrecision() {
		double totalPrecision = 0.0;
		for (Result result: results.values()) {
			totalPrecision += result.getPrecision();
		}
		return totalPrecision / (double)results.size();
	}
	
	
	public double getAverageRecall() {
		double totalRecall = 0.0;
		for (Result result: results.values()) {
			totalRecall += result.getRecall();
		}
		return totalRecall / (double)results.size();
	}
	
	public int getMaxQueryId() {
		int max = 0;
		for (Result result: results.values()) {
			if (result.getQuery().getQueryId() > max) {
				max = result.getQuery().getQueryId();
			}
		}
		return max;
	}

	public String getGraphName() {
		return graphName;
	}

	public void setGraphName(String name) {
		this.graphName = name;
	}
	
	public HashMap<Integer, Result> getAsHashMap() {
		return results;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<Integer> getQueryIds() {
		ArrayList<Integer> queryIds = new ArrayList<Integer>();
		for (Result result: results.values()) {
			queryIds.add(result.getQuery().getQueryId());
		}
		//remove duplicates
		@SuppressWarnings("rawtypes")
		HashSet hs = new HashSet();
		hs.addAll(queryIds);
		queryIds.clear();
		queryIds.addAll(hs);
		return queryIds;
	}
}
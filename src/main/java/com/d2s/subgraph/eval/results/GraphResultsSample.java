package com.d2s.subgraph.eval.results;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.d2s.subgraph.helpers.Helper;

import au.com.bytecode.opencsv.CSVWriter;



public class GraphResultsSample implements GraphResults{
	private HashMap<Integer, QueryResultsRegular> results = new HashMap<Integer, QueryResultsRegular>();
	private String graphName;
	
	public void add(QueryResultsRegular result) {
		results.put(result.getQuery().getQueryId(), result);
	}
	
	/**
	 * aggregate these indivudal sample results to this one sample resultset
	 * @param individualSampleResults
	 */
	public void add(ArrayList<GraphResults> individualSampleResults) {
		
	}
	
	public QueryResultsRegular get(int queryId) {
		return results.get(queryId);
	}
	
	public boolean queryIdExists(int queryId) {
		return results.containsKey(queryId);
	}
	
	public void writeAsCsv(String path) throws IOException {
		File csvFile = new File(path);
		CSVWriter writer = new CSVWriter(new FileWriter(csvFile), ';');
		writer.writeNext(new String[]{"queryId", "isAggregation", "isAsk", "isOnlyDbo", "isSelect", "recall", "query"});
		for (QueryResultsRegular result: results.values()) {
			ArrayList<String> columns = new ArrayList<String>();
			columns.add(Integer.toString(result.getQuery().getQueryId()));
			columns.add(Helper.boolAsString(result.getQuery().isAggregation()));
			columns.add(Helper.boolAsString(result.getQuery().isAsk()));
			columns.add(Helper.boolAsString(result.getQuery().isOnlyDbo()));
			columns.add(Helper.boolAsString(result.getQuery().isSelect()));
			columns.add(Double.toString(result.getRecall()));
			columns.add(result.getQuery().toString());
			writer.writeNext(columns.toArray(new String[columns.size()]));
		}
	    
		writer.close();
	}
	
	public double getAveragePrecision() {
		double totalPrecision = 0.0;
		for (QueryResultsRegular result: results.values()) {
			totalPrecision += result.getPrecision();
		}
		return totalPrecision / (double)results.size();
	}
	
	
	public double getAverageRecall() {
		double totalRecall = 0.0;
		for (QueryResultsRegular result: results.values()) {
			totalRecall += result.getRecall();
		}
		return totalRecall / (double)results.size();
	}
	
	public int getMaxQueryId() {
		int max = 0;
		for (QueryResultsRegular result: results.values()) {
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
	
	public HashMap<Integer, QueryResultsRegular> getAsHashMap() {
		return results;
	}
	
	public ArrayList<QueryResultsRegular> getAsArrayList() {
		return new ArrayList<QueryResultsRegular>(results.values());
	}
	@SuppressWarnings("unchecked")
	public ArrayList<Integer> getQueryIds() {
		ArrayList<Integer> queryIds = new ArrayList<Integer>();
		for (QueryResultsRegular result: results.values()) {
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
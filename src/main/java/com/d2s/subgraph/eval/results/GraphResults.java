package com.d2s.subgraph.eval.results;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import au.com.bytecode.opencsv.CSVWriter;

import com.d2s.subgraph.helpers.Helper;



public abstract class GraphResults {
	
	protected HashMap<Integer, QueryResults> results = new HashMap<Integer, QueryResults>();
	protected String graphName;
	protected int totalTruePositives = 0;
	protected int totalGoldenStandardSize = 0;
	
	public abstract void add(QueryResults result);
	protected abstract String getRewriteMethod();
	protected abstract String getAlgorithm();
	protected abstract int getPercentage();
	protected abstract String getShortGraphName();
	protected abstract String getProperName();
	
	protected QueryResults get(int queryId) {
		return results.get(queryId);
	}
	protected boolean contains(int queryId) {
		return results.containsKey(queryId);
	}
	
	protected boolean queryIdExists(int queryId) {
		return results.containsKey(queryId);
	}
	
	public void writeAsCsv(String path) throws IOException {
		path += "/" + getGraphName().substring(7) + ".csv";//remove http://
		File csvFile = new File(path);
		CSVWriter writer = new CSVWriter(new FileWriter(csvFile), ';');
		writer.writeNext(new String[]{"queryId", "isAggregation", "isAsk", "isOnlyDbo", "isSelect", "recall", "query"});
		for (QueryResults result: results.values()) {
			ArrayList<String> columns = new ArrayList<String>();
			columns.add(Integer.toString(result.getQuery().getQueryId()));
			columns.add(Helper.boolAsString(result.getQuery().hasAggregators()));
			columns.add(Helper.boolAsString(result.getQuery().isAskType()));
			columns.add(Helper.boolAsString(result.getQuery().isOnlyDbo()));
			columns.add(Helper.boolAsString(result.getQuery().isSelectType()));
			columns.add(Double.toString(result.getRecall()));
			columns.add(result.getQuery().toString());
			writer.writeNext(columns.toArray(new String[columns.size()]));
		}
	    
		writer.close();
	}
	
	protected double getAveragePrecision() {
		double totalPrecision = 0.0;
		for (QueryResults result: results.values()) {
			totalPrecision += result.getPrecision();
		}
		return totalPrecision / (double)results.size();
	}
	
	
	protected double getAverageRecall() {
		double totalRecall = 0.0;
		for (QueryResults result: results.values()) {
			totalRecall += result.getRecall();
		}
		return totalRecall / (double)results.size();
	}
	protected double getMedianRecall() {
		DescriptiveStatistics recallStats = new DescriptiveStatistics();
		for (QueryResults result: results.values()) {
			recallStats.addValue(result.getRecall());
		}
		return recallStats.getPercentile(50);
	}
	protected double getStdRecall() {
		DescriptiveStatistics recallStats = new DescriptiveStatistics();
		for (QueryResults result: results.values()) {
			recallStats.addValue(result.getRecall());
		}
		return recallStats.getStandardDeviation();
	}
	
	protected int getMaxQueryId() {
		return Collections.max(results.keySet());
	}

	protected String getGraphName() {
		return graphName;
	}

	public void setGraphName(String name) {
		this.graphName = name;
	}
	
	protected HashMap<Integer, QueryResults> getAsHashMap() {
		return results;
	}
	
	protected ArrayList<QueryResults> getAsArrayList() {
		return new ArrayList<QueryResults>(results.values());
	}
	protected ArrayList<Integer> getQueryIds() {
		return new ArrayList<Integer>(results.keySet());
	}
	public String toString() {
		return getGraphName();
	}
	
	public void addRecallTruePositives(int truePositives) {
		this.totalTruePositives += truePositives;
	}


	public void addRecallGoldenStandardSize(int size) {
		this.totalGoldenStandardSize += size;
	}
	protected int getRecallTruePositives() {
		return totalTruePositives;
	}
	protected int getRecallGoldenStandardSize() {
		return totalGoldenStandardSize;
	}
	protected double getGraphRecall() { 
		return (double)totalTruePositives / (double)totalGoldenStandardSize;
	}

	
}
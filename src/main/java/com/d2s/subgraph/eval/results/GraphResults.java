package com.d2s.subgraph.eval.results;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.data2semantics.query.QueryCollection;

import au.com.bytecode.opencsv.CSVWriter;

import com.d2s.subgraph.queries.Query;
import com.d2s.subgraph.util.StringUtils;



public abstract class GraphResults {
	
	protected QueryCollection<Query> queryCollection;
//	protected HashMap<Integer, QueryResults> results = new HashMap<Integer, QueryResults>();
	protected String graphName;
	protected int totalTruePositives = 0;
	protected int totalGoldenStandardSize = 0;
	
	public abstract void add(Query query);
	protected abstract String getRewriteMethod();
	protected abstract String getAlgorithm();
	protected abstract int getPercentage();
	protected abstract String getShortGraphName();
	protected abstract String getProperName();
	
//	protected QueryResults get(int queryId) {
//		return results.get(queryId);
//	}
//	protected boolean contains(int queryId) {
//		return results.containsKey(queryId);
//	}
//	
//	protected boolean queryIdExists(int queryId) {
//		return results.containsKey(queryId);
//	}
	
//	
	public GraphResults() throws IOException {
		queryCollection = new QueryCollection<Query>();
	}
	public void writeAsCsv(String path) throws IOException {
		path += "/" + getGraphName().substring(7) + ".csv";//remove http://
		File csvFile = new File(path);
		CSVWriter writer = new CSVWriter(new FileWriter(csvFile), ';');
		writer.writeNext(new String[]{"queryId", "isAggregation", "isAsk", "isOnlyDbo", "isSelect", "recall", "query"});
		for (Query query: queryCollection.getQueries()) {
			QueryResults result = query.getResults();
			ArrayList<String> columns = new ArrayList<String>();
			columns.add(Integer.toString(query.getQueryId()));
			columns.add(StringUtils.boolAsString(query.hasAggregators()));
			columns.add(StringUtils.boolAsString(query.isAskType()));
			columns.add(StringUtils.boolAsString(query.isOnlyDbo()));
			columns.add(StringUtils.boolAsString(query.isSelectType()));
			columns.add(Double.toString(result.getRecall()));
			columns.add(query.toString());
			writer.writeNext(columns.toArray(new String[columns.size()]));
		}

	    
		writer.close();
	}
	
	protected double getAveragePrecision() {
		double totalPrecision = 0.0;
		for (Query query: queryCollection.getQueries()) {
			totalPrecision += query.getResults().getPrecision();
		}
		return totalPrecision / (double)queryCollection.getDistinctQueryCount();
	}
	
	
	protected double getAverageRecall() {
		double totalRecall = 0.0;
		for (Query query: queryCollection.getQueries()) {
			totalRecall += query.getResults().getRecall();
		}
		return totalRecall / (double)queryCollection.getDistinctQueryCount();
	}
	protected double getMedianRecall() {
		DescriptiveStatistics recallStats = new DescriptiveStatistics();
		for (Query query: queryCollection.getQueries()) {
			recallStats.addValue(query.getResults().getRecall());
		}
		return recallStats.getPercentile(50);
	}
	protected double getStdRecall() {
		DescriptiveStatistics recallStats = new DescriptiveStatistics();
		for (Query query: queryCollection.getQueries()) {
			recallStats.addValue(query.getResults().getRecall());
		}
		return recallStats.getStandardDeviation();
	}

	public String getGraphName() {
		return graphName;
	}

	public void setGraphName(String name) {
		this.graphName = name;
	}
	
	
//	protected ArrayList<QueryResults> getResults() {
//		ArrayList<QueryResults> queryResults = new ArrayList<QueryResults>();
//		for (Query query: queryCollection.getQueries()) {
//			queryResults.add(query.getResults());
//		}
//		return queryResults;
//	}
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

	public QueryCollection<Query> getQueryCollection() {
		return this.queryCollection;
	}
}
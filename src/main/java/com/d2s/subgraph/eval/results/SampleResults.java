package com.d2s.subgraph.eval.results;

import java.io.IOException;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.data2semantics.query.QueryCollection;

import com.d2s.subgraph.queries.Query;



public abstract class SampleResults {
	public enum SampleType{REGULAR, BASELINE_FREQ, BASELINE_RANDOM};
	public QueryCollection<Query> queryCollection;
	public String graphName;
	private double percentage;
	public int totalTruePositives = 0;
	public int totalGoldenStandardSize = 0;
	public abstract SampleType getSampleType();
	public abstract void add(Query query);
	public abstract String getRewriteMethod();
	public abstract String getAlgorithm();
	public String getPercentage() {
		return Double.toString(Math.round((percentage * (double)100))) + "%";
	}
	public void setPercentage(Double percentage) {
		this.percentage = percentage;
	}
	public abstract String getShortGraphName();
	public abstract String getProperName();
	
	public SampleResults() throws IOException {
		queryCollection = new QueryCollection<Query>();
	}
	
	public double getAveragePrecision() {
		double totalPrecision = 0.0;
		for (Query query: queryCollection.getQueries()) {
			totalPrecision += query.getResults().getPrecision();
		}
		return totalPrecision / (double)queryCollection.getDistinctQueryCount();
	}
	
	
	public double getAverageRecall() {
		double totalRecall = 0.0;
		for (Query query: queryCollection.getQueries()) {
			if (query.getResults() != null) {
				totalRecall += query.getResults().getRecall();
			}
		}
		return totalRecall / (double)queryCollection.getDistinctQueryCount();
	}
	public double getMedianRecall() {
		DescriptiveStatistics recallStats = new DescriptiveStatistics();
		for (Query query: queryCollection.getQueries()) {
			recallStats.addValue(query.getResults().getRecall());
		}
		return recallStats.getPercentile(50);
	}
	public double getStdRecall() {
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
	public int getRecallTruePositives() {
		return totalTruePositives;
	}
	public int getRecallGoldenStandardSize() {
		return totalGoldenStandardSize;
	}
	public double getGraphRecall() { 
		return (double)totalTruePositives / (double)totalGoldenStandardSize;
	}

	public QueryCollection<Query> getQueryCollection() {
		return this.queryCollection;
	}
}
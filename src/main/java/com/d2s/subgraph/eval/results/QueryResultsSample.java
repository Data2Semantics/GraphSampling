package com.d2s.subgraph.eval.results;

import java.util.ArrayList;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class QueryResultsSample extends QueryResults {
	private ArrayList<QueryResults> allQueryResults = new ArrayList<QueryResults>();
	private DescriptiveStatistics recallStats;
	private DescriptiveStatistics precisionStats;
	
	public void add(ArrayList<QueryResults> allQueryResults) {
		this.allQueryResults = allQueryResults;
		aggregateToSingleObject();
	}
	

	
	private void aggregateToSingleObject() {
		setQuery(allQueryResults.get(0).getQuery());
		setGoldenStandardSize(allQueryResults.get(0).getGoldenStandardSize());
		collectStats();
		
		//set precision
		setPrecision(getAvgPrecision());
		
		//set recall
		setRecall(getAvgRecall());
		
	}
	
	
	private double getAvgPrecision() {
		return precisionStats.getMean();
	}
	
	private double getAvgRecall() {
		return recallStats.getMean();
	}
	
	public double getRecallDeviation() {
		return recallStats.getStandardDeviation();
	}
	
	public double getPrecisionDeviation() {
		return precisionStats.getStandardDeviation();
	}
	
	private void collectStats() {
		recallStats = new DescriptiveStatistics();
		precisionStats = new DescriptiveStatistics();
		for (QueryResults result: allQueryResults) {
			if (result != null) {
				precisionStats.addValue(result.getPrecision());
				recallStats.addValue(result.getRecall());
			} else {
				System.out.println("skipped a query result in the stats calculation. this specific query probably resulted in an error (e.g. virtuoso 4000 error).");
			}
		}
	}
	
	@SuppressWarnings("unused")
	private double getMedianPrecision() {
		return precisionStats.getPercentile(50);
	}
	
	@SuppressWarnings("unused")
	private double getMedianRecall() {
		return recallStats.getPercentile(50);
	}
}
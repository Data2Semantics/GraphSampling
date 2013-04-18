package com.d2s.subgraph.eval.results;

import java.util.ArrayList;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.d2s.subgraph.eval.QueryWrapper;



public class QueryResultsSample implements QueryResults {
	private QueryWrapper query;
	private ArrayList<QueryResults> allQueryResults = new ArrayList<QueryResults>();
	private double precision;
	private double recall;
	private DescriptiveStatistics recallStats;
	private DescriptiveStatistics precisionStats;
	private int goldenStandardSize;
	public void add(ArrayList<QueryResults> allQueryResults) {
		this.allQueryResults = allQueryResults;
		aggregateToSingleObject();
	}
	

	
	private void aggregateToSingleObject() {
		setQuery(allQueryResults.get(0).getQuery());
		setGoldenStandardSize(allQueryResults.get(0).getGoldenStandardSize());
		collectStats();
		
		//set precision
		setPrecision(getMedianPrecision());
		
		//set recall
		setRecall(getMedianRecall());
		
	}
	
	
	@SuppressWarnings("unused")
	private double getAvgPrecision() {
		return precisionStats.getMean();
	}
	
	@SuppressWarnings("unused")
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
	
	private double getMedianPrecision() {
		return precisionStats.getPercentile(50);
	}
	
	private double getMedianRecall() {
		return recallStats.getPercentile(50);
	}
	
	public QueryWrapper getQuery() {
		return query;
	}
	public void setQuery(QueryWrapper evalQuery) {
		this.query = evalQuery;
	}
	public double getPrecision() {
		return precision;
	}
	public void setPrecision(double precision) {
		this.precision = precision;
	}
	public double getRecall() {
		return recall;
	}
	public void setRecall(double recall) {
		this.recall = recall;
	}
	
	public String toString() {
		return "recall: " + Double.toString(recall) + " precision: " + Double.toString(precision);
	}
	public void setGoldenStandardSize(int resultSize) {
		this.goldenStandardSize = resultSize;
		
	}
	public int getGoldenStandardSize() {
		return goldenStandardSize;
	}
	
}
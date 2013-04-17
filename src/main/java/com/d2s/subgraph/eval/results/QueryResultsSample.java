package com.d2s.subgraph.eval.results;

import java.util.ArrayList;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.d2s.subgraph.eval.QueryWrapper;



public class QueryResultsSample implements QueryResults {
	private QueryWrapper query;
	private ArrayList<QueryResults> allQueryResults = new ArrayList<QueryResults>();
	private double precision;
	private double recall;
	
	public void add(ArrayList<QueryResults> allQueryResults) {
		this.allQueryResults = allQueryResults;
		aggregateToSingleObject();
	}
	
	private void aggregateToSingleObject() {
		setQuery(allQueryResults.get(0).getQuery());
		
		//set precision
		setPrecision(getMedianPrecision());
		
		//set recall
		setRecall(getMedianRecall());
		
	}
	
	
	@SuppressWarnings("unused")
	private double getAvgPrecision() {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (QueryResults result: allQueryResults) {
		        stats.addValue(result.getPrecision());
		}
		return stats.getMean();
	}
	
	@SuppressWarnings("unused")
	private double getAvgRecall() {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (QueryResults result: allQueryResults) {
		        stats.addValue(result.getRecall());
		}
		return stats.getMean();
	}
	
	private double getMedianPrecision() {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (QueryResults result: allQueryResults) {
	        stats.addValue(result.getPrecision());
		}
		return stats.getPercentile(50);
	}
	
	private double getMedianRecall() {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (QueryResults result: allQueryResults) {
		        stats.addValue(result.getRecall());
		}
		return stats.getPercentile(50);
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
	
}
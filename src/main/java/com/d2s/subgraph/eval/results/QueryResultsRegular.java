package com.d2s.subgraph.eval.results;

import com.d2s.subgraph.queries.QueryWrapper;



public class QueryResultsRegular implements QueryResults {
	private QueryWrapper query;
	private double precision;
	private int goldenStandardSize;
	private double recall;
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
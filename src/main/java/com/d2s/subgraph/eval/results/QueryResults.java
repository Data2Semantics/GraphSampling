package com.d2s.subgraph.eval.results;

import com.d2s.subgraph.queries.Query;

public abstract class QueryResults {
	protected Query query;
	protected double precision;
	protected int goldenStandardSize;
	protected double recall;
	
	
	protected Query getQuery() {
		return query;
	}
	public void setQuery(Query evalQuery) {
		this.query = evalQuery;
	}
	protected double getPrecision() {
		return precision;
	}
	public void setPrecision(double precision) {
		this.precision = precision;
	}
	protected double getRecall() {
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
	protected int getGoldenStandardSize() {
		return goldenStandardSize;
	}
}
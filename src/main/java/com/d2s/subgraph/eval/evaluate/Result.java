package com.d2s.subgraph.eval.evaluate;



public class Result {
	private String query;
	private double precision;
	private double recall;
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
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
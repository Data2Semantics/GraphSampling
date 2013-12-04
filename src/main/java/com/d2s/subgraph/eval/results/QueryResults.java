package com.d2s.subgraph.eval.results;

import java.util.Date;

public abstract class QueryResults {
	protected double precision;
	protected int goldenStandardSize;
	protected double recall;
	protected Date goldenStandardDuration = null;
	protected Date subgraphDuration = null;
	
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
	
	public void setSubgraphDuration(Date date) {
		this.subgraphDuration = date;
	}
	public Date getGoldenStandardDuration(Date date) {
		return this.goldenStandardDuration;
	}
}
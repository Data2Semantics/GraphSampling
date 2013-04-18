package com.d2s.subgraph.eval.results;

import com.d2s.subgraph.eval.QueryWrapper;



public interface QueryResults {
	
	public QueryWrapper getQuery();
	public void setQuery(QueryWrapper evalQuery);
	public double getPrecision();
	public void setPrecision(double precision);
	public double getRecall();
	public void setRecall(double recall);
	public void setGoldenStandardSize(int resultSize);
	public int getGoldenStandardSize();
	
	public String toString();
	
}
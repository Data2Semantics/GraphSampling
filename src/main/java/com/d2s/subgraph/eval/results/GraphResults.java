package com.d2s.subgraph.eval.results;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;



public interface GraphResults {
	public void add(QueryResults result);
	
	public QueryResults get(int queryId);
	public boolean contains(int queryId);
	public boolean queryIdExists(int queryId);
	
	public void writeAsCsv(String path) throws IOException;
	
	public double getAveragePrecision();
	
	
	public double getAverageRecall();
	

	public String getGraphName();

	public void setGraphName(String name);
	
	public HashMap<Integer, QueryResults> getAsHashMap();
	
	public ArrayList<QueryResults> getAsArrayList();
	
	public ArrayList<Integer> getQueryIds();
	
}
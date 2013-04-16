package com.d2s.subgraph.eval.results;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;



public interface GraphResults {
	public void add(QueryResultsRegular result);
	
	public QueryResultsRegular get(int queryId);
	
	public boolean queryIdExists(int queryId);
	
	public void writeAsCsv(String path) throws IOException;
	
	public double getAveragePrecision();
	
	
	public double getAverageRecall();
	

	public String getGraphName();

	public void setGraphName(String name);
	
	public HashMap<Integer, QueryResultsRegular> getAsHashMap();
	
	public ArrayList<QueryResultsRegular> getAsArrayList();
	
	public ArrayList<Integer> getQueryIds();
	
}
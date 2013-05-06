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
	public double getMedianRecall();
	public double getStdRecall();

	public String getGraphName();
	
	public String getShortGraphName();
	
	public String getProperName();

	public void setGraphName(String name);
	
	public HashMap<Integer, QueryResults> getAsHashMap();
	
	public ArrayList<QueryResults> getAsArrayList();
	
	public ArrayList<Integer> getQueryIds();
	
	public String toString();
	
	public void addRecallTruePositives(int truePositives);
	public void addRecallGoldenStandardSize(int size);
	
	public int getRecallTruePositives();
	public int getRecallGoldenStandardSize();
	public double getGraphRecall();
	
	public String getRewriteMethod();
	public String getAlgorithm();
	
}
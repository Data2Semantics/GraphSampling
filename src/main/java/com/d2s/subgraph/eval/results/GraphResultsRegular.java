package com.d2s.subgraph.eval.results;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.d2s.subgraph.helpers.Helper;
import au.com.bytecode.opencsv.CSVWriter;



public class GraphResultsRegular implements GraphResults {
	private HashMap<Integer, QueryResults> results = new HashMap<Integer, QueryResults>();
	private String graphName;
	private int totalTruePositives = 0;
	private int totalGoldenStandardSize = 0;
	public void add(QueryResults result) {
		results.put(result.getQuery().getQueryId(), result);
	}
	
	public QueryResults get(int queryId) {
		return results.get(queryId);
	}
	public boolean contains(int queryId) {
		return results.containsKey(queryId);
	}
	
	public boolean queryIdExists(int queryId) {
		return results.containsKey(queryId);
	}
	
	public void writeAsCsv(String path) throws IOException {
		path += "/" + getGraphName().substring(7) + ".csv";//remove http://
		File csvFile = new File(path);
		CSVWriter writer = new CSVWriter(new FileWriter(csvFile), ';');
		writer.writeNext(new String[]{"queryId", "isAggregation", "isAsk", "isOnlyDbo", "isSelect", "recall", "query"});
		for (QueryResults result: results.values()) {
			ArrayList<String> columns = new ArrayList<String>();
			columns.add(Integer.toString(result.getQuery().getQueryId()));
			columns.add(Helper.boolAsString(result.getQuery().isAggregation()));
			columns.add(Helper.boolAsString(result.getQuery().isAsk()));
			columns.add(Helper.boolAsString(result.getQuery().isOnlyDbo()));
			columns.add(Helper.boolAsString(result.getQuery().isSelect()));
			columns.add(Double.toString(result.getRecall()));
			columns.add(result.getQuery().toString());
			writer.writeNext(columns.toArray(new String[columns.size()]));
		}
	    
		writer.close();
	}
	
	public double getAveragePrecision() {
		double totalPrecision = 0.0;
		for (QueryResults result: results.values()) {
			totalPrecision += result.getPrecision();
		}
		return totalPrecision / (double)results.size();
	}
	
	
	public double getAverageRecall() {
		double totalRecall = 0.0;
		for (QueryResults result: results.values()) {
			totalRecall += result.getRecall();
		}
		return totalRecall / (double)results.size();
	}
	public double getMedianRecall() {
		DescriptiveStatistics recallStats = new DescriptiveStatistics();
		for (QueryResults result: results.values()) {
			recallStats.addValue(result.getRecall());
		}
		return recallStats.getPercentile(50);
	}
	public double getStdRecall() {
		DescriptiveStatistics recallStats = new DescriptiveStatistics();
		for (QueryResults result: results.values()) {
			recallStats.addValue(result.getRecall());
		}
		return recallStats.getStandardDeviation();
	}
	
	public int getMaxQueryId() {
		return Collections.max(results.keySet());
	}

	public String getGraphName() {
		return graphName;
	}

	public void setGraphName(String name) {
		this.graphName = name;
	}
	
	public HashMap<Integer, QueryResults> getAsHashMap() {
		return results;
	}
	
	public ArrayList<QueryResults> getAsArrayList() {
		return new ArrayList<QueryResults>(results.values());
	}
	public ArrayList<Integer> getQueryIds() {
		return new ArrayList<Integer>(results.keySet());
	}
	public String toString() {
		return getGraphName();
	}
	
	public String getShortGraphName() {
		String graphName = getGraphName();
		graphName = graphName.replace("unweighted_", "");
		graphName = graphName.replace(".nt", "");
		ArrayList<String> parts = new ArrayList<String>(Arrays.asList( graphName.split("_")));
		parts.remove(0); //http://dbp
		String shortGraphname = "";
		for (String part: parts) {
			shortGraphname += part + "_";
		}
		shortGraphname = shortGraphname.substring(0, shortGraphname.length()-1);//remove trailing _
		return shortGraphname;
		
	}
	
	public String getProperName() {
		int rewriteMethod = Helper.getRewriteMethod(getGraphName());
		int analysisAlg = Helper.getAnalysisAlgorithm(getGraphName());
		String properName = "";
		if (rewriteMethod == Helper.REWRITE_NODE1) {
			properName += "Node1";
		} else if (rewriteMethod == Helper.REWRITE_NODE2) {
			properName += "Node2";
		} else if (rewriteMethod == Helper.REWRITE_NODE3) {
			properName += "Node3";
		} else if (rewriteMethod == Helper.REWRITE_NODE4) {
			properName += "Node4";
		} else if (rewriteMethod == Helper.REWRITE_PATH) {
			properName += "Path";
		}
		if (analysisAlg == Helper.ALG_BETWEENNESS) {
			properName += " betweenness";
		} else if (analysisAlg == Helper.ALG_EIGENVECTOR) {
			properName += " eigenvector";
		} else if (analysisAlg == Helper.ALG_INDEGREE) {
			properName += " indegree";
		} else if (analysisAlg == Helper.ALG_OUTDEGREE) {
			properName += " outdegree";
		} else if (analysisAlg == Helper.ALG_PAGERANK) {
			properName += " pagerank";
		}
		ArrayList<String> parts = new ArrayList<String>(Arrays.asList( graphName.split("-")));
		String trailingBit = parts.get(parts.size()-1);
		trailingBit = trailingBit.replace(".nt", "");
		properName += " " + trailingBit + "%";
		return properName;
	}

	public void addRecallTruePositives(int truePositives) {
		this.totalTruePositives += truePositives;
	}


	public void addRecallGoldenStandardSize(int size) {
		this.totalGoldenStandardSize += size;
	}
	public int getRecallTruePositives() {
		return totalTruePositives;
	}
	public int getRecallGoldenStandardSize() {
		return totalGoldenStandardSize;
	}
	public double getGraphRecall() { 
		return (double)totalTruePositives / (double)totalGoldenStandardSize;
	}
	public String getRewriteMethod() {
		String rewriteMethod = Helper.getRewriteMethodAsString(graphName);
		if (rewriteMethod.length() == 0) {
			if (graphName.contains("Baseline")) {
				rewriteMethod = "baseline";
			} else {
				rewriteMethod = graphName;
			}
		}
		return rewriteMethod;
	}
	public String getAlgorithm() {
		String algorithm = Helper.getAlgorithmAsString(graphName);
		if (algorithm.length() == 0) {
			algorithm = graphName;
			if (graphName.contains("Baseline")) {
				algorithm = "resource frequency";
			} else {
				algorithm = graphName;
			}
		}
		return algorithm;
	}
}
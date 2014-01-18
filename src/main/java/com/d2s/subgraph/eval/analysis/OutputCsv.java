package com.d2s.subgraph.eval.analysis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.data2semantics.query.QueryCollection;

import au.com.bytecode.opencsv.CSVWriter;

import com.d2s.subgraph.eval.Config;
import com.d2s.subgraph.eval.experiments.ExperimentSetup;
import com.d2s.subgraph.eval.results.QueryResults;
import com.d2s.subgraph.eval.results.SampleResults;
import com.d2s.subgraph.queries.Query;
import com.d2s.subgraph.util.StringUtils;
import com.d2s.subgraph.util.StringUtils.Algorithms;
import com.d2s.subgraph.util.StringUtils.RewriteMethods;

public class OutputCsv extends OutputWrapper {
	public OutputCsv(ExperimentSetup experimentSetup, ArrayList<SampleResults> allGraphResults, QueryCollection<Query> queryCollection, File resultsDir) {
		super(experimentSetup, allGraphResults, queryCollection, resultsDir);
	}
	
	

	/**
	 * table with graph evaluations as columns, and queries per row (with query id as row header)
	 * @throws IOException 
	 */
	public void asCsvTable() throws IOException {
//		System.out.println("writing csv files for "  + onlyGraphsContaining);
		HashMap<Integer, ArrayList<String>> table = new HashMap<Integer, ArrayList<String>>();
		
		for (Query query: getQueryCollection().getQueries()) {
			int queryId = query.getQueryId();
			
			ArrayList<String> row = new ArrayList<String>();
			row.add(Integer.toString(queryId));
			table.put(queryId, row);
		}
		
		for (SampleResults graphResults: allGraphResults) {
//			if (StringUtils.partialStringMatch(graphResults.getGraphName(), onlyGraphsContaining)) {
				for (Query query: graphResults.getQueryCollection().getQueries()) {
					ArrayList<String> row = table.get(query.getQueryId());
					if (query.getResults() != null) {
						QueryResults queryResults = query.getResults();
						row.add(Double.toString(queryResults.getRecall()));
					} else {
						row.add("N/A");
					}
				}
//			}
		}
		
		File csvFile = new File(resultsDir.getAbsolutePath() + "/" + Config.FILE_CSV_FULL_LIST);
		CSVWriter writer = new CSVWriter(new FileWriter(csvFile), ';');
		ArrayList<String> header = new ArrayList<String>();
		header.add("queryId");
		for (SampleResults graphResults: allGraphResults) {
//			if (StringUtils.partialStringMatch(graphResults.getGraphName(), onlyGraphsContaining)) {
				header.add(graphResults.getShortGraphName());
//			}
			
		}
		writer.writeNext(header.toArray(new String[header.size()]));
		for (ArrayList<String> row: table.values()) {
			writer.writeNext(row.toArray(new String[row.size()]));
		}
		writer.close();
	}
	
	/**
	 * table with graph evaluations as columns, and queries per row (with query id as row header)
	 * @throws IOException 
	 */
	public void asCsvFlatList() throws IOException {
		System.out.println("writing csv flatlist");
		File csvFile = new File(resultsDir.getAbsolutePath() + "/" + Config.FILE_CSV_FLAT_FULL_LIST);
		CSVWriter writer = new CSVWriter(new FileWriter(csvFile), ';');
		writer.writeNext(new String[]{"queryId", "graph", "recall", "rewrMethod", "algorithm"});
		
		for (SampleResults graphResults: allGraphResults) {
//			if (StringUtils.partialStringMatch(graphResults.getGraphName(), onlyGraphsContaining)) {
//				for (Query query: queryCollection.getQueries()) {
//					if (graphResults.contains(query.getQueryId())) {
//						writer.writeNext(new String[]{
//								Integer.toString(query.getQueryId()), 
//								graphResults.getProperName(), 
//								Double.toString(graphResults.get(query.getQueryId()).getRecall()), 
//								graphResults.getRewriteMethod(), 
//								graphResults.getAlgorithm() + " " + Integer.toString(graphResults.getPercentage()) + "%"});
//					}
//				}
				for (Query query: graphResults.getQueryCollection().getQueries()) {
					writer.writeNext(new String[]{
							Integer.toString(query.getQueryId()), 
							graphResults.getProperName(), 
							Double.toString(query.getResults().getRecall()), 
							graphResults.getRewriteMethod(), 
							graphResults.getAlgorithm() + " " + graphResults.getPercentage()});
				}
//			}
		}
		writer.close();
	}
	
	public void rewriteVsAlgs() throws IOException {
		System.out.println("writing csv rewrite vs. algs");
		HashMap<Algorithms, Double> node1 = new HashMap<Algorithms, Double>();
		HashMap<Algorithms, Double> node2 = new HashMap<Algorithms, Double>();
		HashMap<Algorithms, Double> node3 = new HashMap<Algorithms, Double>();
		HashMap<Algorithms, Double> node4 = new HashMap<Algorithms, Double>();
		HashMap<Algorithms, Double> path = new HashMap<Algorithms, Double>();
		
		for (SampleResults graphResults: allGraphResults) {
			String graphName = graphResults.getGraphName();
//			if (StringUtils.partialStringMatch(graphResults.getGraphName(), onlyGraphsContaining) && !graphName.contains("sample") && !graphName.contains("Baseline")) {
				HashMap<Algorithms, Double> hashmapPick = null;
				RewriteMethods rewriteMethod = StringUtils.getRewriteMethod(graphName);
				Algorithms analysisAlgorithm = StringUtils.getAnalysisAlgorithm(graphName);
				
				
				
				if (rewriteMethod == RewriteMethods.RESOURCE_SIMPLE) hashmapPick = node1;
				if (rewriteMethod == RewriteMethods.RESOURCE_WITHOUT_LIT) hashmapPick = node2;
				if (rewriteMethod == RewriteMethods.RESOURCE_UNIQUE) hashmapPick = node3;
				if (rewriteMethod == RewriteMethods.RESOURCE_CONTEXT) hashmapPick = node4;
				if (rewriteMethod == RewriteMethods.PATH) hashmapPick = path;
				if (hashmapPick == null) {
					System.out.println("Not able to detect rewrite method or analysis of graph " + graphName);
					System.exit(1);
				}
				hashmapPick.put(analysisAlgorithm, graphResults.getAverageRecall());
//			}
		}
		File csvFile = new File(resultsDir.getAbsolutePath() + "/" + Config.FILE_CSV_REWR_VS_ALGS);
		CSVWriter writer = new CSVWriter(new FileWriter(csvFile), ';');
		double sampleAverage = getSampleAverage("0.5");
		writer.writeNext(new String[]{Double.toString(sampleAverage), "eigenvector", "pagerank", "betweenness", "indegree", "outdegree"});
		
		ArrayList<String> row = getHashMapAsArrayRow(node1);
		row.add(0, "node1");
		writer.writeNext(row.toArray(new String[row.size()]));
		
		row = getHashMapAsArrayRow(node2);
		row.add(0, "node2");
		writer.writeNext(row.toArray(new String[row.size()]));
		
		row = getHashMapAsArrayRow(node3);
		row.add(0, "node3");
		writer.writeNext(row.toArray(new String[row.size()]));
		
		row = getHashMapAsArrayRow(node4);
		row.add(0, "node4");
		writer.writeNext(row.toArray(new String[row.size()]));
		
		row = getHashMapAsArrayRow(path);
		row.add(0, "path");
		writer.writeNext(row.toArray(new String[row.size()]));
		
		writer.close();
	}
	private ArrayList<String> getHashMapAsArrayRow(HashMap<Algorithms, Double> hm) {
		ArrayList<String> row = new ArrayList<String>();
		if (hm.containsKey(Algorithms.EIGENVECTOR)) {
			row.add(Double.toString(hm.get(Algorithms.EIGENVECTOR)));
		} else {
			row.add("");
		}
		if (hm.containsKey(Algorithms.PAGERANK)) {
			row.add(Double.toString(hm.get(Algorithms.PAGERANK)));
		} else {
			row.add("");
		}
		if (hm.containsKey(Algorithms.BETWEENNESS)) {
			row.add(Double.toString(hm.get(Algorithms.BETWEENNESS)));
		} else {
			row.add("");
		}
		if (hm.containsKey(Algorithms.INDEGREE)) {
			row.add(Double.toString(hm.get(Algorithms.INDEGREE)));
		} else {
			row.add("");
		}
		if (hm.containsKey(Algorithms.OUTDEGREE)) {
			row.add(Double.toString(hm.get(Algorithms.OUTDEGREE)));
		} else {
			row.add("");
		}
		return row;
	}
	private double getSampleAverage(String mode) {
		System.out.println("todo: implement random sample (avg)");
		return 0.0;
//		boolean found = false;
//		double sampleAverage = 0.0;
//		for (SampleResults results: this.allGraphResults) {
//			if (results.getGraphName().equals("http://" + experimentSetup.getGraphPrefix() + "sample_" + mode + ".nt")) {
//				found = true;
//				sampleAverage = results.getAverageRecall();
//			}
//		}
//		if (!found) {
//			System.out.println("could not find average recall for sample graphs... Tried looking for " + experimentSetup.getGraphPrefix() + "sample_" + mode + ".nt");
//			System.exit(1);
//		}
//		return sampleAverage;
		
	}
	public void outputAverageRecallPerQuery() throws IOException {
		File csvFile = new File(resultsDir.getAbsolutePath() + "/" + Config.FILE_CSV_AVG_RECALL_PER_QUERY);
		CSVWriter writer = new CSVWriter(new FileWriter(csvFile), ';');
		writer.writeNext(new String[]{"queryId", "avgRecall"});
		for (Query query: getQueryCollection().getQueries()) {
			int queryId = query.getQueryId();
			
			double totalRecall = 0.0;// totalrecall!
			int numGraphs = 0;
			for (SampleResults results: allGraphResults) {
				if (results.getQueryCollection().getQuery(query.toString()) != null) {
//					if (StringUtils.partialStringMatch(results.getGraphName(), onlyGraphsContaining)) {
						QueryResults queryResults = results.getQueryCollection().getQuery(query.toString()).getResults();
						totalRecall += queryResults.getRecall();
						numGraphs++;
//					}
				}
			}
			
			double avgRecall = totalRecall / (double)numGraphs;
			writer.writeNext(new String[]{Integer.toString(queryId), Double.toString(avgRecall)});
		}
		writer.close();
		
	}
	public void outputBestRecallPerAlgorithm() throws IOException {
		File csvFile = new File(resultsDir.getAbsolutePath() + "/" + Config.FILE_CSV_BEST_RECALL_PER_ALG);
		CSVWriter writer = new CSVWriter(new FileWriter(csvFile), ';');
		writer.writeNext(new String[]{"queryId", "algorithm", "bestRecall", "avgQueryRecall"});
//		int[] algorithms = new int[]{Utils.ALG_BETWEENNESS, Utils.ALG_EIGENVECTOR, Utils.ALG_INDEGREE, Utils.ALG_OUTDEGREE, Utils.ALG_PAGERANK};
		Algorithms[] algorithms = new Algorithms[]{Algorithms.INDEGREE, Algorithms.OUTDEGREE, Algorithms.PAGERANK};
		RewriteMethods[] rewrMethods = new RewriteMethods[]{RewriteMethods.RESOURCE_CONTEXT, RewriteMethods.RESOURCE_SIMPLE, RewriteMethods.RESOURCE_UNIQUE, RewriteMethods.RESOURCE_WITHOUT_LIT, RewriteMethods.PATH};
		
		for (Query query: getQueryCollection().getQueries()) {
			ArrayList<ArrayList<String>> rows = new ArrayList<ArrayList<String>>();
			int queryId = query.getQueryId();
			
			double totalRecall = 0.0;
			int graphCount = 0;
			for (Algorithms algorithm: algorithms) {
				ArrayList<String> row = new ArrayList<String>();
				double bestRecall = 0.0;
				int numGraphResultsFound = 0;
				for (SampleResults results: allGraphResults) {
					if (StringUtils.getAnalysisAlgorithm(results.getGraphName()) == algorithm) {
						numGraphResultsFound++;
						totalRecall += results.getQueryCollection().getQuery(query.toString()).getResults().getRecall();
						graphCount++;
						double currentRecall = results.getQueryCollection().getQuery(query.toString()).getResults().getRecall();
						if (currentRecall > bestRecall) {
							bestRecall = currentRecall;
						}
					}
				}
				/**
				 * 
				 * 
				 * 
				 * can I delete below? can't see why we need this
				 * 
				 * 
				 * 
				 * 
				 */
//				if (numGraphResultsFound != rewrMethods.length) {
//					
//					System.out.println("unable to find all rewrite methods when calculating best recall per algorithm");
//					System.exit(1);
//				} else {
					row.add(Integer.toString(queryId));
					row.add(StringUtils.getAlgorithmAsString(algorithm));
					row.add(Double.toString(bestRecall));
					rows.add(row);
//				}
			}
			double avgQueryRecall = totalRecall / (double)graphCount;
			for (ArrayList<String> row: rows) {
				writer.writeNext(new String[]{row.get(0), row.get(1), row.get(2), Double.toString(avgQueryRecall)});
			}
			
			
		}
		writer.close();
	}
}

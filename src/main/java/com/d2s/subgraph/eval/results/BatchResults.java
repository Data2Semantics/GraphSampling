package com.d2s.subgraph.eval.results;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import au.com.bytecode.opencsv.CSVWriter;

import com.d2s.subgraph.eval.EvaluateGraph;
import com.d2s.subgraph.eval.experiments.ExperimentSetup;
import com.d2s.subgraph.eval.results.GraphResults;
import com.d2s.subgraph.helpers.Helper;
import com.d2s.subgraph.helpers.RHelper;
import com.d2s.subgraph.queries.GetQueries;
import com.d2s.subgraph.queries.QueryWrapper;

public class BatchResults {
	private File resultsDir;
	private ArrayList<GraphResults> batchResults = new ArrayList<GraphResults>();
	private GetQueries queries;
	private ExperimentSetup experimentSetup;
	private static String FILE_CSV_SUMMARY = "summary.csv";
	private static String FILE_HTML_SUMMARY = "results.html";
	private static String FILE_CSV_FULL_LIST = "list.csv";
	private static String FILE_CSV_FLAT_FULL_LIST = "flatlist.csv";
	private static String FILE_CSV_REWR_VS_ALGS = "rewrVsAlgs.csv";
	private static String FILE_CSV_AVG_RECALL_PER_QUERY = "avgRecallPerQuery.csv";
	private static String FILE_CSV_BEST_RECALL_PER_ALG = "bestRecallPerAlgorithm.csv";
	private HashMap<String, Boolean> modesImported = new HashMap<String, Boolean>();
	
	public BatchResults(ExperimentSetup experimentSetup, GetQueries queries) throws IOException {
		this.experimentSetup = experimentSetup;
		this.queries = queries;
		this.resultsDir = new File(experimentSetup.getEvalResultsDir());
		FileUtils.deleteDirectory(resultsDir);
		resultsDir.mkdir();
	}
	
	public void writeOutput() throws IOException, InterruptedException {
		if (batchResults.size() > 0) {
			Collections.sort(batchResults, new GraphResultsComparator());
			writeSummaryCsv();
			ArrayList<ArrayList<String>> modesToOutput = new ArrayList<ArrayList<String>>();
			modesToOutput.add(new ArrayList<String>(Arrays.asList(new String[]{"max-20", "0.2"})));
			modesToOutput.add(new ArrayList<String>(Arrays.asList(new String[]{"max-50", "0.5"})));
			for (ArrayList<String> modes: modesToOutput) {
				boolean outputThisMode = false;
				for (String mode: modes) {
					if (modesImported.containsKey(mode) && modesImported.get(mode)) {
						outputThisMode = true;
						break;
					}
				}
				if (outputThisMode) {
					outputAsHtmlTable(modes);
					outputAsCsvTable(modes);
					outputAsCsvFlatList(modes);
					outputRewriteVsAlgs(modes);
					outputAverageRecallPerQuery(modes);
					outputBestRecallPerAlgorithm(modes);
//					plotBoxPlots(mode);
				}
			}
			drawPlots();
		} else {
			System.out.println("no results to write output for");
		}
	}
	
	
	private void outputBestRecallPerAlgorithm(ArrayList<String> onlyGraphsContaining) throws IOException {
		File csvFile = new File(resultsDir.getAbsolutePath() + "/" + FILE_CSV_BEST_RECALL_PER_ALG);
		CSVWriter writer = new CSVWriter(new FileWriter(csvFile), ';');
		writer.writeNext(new String[]{"queryId", "algorithm", "bestRecall", "avgQueryRecall"});
		int[] algorithms = new int[]{Helper.ALG_BETWEENNESS, Helper.ALG_EIGENVECTOR, Helper.ALG_INDEGREE, Helper.ALG_OUTDEGREE, Helper.ALG_PAGERANK};
		int[] rewrMethods = new int[]{Helper.REWRITE_NODE1, Helper.REWRITE_NODE2, Helper.REWRITE_NODE3, Helper.REWRITE_NODE4, Helper.REWRITE_PATH};
		
		for (QueryWrapper query: queries.getQueries()) {
			ArrayList<ArrayList<String>> rows = new ArrayList<ArrayList<String>>();
			int queryId = query.getQueryId();
			
			double totalRecall = 0.0;
			int graphCount = 0;
			for (int algorithm: algorithms) {
				ArrayList<String> row = new ArrayList<String>();
				double bestRecall = 0.0;
				int numGraphResultsFound = 0;
				for (GraphResults results: batchResults) {
					if (Helper.getAnalysisAlgorithm(results.getGraphName()) == algorithm) {
						numGraphResultsFound++;
						totalRecall += results.get(queryId).getRecall();
						graphCount++;
//						int rewrMethod = Helper.getRewriteMethod(results.getGraphName());
						if (results.get(queryId).getRecall() > bestRecall) {
							bestRecall = results.get(queryId).getRecall();
						}
					}
				}
				if (numGraphResultsFound != rewrMethods.length) {
					System.out.println("unable to find all rewrite methods when calculating best recall per algorithm");
					System.exit(1);
				} else {
					row.add(Integer.toString(queryId));
					row.add(Helper.getAlgorithmAsString(algorithm));
					row.add(Double.toString(bestRecall));
					rows.add(row);
				}
			}
			double avgQueryRecall = totalRecall / (double)graphCount;
			for (ArrayList<String> row: rows) {
				writer.writeNext(new String[]{row.get(0), row.get(1), row.get(2), Double.toString(avgQueryRecall)});
			}
			
			
		}
		writer.close();
	}

	private void outputAverageRecallPerQuery(ArrayList<String> onlyGraphsContaining) throws IOException {
		File csvFile = new File(resultsDir.getAbsolutePath() + "/" + FILE_CSV_AVG_RECALL_PER_QUERY);
		CSVWriter writer = new CSVWriter(new FileWriter(csvFile), ';');
		writer.writeNext(new String[]{"queryId", "avgRecall"});
		for (QueryWrapper query: queries.getQueries()) {
			int queryId = query.getQueryId();
			
			double totalRecall = 0.0;// totalrecall!
			int numGraphs = 0;
			for (GraphResults results: batchResults) {
				if (results.contains(queryId)) {
//					if (onlyGraphsContaining.length() == 0 || results.getGraphName().contains(onlyGraphsContaining)) {
					if (Helper.partialStringMatch(results.getGraphName(), onlyGraphsContaining)) {
						QueryResults queryResults = results.get(queryId);
						totalRecall += queryResults.getRecall();
						numGraphs++;
					}
				}
			}
			
			double avgRecall = totalRecall / (double)numGraphs;
			writer.writeNext(new String[]{Integer.toString(queryId), Double.toString(avgRecall)});
		}
		writer.close();
		
	}

	public void add(GraphResults graphResults) {
		if (graphResults.getGraphName().contains("max-20")) modesImported.put("max-20", true);
		if (graphResults.getGraphName().contains("0.2")) modesImported.put("0.2", true);
		if (graphResults.getGraphName().contains("max-50")) modesImported.put("max-50", true);
		if (graphResults.getGraphName().contains("0.5")) modesImported.put("0.5", true);
		this.batchResults.add(graphResults);
	}
	
	private void drawPlots() throws IOException, InterruptedException {
		System.out.println("drawing plots");
		RHelper rHelper = new RHelper();
		rHelper.drawPlots(resultsDir);
	}
	private void writeSummaryCsv() throws IOException {
		System.out.println("writing summary CSV");
		File csvFile = new File(resultsDir.getAbsolutePath() + "/" + FILE_CSV_SUMMARY);
		CSVWriter writer = new CSVWriter(new FileWriter(csvFile), ';');
		writer.writeNext(new String[]{"graph", "avg recall", "median recall", "std recall", "recallOnAllQueries", "goldenSize", "truePositives", "rewrMethod", "algorithm"});
		for (GraphResults graphResults: batchResults) {
			writer.writeNext(new String[]{
					graphResults.getProperName(), 
					Double.toString(graphResults.getAverageRecall()), 
					Double.toString(graphResults.getMedianRecall()), 
					Double.toString(graphResults.getStdRecall()), 
					Double.toString(graphResults.getGraphRecall()),
					Double.toString(graphResults.getRecallGoldenStandardSize()),
					Double.toString(graphResults.getRecallTruePositives()),
					graphResults.getRewriteMethod(),
					graphResults.getAlgorithm() + " " + Integer.toString(graphResults.getPercentage()) + "%"
			});
		}
		writer.close();
	}
	
	/**
	 * table with graph evaluations as columns, and queries per row (with query id as row header)
	 * @throws IOException 
	 */
	private void outputAsCsvTable(ArrayList<String> onlyGraphsContaining) throws IOException {
		System.out.println("writing csv files for "  + onlyGraphsContaining);
		HashMap<Integer, ArrayList<String>> table = new HashMap<Integer, ArrayList<String>>();
		for (QueryWrapper query: queries.getQueries()) {
			int queryId = query.getQueryId();
			
			ArrayList<String> row = new ArrayList<String>();
			row.add(Integer.toString(queryId));
			table.put(queryId, row);
		}
		
		for (GraphResults graphResults: batchResults) {
			if (Helper.partialStringMatch(graphResults.getGraphName(), onlyGraphsContaining)) {
				for (QueryWrapper query: queries.getQueries()) {
					ArrayList<String> row = table.get(query.getQueryId());
					if (graphResults.contains(query.getQueryId())) {
						QueryResults queryResults = graphResults.get(query.getQueryId());
						row.add(Double.toString(queryResults.getRecall()));
					} else {
						row.add("N/A");
					}
				}
			}
		}
		
		File csvFile = new File(resultsDir.getAbsolutePath() + "/" + onlyGraphsContaining.get(0) + "_" + FILE_CSV_FULL_LIST);
		CSVWriter writer = new CSVWriter(new FileWriter(csvFile), ';');
		ArrayList<String> header = new ArrayList<String>();
		header.add("queryId");
		for (GraphResults graphResults: batchResults) {
			if (Helper.partialStringMatch(graphResults.getGraphName(), onlyGraphsContaining)) {
				header.add(graphResults.getShortGraphName());
			}
			
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
	private void outputAsCsvFlatList(ArrayList<String> onlyGraphsContaining) throws IOException {
		System.out.println("writing csv flatlist for "  + onlyGraphsContaining);
		File csvFile = new File(resultsDir.getAbsolutePath() + "/" + onlyGraphsContaining.get(0) + "_"+ FILE_CSV_FLAT_FULL_LIST);
		CSVWriter writer = new CSVWriter(new FileWriter(csvFile), ';');
		writer.writeNext(new String[]{"queryId", "graph", "recall", "rewrMethod", "algorithm"});
		
		for (GraphResults graphResults: batchResults) {
			if (Helper.partialStringMatch(graphResults.getGraphName(), onlyGraphsContaining)) {
				for (QueryWrapper query: queries.getQueries()) {
					if (graphResults.contains(query.getQueryId())) {
						writer.writeNext(new String[]{
								Integer.toString(query.getQueryId()), 
								graphResults.getProperName(), 
								Double.toString(graphResults.get(query.getQueryId()).getRecall()), 
								graphResults.getRewriteMethod(), 
								graphResults.getAlgorithm() + " " + Integer.toString(graphResults.getPercentage()) + "%"});
					}
				}
			}
		}
		writer.close();
	}
	
	private void outputRewriteVsAlgs(ArrayList<String> onlyGraphsContaining) throws IOException {
		System.out.println("writing csv rewrite vs. algs "  + onlyGraphsContaining);
		HashMap<Integer, Double> node1 = new HashMap<Integer, Double>();
		HashMap<Integer, Double> node2 = new HashMap<Integer, Double>();
		HashMap<Integer, Double> node3 = new HashMap<Integer, Double>();
		HashMap<Integer, Double> node4 = new HashMap<Integer, Double>();
		HashMap<Integer, Double> path = new HashMap<Integer, Double>();
		
		for (GraphResults graphResults: batchResults) {
			String graphName = graphResults.getGraphName();
			if (Helper.partialStringMatch(graphResults.getGraphName(), onlyGraphsContaining) && !graphName.contains("sample") && !graphName.contains("Baseline")) {
				HashMap<Integer, Double> hashmapPick = null;
				int rewriteMethod = Helper.getRewriteMethod(graphName);
				int analysisAlgorithm = Helper.getAnalysisAlgorithm(graphName);
				
				
				
				if (rewriteMethod == Helper.REWRITE_NODE1) hashmapPick = node1;
				if (rewriteMethod == Helper.REWRITE_NODE2) hashmapPick = node2;
				if (rewriteMethod == Helper.REWRITE_NODE3) hashmapPick = node3;
				if (rewriteMethod == Helper.REWRITE_NODE4) hashmapPick = node4;
				if (rewriteMethod == Helper.REWRITE_PATH) hashmapPick = path;
				if (hashmapPick == null || analysisAlgorithm == -1) {
					System.out.println("Not able to detect rewrite method or analysis of graph " + graphName);
					System.exit(1);
				}
				hashmapPick.put(analysisAlgorithm, graphResults.getAverageRecall());
			}
		}
		File csvFile = new File(resultsDir.getAbsolutePath() + "/" + onlyGraphsContaining.get(0) + "_"+ FILE_CSV_REWR_VS_ALGS);
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
	
	
	private double getSampleAverage(String mode) {
		boolean found = false;
		double sampleAverage = 0.0;
		for (GraphResults results: this.batchResults) {
			if (results.getGraphName().equals("http://" + experimentSetup.getGraphPrefix() + "sample_" + mode + ".nt")) {
				found = true;
				sampleAverage = results.getAverageRecall();
			}
		}
		if (!found) {
			System.out.println("could not find average recall for sample graphs... Tried looking for " + experimentSetup.getGraphPrefix() + "sample_" + mode + ".nt");
			System.exit(1);
		}
		return sampleAverage;
		
	}
	
	private ArrayList<String> getHashMapAsArrayRow(HashMap<Integer, Double> hm) {
		ArrayList<String> row = new ArrayList<String>();
		if (hm.containsKey(Helper.ALG_EIGENVECTOR)) {
			row.add(Double.toString(hm.get(Helper.ALG_EIGENVECTOR)));
		} else {
			row.add("");
		}
		if (hm.containsKey(Helper.ALG_PAGERANK)) {
			row.add(Double.toString(hm.get(Helper.ALG_PAGERANK)));
		} else {
			row.add("");
		}
		if (hm.containsKey(Helper.ALG_BETWEENNESS)) {
			row.add(Double.toString(hm.get(Helper.ALG_BETWEENNESS)));
		} else {
			row.add("");
		}
		if (hm.containsKey(Helper.ALG_INDEGREE)) {
			row.add(Double.toString(hm.get(Helper.ALG_INDEGREE)));
		} else {
			row.add("");
		}
		if (hm.containsKey(Helper.ALG_OUTDEGREE)) {
			row.add(Double.toString(hm.get(Helper.ALG_OUTDEGREE)));
		} else {
			row.add("");
		}
		return row;
	}
	/**
	 * html table with graph evalutations as columns, and queries per row
	 * @throws IOException
	 */
	private void outputAsHtmlTable(ArrayList<String> onlyGraphsContaining) throws IOException {
		System.out.println("writing html files for "  + onlyGraphsContaining);
		String encodedEndpoint = URLEncoder.encode(EvaluateGraph.OPS_VIRTUOSO, "UTF-8"); 
		String html = "<html><head>\n" +
				"<link rel='stylesheet' href='http://www.few.vu.nl/~lrietveld/static/style.css' type='text/css' />\n" +
				"<script src='http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js'></script>\n" +
				"<script src='http://www.few.vu.nl/~lrietveld/static/tablesorter/jquery.tablesorter.min.js'></script>\n" +
				"<script src='http://www.few.vu.nl/~lrietveld/static/script.js'></script>\n" +
				"<script src='http://www.few.vu.nl/~lrietveld/static/jquery.stickytableheaders.js'></script>\n" +
				"</head>" +
				"\n<body>" +
				"\n<table id='myTable' class='tablesorter'>\n";
		HashMap<Integer, ArrayList<String>> table = new HashMap<Integer, ArrayList<String>>();
		
		
		//fill first two col of table (queryId and avg for this query)
		for (QueryWrapper query: queries.getQueries()) {
			int queryId = query.getQueryId();
			
			ArrayList<String> row = new ArrayList<String>();
			double totalRecall = 0.0;// totalrecall!
			int numGraphs = 0;
			for (GraphResults results: batchResults) {
				if (results.contains(queryId)) {
//					if (onlyGraphsContaining.length() == 0 || results.getGraphName().contains(onlyGraphsContaining)) {
					if (Helper.partialStringMatch(results.getGraphName(), onlyGraphsContaining)) {
						QueryResults queryResults = results.get(queryId);
						totalRecall += queryResults.getRecall();
						numGraphs++;
					}
				}
			}
			
			double avgRecall = totalRecall / (double)numGraphs;
			int goldenStandardSize = 0;
			String url = "#";
			if (batchResults.get(0).contains(queryId)) {
				QueryWrapper queryWrapper = batchResults.get(0).get(queryId).getQuery();
				goldenStandardSize = batchResults.get(0).get(queryId).getGoldenStandardSize();
				String encodedQuery = URLEncoder.encode(queryWrapper.getQueryString(experimentSetup.getGoldenStandardGraph()), "UTF-8");
				url = "http://yasgui.laurensrietveld.nl?endpoint=" + encodedEndpoint + "&query=" + encodedQuery + "&tabTitle=" + queryWrapper.getQueryId();
			}
			row.add("<td>" + queryId + "</td>");
			row.add("<td><a href='" + url + "' target='_blank'>" + Helper.getDoubleAsFormattedString(avgRecall) + " (n:" + goldenStandardSize + ")</a></td>");
			row.add("<td>" + query.getNumberOfJoins() + "</td>");
			row.add("<td>" + query.getNumberOfNonOptionalTriplePatterns() + "</td>");
			row.add("<td>" + query.getTriplePatternCountCcv() + "</td>");
			row.add("<td>" + query.getTriplePatternCountCvv() + "</td>");
			row.add("<td>" + query.getTriplePatternCountVcc() + "</td>");
			table.put(queryId, row);
		}
		
		
		html += "<thead>\n<tr>";
		html += "<th>queryId</th><th>avg</th><th>#joins<br></th><th>#tp's<br>(non opt)<br><th>#ccv<br></th><th>#cvv<br></th><th>#vcc<br></th>";
		
		for (GraphResults graphResults: batchResults) {
			if (Helper.partialStringMatch(graphResults.getGraphName(), onlyGraphsContaining)) {
				html += "\n<th>" + graphResults.getGraphName().substring("http://".length()).replace('_', '-') + "<br>(avg: " + Helper.getDoubleAsFormattedString(graphResults.getAverageRecall()) + ")</th>";
				for (QueryWrapper query: queries.getQueries()) {
					ArrayList<String> row = table.get(query.getQueryId());
					if (graphResults.contains(query.getQueryId())) {
						QueryResults queryResults = graphResults.get(query.getQueryId());
						String encodedQuery = URLEncoder.encode(queryResults.getQuery().getQueryString(graphResults.getGraphName()), "UTF-8");
						String url = "http://yasgui.laurensrietveld.nl?endpoint=" + encodedEndpoint + "&query=" + encodedQuery + "&tabTitle=" + queryResults.getQuery().getQueryId();
						String title = StringEscapeUtils.escapeHtml(queryResults.getQuery().getQueryString(graphResults.getGraphName()));
						String cell = "<td title='" + title + "'><a href='" + url + "' target='_blank'>" + Helper.getDoubleAsFormattedString(queryResults.getRecall()) + "</a></td>";
						row.add(cell);
					} else {
						row.add("<td>N/A</td>");
					}
				}
			}
		}
		
		html += "</tr></thead><tbody>\n";
		for (ArrayList<String> row: table.values()) {
			html += "\n<tr>";
			for (String cell: row) {
				html += cell;
			}
			html += "</tr>";
		}
		html += "\n</tbody> </table></body></html>";
		FileUtils.writeStringToFile(new File(experimentSetup.getEvalResultsDir() + "/" + onlyGraphsContaining.get(0) + "_" + FILE_HTML_SUMMARY), html);
	}
	

	

	public static void main(String[] args)  {
		
	}

}

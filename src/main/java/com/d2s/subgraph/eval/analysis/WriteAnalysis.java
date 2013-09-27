package com.d2s.subgraph.eval.analysis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.data2semantics.query.QueryCollection;

import au.com.bytecode.opencsv.CSVWriter;

import com.d2s.subgraph.eval.Config;
import com.d2s.subgraph.eval.experiments.ExperimentSetup;
import com.d2s.subgraph.eval.results.GraphResults;
import com.d2s.subgraph.eval.results.GraphResultsComparator;
import com.d2s.subgraph.io.QResultsLoader;
import com.d2s.subgraph.queries.Query;

public class WriteAnalysis {
	private File resultsDir;
	private ArrayList<GraphResults> allGraphResults = new ArrayList<GraphResults>();
	private QueryCollection<Query> queryCollection;
	private ExperimentSetup experimentSetup;

	private HashMap<String, Boolean> modesImported = new HashMap<String, Boolean>();
	
	private OutputCsv outputCsv;
	private OutputHtml outputHtml;
	private OutputR outputR;

	
	public WriteAnalysis(ExperimentSetup experimentSetup) throws IOException {
		this.experimentSetup = experimentSetup;
		this.queryCollection = experimentSetup.getQueryCollection();
		this.resultsDir = new File(experimentSetup.getEvalResultsDir());
		FileUtils.deleteDirectory(resultsDir);
		resultsDir.mkdir();
		
		outputCsv = new OutputCsv(experimentSetup, allGraphResults, queryCollection, resultsDir);
		outputHtml = new OutputHtml(experimentSetup, allGraphResults, queryCollection, resultsDir);
		outputR = new OutputR(experimentSetup, allGraphResults, queryCollection, resultsDir);
		
	}
	
	public void writeOutput() throws IOException, InterruptedException {
		if (allGraphResults.size() > 0) {
			Collections.sort(allGraphResults, new GraphResultsComparator());
			writeSummaryCsv();
			writeQuerySummaryCsv();
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
					outputHtml.asHtmlTable(modes);
					outputCsv.asCsvTable(modes);
					outputCsv.asCsvFlatList(modes);
					outputCsv.rewriteVsAlgs(modes);
					outputCsv.outputAverageRecallPerQuery(modes);
					outputCsv.outputBestRecallPerAlgorithm(modes);
				}
			}
			outputR.drawPlots();
		} else {
			System.out.println("no results to write output for");
		}
	}
	
	
	

	

	public void add(GraphResults graphResults) {
		if (graphResults.getGraphName().contains("max-20")) modesImported.put("max-20", true);
		if (graphResults.getGraphName().contains("0.2")) modesImported.put("0.2", true);
		if (graphResults.getGraphName().contains("max-50")) modesImported.put("max-50", true);
		if (graphResults.getGraphName().contains("0.5")) modesImported.put("0.5", true);
		this.allGraphResults.add(graphResults);
	}
	
	
	private void writeSummaryCsv() throws IOException {
		System.out.println("writing summary CSV");
		File csvFile = new File(resultsDir.getAbsolutePath() + "/" + Config.FILE_CSV_SUMMARY);
		CSVWriter writer = new CSVWriter(new FileWriter(csvFile), ';');
		writer.writeNext(new String[]{"graph", "avg recall", "median recall", "std recall", "recallOnAllQueries", "goldenSize", "truePositives", "rewrMethod", "algorithm"});
		for (GraphResults graphResults: allGraphResults) {
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
	
	private void writeQuerySummaryCsv() throws IOException {
		System.out.println("writing query summary CSV");
		File csvFile = new File(resultsDir.getAbsolutePath() + "/" + Config.FILE_CSV_QUERY_SUMMARY);
		CSVWriter writer = new CSVWriter(new FileWriter(csvFile), ';');
		ArrayList<String> headers = new ArrayList<String>();
		headers.add("queryId");
		for (GraphResults graphResults: allGraphResults) {
			headers.add(graphResults.getProperName());
		}
		writer.writeNext(headers.toArray(new String[headers.size()]));
		for (Query query: queryCollection.getQueries()) {
			ArrayList<String> row = new ArrayList<String>();
			int queryId = query.getQueryId();
			row.add(Integer.toString(queryId));
			for (GraphResults graphResults: allGraphResults) {
				row.add(Double.toString(graphResults.getQueryCollection().getQuery(query.toString()).getResults().getRecall()));
			}
			writer.writeNext(row.toArray(new String[row.size()]));
		}
		
		writer.close();
	}
	
	
	public static void doWrite(ExperimentSetup experimentSetup) throws IOException, InterruptedException {
		WriteAnalysis writeAnalysis = QResultsLoader.read(experimentSetup);
		writeAnalysis.writeOutput();
		
	}
	public static void main(String[] args)  {
		
	}
}

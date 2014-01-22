package com.d2s.subgraph.eval.qtriples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.d2s.subgraph.eval.Config;
import com.d2s.subgraph.eval.analysis.WriteAnalysis;
import com.d2s.subgraph.eval.experiments.ExperimentSetup;
import com.d2s.subgraph.eval.experiments.SwdfExperimentSetup;
import com.d2s.subgraph.eval.results.SampleResults;
import com.d2s.subgraph.eval.results.SampleResultsRandom;
import com.d2s.subgraph.eval.results.SampleResultsRegular;
import com.d2s.subgraph.queries.Query;

public class CalcRecall {
	private int maxSamples = Integer.MAX_VALUE;
	private int maxQueries = Integer.MAX_VALUE;
	private ExperimentSetup experimentSetup;
	CalcCutoffWeight cutoffWeights;
	private File[] queryDirs;
	ArrayList<SampleResults> regularResults = new ArrayList<SampleResults>();
	ArrayList<SampleResults> baselineResults = new ArrayList<SampleResults>();
	private File cwd;
	private double maxSampleSize;
	public CalcRecall(ExperimentSetup experimentSetup, double maxSampleSize, File cwd) throws IOException {
		this.experimentSetup = experimentSetup;
		this.cwd = cwd;
		this.maxSampleSize = maxSampleSize;
		cutoffWeights = new CalcCutoffWeight(experimentSetup, maxSampleSize, cwd);
		cutoffWeights.calcForFiles();
		if (cutoffWeights.getCutoffWeights().size() == 0) throw new IllegalStateException("Could not find any cutoff weights. unable to calc recall");
		fetchQueryDirs();
	}
	
	
	private void calcRecallForSamples() throws IOException, InterruptedException {
//		ArrayList<SampleResults> allResults = new ArrayList<SampleResults>();
		int count = 0;
		for (String sample: cutoffWeights.getCutoffWeights().keySet()) {
			System.out.println(sample);
			
			if (!sample.equals("resourceContext_indegree")) continue;
//			System.out.println(sample);
			if (count >= maxSamples) break;
			count++;
			HashMap<String, Double> sampleWeights = fetchSampleWeights(sample);
			System.out.println("calculating recall for dataset " + experimentSetup.getId() + " and sample " + sample);
			if (isBaselineSample(sample)) {
				baselineResults.add(calcForQueries(sample, sampleWeights));
			} else {
				regularResults.add(calcForQueries(sample, sampleWeights));
			}
		}
		
	}
	
	private boolean isBaselineSample(String sampleText) {
		return sampleText.toLowerCase().contains("baseline");
	}
	
	private void concatAndWriteOutput() throws IOException, InterruptedException {
		WriteAnalysis analysisOutput = new WriteAnalysis(experimentSetup, maxSampleSize);
		ArrayList<SampleResults> randomSampleResultsList = new ArrayList<SampleResults>();
		
		for (SampleResults results: baselineResults) {
			
			if (results.getGraphName().toLowerCase().contains("random")) {
				randomSampleResultsList.add(results);
			} else {
				regularResults.add(0, results);
			}
		}
		if (randomSampleResultsList.size() > 0) {
			SampleResultsRandom randomSampleResults = new SampleResultsRandom();
			randomSampleResults.add(randomSampleResultsList);
			regularResults.add(0,randomSampleResults);
		}
		
		analysisOutput.add(regularResults);
		analysisOutput.writeOutput();
		
	}
	private HashMap<String, Double> fetchSampleWeights(String sample) throws IOException {
		File sampleFile = new File(cwd.getAbsolutePath() + "/" + Config.PATH_WEIGHTED_QUERY_TRIPLES + experimentSetup.getId() + "/" + sample);
		if (!sampleFile.exists()) throw new IOException("tried to locate " + sampleFile.getPath() + ", but it isnt there. Unable to calc recall");
		HashMap<String, Double> sampleWeights = new HashMap<String, Double>();
		String line;
		BufferedReader br = new BufferedReader(new FileReader(sampleFile));
		while ((line = br.readLine()) != null) {
			if (line.length() > 0) {
				String[] splitLine = line.split("\t");
				String triple = "";
				Double weight = null;
				for (int i = 0; i < (splitLine.length -1); i++) {
					if (triple.length() > 0) triple += "\t";
					triple += splitLine[i];
				}
				weight = Double.parseDouble(splitLine[splitLine.length - 1]);
				sampleWeights.put(triple, weight);
			}
		}
		br.close();
		return sampleWeights;
	}
	private void fetchQueryDirs() {
		File queryTripleDir = new File(cwd.getAbsolutePath() + "/" + Config.PATH_QUERY_TRIPLES + experimentSetup.getId());
		File[] qsDirs = queryTripleDir.listFiles();
		
		TreeMap<Integer, File> filteredQsDirs = new TreeMap<Integer, File>();
		for (File file: qsDirs) {
			if (file.getName().startsWith("query-")) {
				filteredQsDirs.put(Integer.parseInt(file.getName().substring("query-".length())), file);
			}
		}
		filteredQsDirs.values().toArray(new File[filteredQsDirs.size()]);
		queryDirs = filteredQsDirs.values().toArray(new File[filteredQsDirs.size()]);
		if (queryDirs.length == 0) throw new IllegalStateException("could not find queries to calc recall for. Searching in dir " + queryTripleDir.getPath());
	}
	
	private SampleResults calcForQueries(String sample, HashMap<String, Double> sampleWeights) throws IOException {
		SampleResults results = new SampleResultsRegular();
		results.setGraphName(sample);
		int count = 0;
		for (File queryDir: queryDirs) {
//			System.out.println(queryDir.getName());System.exit(1);
			if (!queryDir.getName().equals("query-1")) continue;
//			System.out.println(queryDir.getName());dd
			if (count > maxQueries) break;
			try {
				Query query = CalcRecallForQuery.calc(experimentSetup, sample, sampleWeights, queryDir, cutoffWeights.getCutoffWeights().get(sample));
				query.setQueryId(count);
				results.add(query);
				count++;
			} catch (IllegalStateException e) {
				System.out.println(e.getMessage());
//				throw e;
			}
			
		}
		System.out.println("avg recall: " + results.getAverageRecall());
		results.setPercentage(cutoffWeights.getCutoffSize(sample));
		return results;
	}
	

	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, InterruptedException {
		File cwd = new File("");
		CalcRecall calc = new CalcRecall(new SwdfExperimentSetup(true), 0.5, cwd);
		calc.maxSamples = 1;
		calc.maxQueries = 5;
		calc.calcRecallForSamples();
		calc.concatAndWriteOutput();
	}
}

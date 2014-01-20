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
import com.d2s.subgraph.eval.results.SampleResultsRegular;
import com.d2s.subgraph.queries.Query;

public class CalcRecall {
	private int maxSamples = Integer.MAX_VALUE;
	private int maxQueries = Integer.MAX_VALUE;
	private ExperimentSetup experimentSetup;
	CalcCutoffWeight cutoffWeights;
	private File[] queryDirs;
	private WriteAnalysis analysisOutput;
	
	public CalcRecall(ExperimentSetup experimentSetup, double maxSampleSize) throws IOException {
		this.experimentSetup = experimentSetup;
		this.analysisOutput = new WriteAnalysis(experimentSetup);
		cutoffWeights = new CalcCutoffWeight(experimentSetup, maxSampleSize);
		cutoffWeights.calcForFiles();
		if (cutoffWeights.getCutoffWeights().size() == 0) throw new IllegalStateException("Could not find any cutoff weights. unable to calc recall");
		fetchQueryDirs();
	}
	
	
	private void calcRecallForSamples() throws IOException, InterruptedException {
		int count = 0;
		for (String sample: cutoffWeights.getCutoffWeights().keySet()) {
			if (count >= maxSamples) break;
			count++;
			HashMap<String, Double> sampleWeights = fetchSampleWeights(sample);
			System.out.println("calculating recall for dataset " + experimentSetup.getId() + " and sample " + sample);
			analysisOutput.add(calcForQueries(sample, sampleWeights));
		}
		analysisOutput.writeOutput();
	}
	private HashMap<String, Double> fetchSampleWeights(String sample) throws IOException {
		File sampleFile = new File(Config.PATH_WEIGHTED_QUERY_TRIPLES + experimentSetup.getId() + "/" + sample);
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
		File queryTripleDir = new File(Config.PATH_QUERY_TRIPLES + experimentSetup.getId());
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
		SampleResultsRegular results = new SampleResultsRegular();
		results.setGraphName(sample);
		int count = 0;
		for (File queryDir: queryDirs) {
			System.out.println(queryDir.getName());
			if (count > maxQueries) break;
			Query query = CalcRecallForQuery.calc(experimentSetup, sample, sampleWeights, queryDir, cutoffWeights.getCutoffWeights().get(sample));
			query.setQueryId(count);
			results.add(query);
			count++;
		}
		System.out.println("avg recall: " + results.getAverageRecall());
		results.setPercentage(cutoffWeights.getCutoffSize(sample));
		return results;
	}
	

	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, InterruptedException {
		CalcRecall calc = new CalcRecall(new SwdfExperimentSetup(true), 0.5);
		calc.maxSamples = 1;
		calc.maxQueries = 10;
		calc.calcRecallForSamples();
	}
}

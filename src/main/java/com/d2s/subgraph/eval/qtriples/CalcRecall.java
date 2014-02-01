package com.d2s.subgraph.eval.qtriples;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.d2s.subgraph.eval.Config;
import com.d2s.subgraph.eval.analysis.WriteAnalysis;
import com.d2s.subgraph.eval.experiments.ExperimentSetup;
import com.d2s.subgraph.eval.experiments.ExperimentSetupHelper;
import com.d2s.subgraph.eval.experiments.ObmExperimentSetup;
import com.d2s.subgraph.eval.results.SampleResults;
import com.d2s.subgraph.eval.results.SampleResultsRandom;
import com.d2s.subgraph.eval.results.SampleResultsRegular;
import com.d2s.subgraph.queries.Query;

public class CalcRecall implements Runnable{
	private int maxSamples = Integer.MAX_VALUE;
	private int maxQueries = Integer.MAX_VALUE;
	private ExperimentSetup experimentSetup;
	public static Map<String, Double> queryTriplesWithRandomWeight = null;
//	CalcCutoffWeight cutoffWeights;
	private File[] queryDirs;
	ArrayList<SampleResults> regularResults = new ArrayList<SampleResults>();
	ArrayList<SampleResults> baselineResults = new ArrayList<SampleResults>();
	private File cwd;
	private double maxSampleSize;
	private TreeMap<String, Double> cutoffWeights;
	private TreeMap<String, Double> cutoffSizes;
	private int totalSampleSize;
	private TreeMap<String, Double> cutoffWeightsPlusOne;
	private TreeMap<String, Double> cutoffSizesPlusOne;
	private Map<String, Map<String, Double>> allSampleWeights;
	public CalcRecall(ExperimentSetup experimentSetup, double maxSampleSize, TreeMap<String, Double> cutoffWeights, TreeMap<String, Double> cutoffSizes, TreeMap<String, Double> cutoffWeightsPlusOne, TreeMap<String, Double> cutoffSizesPlusOne, int totalSampleSize, File cwd) throws IOException {
		this.experimentSetup = experimentSetup;
		this.cwd = cwd;
		this.totalSampleSize = totalSampleSize;
		this.maxSampleSize = maxSampleSize;
		this.cutoffWeights = cutoffWeights;
		this.cutoffSizes = cutoffSizes;
		this.cutoffWeightsPlusOne = cutoffWeightsPlusOne;
		this.cutoffSizesPlusOne = cutoffSizesPlusOne;
//		queryTriplesWithRandomWeight = getQueryTriplesWithRandomWeightFromFile();
//		cutoffWeights = new CalcCutoffWeight(experimentSetup, cwd, maxSampleSize);
//		cutoffWeights.calcForFiles();
//		System.out.println(cutoffWeights.getCutoffWeights());
//		System.exit(1);
		if (cutoffWeights.size() == 0) throw new IllegalStateException("Could not find any cutoff weights. unable to calc recall");
		fetchQueryDirs();
	}
	
	
	public void setTripleWeights(Map<String, Map<String, Double>> allSampleWeights) {
		this.allSampleWeights = allSampleWeights;
	}
	
	public static void setTripleRandomWeights(Map<String, Double> sampleWeights) {
		queryTriplesWithRandomWeight = sampleWeights;
	}
	
	private void initWeights() throws IOException {
		setTripleWeights(Util.fetchAllSampleWeights(cwd, experimentSetup.getId()));
		setTripleRandomWeights(Util.getQueryTriplesWithRandomWeightFromFile(experimentSetup.getId()));
	}
	
	
	void calcRecallForSamples() throws IOException, InterruptedException {
		
//		ArrayList<SampleResults> allResults = new ArrayList<SampleResults>();
		int count = 0;
		
//		File weightedQueryTriplesDir = new File(cwd.getAbsolutePath() + "/" + Config.PATH_WEIGHTED_QUERY_TRIPLES + experimentSetup.getId());
		for (String sample: allSampleWeights.keySet()) {
			if (count >= maxSamples) break;
			count++;
			System.out.println("calculating recall for dataset " + experimentSetup.getId() + " and sample " + sample);
			if (isBaselineSample(sample)) {
				baselineResults.add(calcForQueries(sample, allSampleWeights.get(sample)));
			} else {
				regularResults.add(calcForQueries(sample, allSampleWeights.get(sample)));
			}
		}
//		for (File weightedQueryTriplesOfSample: weightedQueryTriplesDir.listFiles()) {
//		for (String sample: cutoffWeights.getCutoffWeights().keySet()) {
//			System.out.println(weightedQueryTriplesOfSample.getName());
			
//			if (!sample.equals("resourceContext_indegree")) continue;
//			System.out.println(sample);
			
//		}
	}
	
	private boolean isBaselineSample(String sampleName) {
		return sampleName.toLowerCase().contains("baseline");
	}
	void concatAndWriteOutput() throws IOException, InterruptedException {
		WriteAnalysis analysisOutput = new WriteAnalysis(experimentSetup, maxSampleSize);
		ArrayList<SampleResults> randomSampleResultsList = new ArrayList<SampleResults>();
		
		
		for (SampleResults results: baselineResults) {
			
			if (isRandomSample(results.getGraphName())) {
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
	
	private boolean isRandomSample(String sampleName) {
		return sampleName.toLowerCase().contains("random");
	}
	
	
	
//	private HashMap<String, Double> getQueryTriplesWithRandomWeightFromFile() throws IOException {
//		return fetchSampleWeights(new File(Config.PATH_RANDOM_WEIGHTED_QTRIPLES + experimentSetup.getId()));
//	}
//	public static HashMap<String, Double> fetchSampleWeights(File sampleFile) throws IOException {
//		
////		File sampleFile = new File(cwd.getAbsolutePath() + "/" + Config.PATH_WEIGHTED_QUERY_TRIPLES + experimentSetup.getId() + "/" + sample);
////		if (!sampleFile.exists()) throw new IOException("tried to locate " + sampleFile.getPath() + ", but it isnt there. Unable to calc recall");
//		HashMap<String, Double> sampleWeights = new HashMap<String, Double>();
//		String line;
//		BufferedReader br = new BufferedReader(new FileReader(sampleFile));
//		while ((line = br.readLine()) != null) {
//			if (line.length() > 0) {
//				String[] splitLine = line.split("\t");
//				StringBuilder triple = new StringBuilder();
//				Double weight = null;
//				for (int i = 0; i < (splitLine.length -1); i++) {
//					if (triple.length() > 0) triple.append("\t");
//					triple.append(splitLine[i]);
//				}
//				weight = Double.parseDouble(splitLine[splitLine.length - 1]);
//				sampleWeights.put(triple.toString(), weight);
//			}
//		}
//		br.close();
//		return sampleWeights;
//	}
	
	
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
	
	private SampleResults calcForQueries(String sample, Map<String, Double> map) throws IOException {
		SampleResults results = new SampleResultsRegular();
		results.setGraphName(sample);
		int count = 0;
		
		for (File queryDir: queryDirs) {
//			System.out.print(".");
//			System.out.println(queryDir.getName());System.exit(1);
//			if (!queryDir.getName().equals("query-1")) continue;
//			System.out.println(queryDir.getName());dd
			if (count > maxQueries) break;
			try {
				Double cutoffWeight;
				if (isRandomSample(sample)) {
					cutoffWeight = 1.0 - maxSampleSize;
				} else {
					cutoffWeight = cutoffWeights.get(sample);
					if (cutoffWeight == null) throw new IllegalStateException("wanted to calc recall for " + sample + " but it seems we do not have the calculated cutoff weights!");
				}
				Double cutoffSize = cutoffSizes.get(sample);
				Double cutoffWeightPlusOne = cutoffWeightsPlusOne.get(sample);
				Double cutoffSizePlusOne = cutoffSizesPlusOne.get(sample);
				Query query = CalcRecallForQuery.calc(
						experimentSetup, 
						sample, 
						map, 
						queryDir, 
						cutoffWeight, 
						cutoffSize, 
						cutoffWeightPlusOne, 
						cutoffSizePlusOne, 
						maxSampleSize, 
						totalSampleSize);

				query.setQueryId(count);
				results.add(query);
				count++;
			} catch (IllegalStateException e) {
				System.out.println(e.getMessage());
//				throw e;
			}
			
		}
		System.out.println("avg recall: " + results.getAverageRecall());
		results.setPercentage((cutoffSizes.get(sample) != null? cutoffSizes.get(sample): maxSampleSize));
		return results;
	}
	

	




	public void run() {
		try {
			calcRecallForSamples();
			concatAndWriteOutput();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, InterruptedException {
		File cwd = new File("");
		ExperimentSetup experimentSetup = null;
		Double maxSampleSize = 0.0;
		for (String arg: args) {
			System.out.println("arg: " + arg);
		}
		if (args.length > 0) {
			experimentSetup = ExperimentSetupHelper.get(args[0]);
			maxSampleSize = Double.parseDouble(args[1]);
			cwd = new File("/run/shm/");
			CalcCutoffWeight cutoffWeights = new CalcCutoffWeight(experimentSetup, cwd, maxSampleSize);
			cutoffWeights.calcForFiles();
			
	//		ExperimentSetup experimentSetup, double maxSampleSize, TreeMap<String, Double> cutoffWeights, TreeMap<String, Double> cutoffSizes, File cwd) throws IOExceptio
			CalcRecall calc = new CalcRecall(
					experimentSetup, 
					maxSampleSize, 
					cutoffWeights.getCutoffWeights(maxSampleSize), 
					cutoffWeights.getCutoffSizes(maxSampleSize), 
					cutoffWeights.getCutoffWeightsPlusOne(maxSampleSize), 
					cutoffWeights.getCutoffSizesPlusOne(maxSampleSize), 
					cutoffWeights.getTotalSampleSize(),  cwd);
	//		calc.maxSamples = 1;
	//		calc.maxQueries = 2;
			calc.initWeights();
			calc.calcRecallForSamples();
			calc.concatAndWriteOutput();
		} else {
			ExperimentSetup[] setups = {
//				new SwdfExperimentSetup(true),
				new ObmExperimentSetup(true),
			};
			
			
			for (ExperimentSetup setup: setups) {
				CalcCutoffWeight cutoffWeights = new CalcCutoffWeight(setup, cwd, maxSampleSize);
				cutoffWeights.calcForFiles();
				
//				System.out.println(cutoffWeights.getCutoffWeightsPlusOne(maxSampleSize));
//				System.exit(1);
		//		ExperimentSetup experimentSetup, double maxSampleSize, TreeMap<String, Double> cutoffWeights, TreeMap<String, Double> cutoffSizes, File cwd) throws IOExceptio
				CalcRecall calc = new CalcRecall(
						setup, 
						maxSampleSize, 
						cutoffWeights.getCutoffWeights(maxSampleSize), 
						cutoffWeights.getCutoffSizes(maxSampleSize), 
						cutoffWeights.getCutoffWeightsPlusOne(maxSampleSize), 
						cutoffWeights.getCutoffSizesPlusOne(maxSampleSize), 
						cutoffWeights.getTotalSampleSize(), 
						cwd);
		//		calc.maxSamples = 1;
		//		calc.maxQueries = 2;
				calc.calcRecallForSamples();
				calc.concatAndWriteOutput();
			}
		}
	}
}

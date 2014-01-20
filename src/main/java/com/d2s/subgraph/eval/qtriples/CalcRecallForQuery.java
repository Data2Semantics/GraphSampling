package com.d2s.subgraph.eval.qtriples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

import com.d2s.subgraph.eval.experiments.ExperimentSetup;
import com.d2s.subgraph.eval.results.QueryResultsRegular;
import com.d2s.subgraph.queries.Query;

public class CalcRecallForQuery {
	
	private ExperimentSetup experimentSetup;
	private Query query;
	private Double cutoffWeight;
	private double recall = 0.0;
	private String sample;
	private HashMap<String, Double> sampleWeights;
	private File queryDir;
	private File[] querySolutionDirs;
	private int queryLimit = 0;
	public CalcRecallForQuery(ExperimentSetup experimentSetup, String sample, HashMap<String, Double> sampleWeights, File queryDir, Double cutoffWeight) throws IOException {
		this.experimentSetup = experimentSetup;
		this.cutoffWeight = cutoffWeight;
		this.sample = sample;
		this.sampleWeights = sampleWeights;
		this.queryDir = queryDir;
		File queryFile = new File(queryDir.getPath() + "/query.txt");
		if (!queryFile.exists()) throw new IOException("tried to locate " + queryFile.getPath() + ", but it isnt there. Unable to calc recall");
		
		
		
		query = Query.create(FileUtils.readFileToString(queryFile));
		queryLimit = (int) query.getLimit();
		fetchQuerySolutionDirs();
	}
	
//	public void setQueryId(int queryId) {
//		query.setQueryId(queryId);
//	}
	
	private void fetchQuerySolutionDirs() {
		File[] qsDirs = queryDir.listFiles();
		
		ArrayList<File> filteredQsDirs = new ArrayList<File>();
		for (File file: qsDirs) {
			if (file.getName().startsWith("qs")) {
				filteredQsDirs.add(file);
			}
		}
		querySolutionDirs = filteredQsDirs.toArray(new File[filteredQsDirs.size()]);
		if (querySolutionDirs.length == 0) throw new IllegalStateException("could not find query solutions to calc recall for. Searching in dir " + queryDir.getPath());
	}
	
	
	public void run() throws NumberFormatException, IOException {
		int correctCount = 0;
		int incorrectCount = 0; 
//		System.out.println(queryDir.getPath());
		for (File qsDir: querySolutionDirs) {
			if (queryLimit > 0 && correctCount > queryLimit) {
				recall = 1;
			}
			boolean qsInSample = isQuerySolutionInSample(qsDir);
			if (qsInSample) {
				correctCount++;
			} else {
				incorrectCount++;
			}
		}
		
		//first take into account the query limit.
		if (queryLimit > 0) {
			int maxNumberOfIncorrect = (queryLimit - correctCount);
			incorrectCount = Math.min(maxNumberOfIncorrect, incorrectCount);
		}
		if (correctCount > 0 || incorrectCount > 0) {
			recall = correctCount / (correctCount + incorrectCount);
			QueryResultsRegular results = new QueryResultsRegular();
			results.setRecall(recall);
			query.setResults(results);
		} else {
			System.out.println("no query triples found for query " + queryDir.getPath());
		}
	}	
	private boolean isQuerySolutionInSample(File qsDir) throws NumberFormatException, IOException {
		boolean querySolutionOk = checkRequiredFile(qsDir);
		if (querySolutionOk) {
			//todo: check optionals
			querySolutionOk = checkUnionFiles(qsDir);
		}
		return querySolutionOk;
	}
	
	private boolean checkRequiredFile(File qsDir) throws NumberFormatException, IOException {
		boolean requiredTriplesInSample = true;
		File reqFile = new File(qsDir.getPath() + "/required");
		if (reqFile.exists()) {
			String line;
			BufferedReader br = new BufferedReader(new FileReader(reqFile));
			while ((line = br.readLine()) != null) {
				if (line.length() > 0) {
					Double weight = sampleWeights.get(line);
					if (weight != null) {
						if (weight > cutoffWeight) {
							//continue checking other triples
						} else {
							//1 of our required files would not be in the sample.
							//no need checking the others. stop!
							requiredTriplesInSample = false;
							break;
						}
					} else {
						br.close();
						throw new IllegalStateException("tried to find sample weight for triple " + line + " from file " + reqFile.getPath() + ", but could not match them!");
					}
				}
			}
			br.close();
		} else {
//			System.out.println("notice: required file does not exist for querysolutiondir " + qsDir.getPath());
		}
		
		return requiredTriplesInSample;
	}
	
	private boolean checkTriplesFile(File triplesFile) throws IOException {
		boolean checkOk = true;
		if (triplesFile.exists()) {
			String line;
			BufferedReader br = new BufferedReader(new FileReader(triplesFile));
			while ((line = br.readLine()) != null) {
				if (line.length() > 0) {
					Double weight = sampleWeights.get(line);
					if (weight != null) {
						if (weight > cutoffWeight) {
							//continue checking other triples
						} else {
							//1 of our required files would not be in the sample.
							//no need checking the others. stop!
							checkOk = false;
							break;
						}
					} else {
						br.close();
						throw new IllegalStateException("tried to find sample weight for triple " + line + " from file " + triplesFile.getPath() + ", but could not match them!");
					}
				}
			}
			br.close();
		} else {
			System.out.println("notice: required file does not exist for querysolutiondir " + triplesFile.getPath());
		}
		
		return checkOk;
	}
	private boolean checkUnionFiles(File qsDir) throws NumberFormatException, IOException {
		boolean checkOk = true;
		File unionDir = new File(qsDir.getPath() + "/unions");
		if (unionDir.exists()) {
			ArrayList<File> unionFiles = new ArrayList<File>(Arrays.asList(unionDir.listFiles()));
			//group them. there might be a hierarchy of unions. For each union blocks with the same hierarchy, at least 1 should exist!
			TreeMap<Integer, ArrayList<File>> groupedUnionFiles = new TreeMap<Integer, ArrayList<File>>(); 
			for (File unionFile: unionFiles) {
				String[] splitName = unionFile.getName().split("_");
				int depth = Integer.parseInt(splitName[0]);
//				int blockId = Integer.parseInt(splitName[1]);
				if (!groupedUnionFiles.containsKey(depth)) {
					groupedUnionFiles.put(depth, new ArrayList<File>());
				}
				groupedUnionFiles.get(depth).add(unionFile);
			}
			
			for (int depth: groupedUnionFiles.keySet()) {
				ArrayList<File> unionFilesWithSameDepth = groupedUnionFiles.get(depth);
				checkOk = checkUnionFiles(unionFilesWithSameDepth);
			}
		}
		
		return checkOk;
	}
	private boolean checkUnionFiles(ArrayList<File> unionFiles) throws NumberFormatException, IOException {
		boolean checkOk = false;
		
		for (File unionFile: unionFiles) {
			checkOk = checkTriplesFile(unionFile);
			if (checkOk) break;//we just need 1 to be ok!
		}
		return checkOk;
	}
	
	
//	private boolean isTripleIncluded(String triple) {
//		return sampleWeights.get(triple) > cutoffWeight;
//	}
	public static Query calc(ExperimentSetup experimentSetup, String sample, HashMap<String, Double> sampleWeights, File queryDir, Double cutoffWeight) throws IOException {
		CalcRecallForQuery calc = new CalcRecallForQuery(experimentSetup, sample, sampleWeights, queryDir, cutoffWeight);
		calc.run();
		return calc.query;
	}

	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
//		CalcRecallForQuery calc = new CalcRecallForQuery(new SwdfExperimentSetup(true, true), 0.5);
//		calc.calcRecallForSamples();
	}




}

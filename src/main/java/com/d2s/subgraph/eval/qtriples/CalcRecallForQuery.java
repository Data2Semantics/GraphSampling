package com.d2s.subgraph.eval.qtriples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

import com.d2s.subgraph.eval.experiments.Bio2RdfExperimentSetup;
import com.d2s.subgraph.eval.experiments.ExperimentSetup;
import com.d2s.subgraph.eval.experiments.SwdfExperimentSetup;
import com.d2s.subgraph.eval.results.QueryResultsRegular;
import com.d2s.subgraph.queries.Query;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class CalcRecallForQuery {
	
	private ExperimentSetup experimentSetup;
	private Query query;
	private Double cutoffWeight;
	private String sample;
	private Map<String, Double> sampleWeights;
	private File queryDir;
	private boolean verbose = false;
	private File[] querySolutionDirs;
	private int queryLimit = 0;
	private Double cutoffSize;
	private Double maxSampleSize;
	private int totalSampleSize;
	private Double cutoffSizePlusOne;
	private Double cutoffWeightPlusOne;
	public CalcRecallForQuery(ExperimentSetup experimentSetup, String sample, Map<String, Double> map, File queryDir, Double cutoffWeight, Double cutoffSize, Double cutoffWeightPlusOne, Double cutoffSizePlusOne, Double maxSampleSize, int totalSampleSize) throws IOException {
		this.experimentSetup = experimentSetup;
		this.cutoffSize = cutoffSize;
		this.cutoffWeight = cutoffWeight;
		this.cutoffSizePlusOne = cutoffSizePlusOne;
		this.cutoffWeightPlusOne = cutoffWeightPlusOne;
		this.maxSampleSize = maxSampleSize;
		this.totalSampleSize = totalSampleSize;
		this.sample = sample;
		this.sampleWeights = map;
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
		Double recall = null;
		int correctCount = 0;
		int incorrectCount = 0; 
//		System.out.println(queryDir.getPath());
		for (File qsDir: querySolutionDirs) {
			if (verbose) System.out.println("processing querysolution " + qsDir.getName());
			if (queryLimit > 0 && correctCount > queryLimit) {
				recall = 1.0;
				break;
			}
			boolean qsInSample = isQuerySolutionInSample(qsDir);
			if (qsInSample) {
//				if (verbose) System.out.println(qsDir.getName());
				correctCount++;
			} else {
				incorrectCount++;
			}
		}
		if (verbose) System.out.println("correct: " + correctCount + ", incorrect: " + incorrectCount);
		//first take into account the query limit.
		if (queryLimit > 0) {
			int maxNumberOfIncorrect = (queryLimit - correctCount);
			incorrectCount = Math.min(maxNumberOfIncorrect, incorrectCount);
		}
		
		if (correctCount > 0 || incorrectCount > 0) {
			if (recall == null) recall = (double)correctCount / (double)(correctCount + incorrectCount);
			if (verbose) System.out.println("recall: " + recall);
			QueryResultsRegular results = new QueryResultsRegular();
			results.setRecall(recall);
			query.setResults(results);
		} else {
			System.out.println("no query triples found for query " + queryDir.getPath());
		}
	}	
	
	/**
	 * Each query solution might have several 'collapsed' query solutions. There may be several query solutions to our distinct * query, which are each able to answer
	 * a single query solution from the original query (e.g. when that query uses distinct icw projection vars). In that case we only need 1 of the query solutions from our distinct * query!
	 * Therefore, we need to differentiate between some query solutions. We do that by storing them in separate 'collapsed' dirs
	 * So, whenever one of these 'collapsed' dirs match, we mark this query solution as 'ok'!
	 * @return
	 */
	private boolean isQuerySolutionInSample(File qsDir) throws NumberFormatException, IOException {
		boolean querySolutionOk = false;
//		System.out.println(qsDir.getPath());
		for (File collapsedDir: qsDir.listFiles()) {
			if (!collapsedDir.getName().startsWith("collapsed")) throw new IllegalStateException("expected a set of collapsed dirs in this path. but found: " + collapsedDir.getPath());
			if (!validateCollapsedDir(collapsedDir)) throw new IllegalStateException("could not find triple files in qs dir: " + qsDir.getPath());
//			System.out.print("+");
			if (querySolutionOk) {
//				System.out.println("match!");
				break;//we found 1 match, stop!
			}
			querySolutionOk = checkRequiredFile(collapsedDir);
			if (querySolutionOk) {
				querySolutionOk = checkUnionFiles(collapsedDir);
			}
			
		}
		return querySolutionOk;
	}
	
	private boolean validateCollapsedDir(File qsDir) {
		boolean valid = new File(qsDir, "required").exists();
		if (!valid) {
			File unionDir = new File(qsDir, "unions");
			if (unionDir.exists()) {
				valid = unionDir.listFiles().length > 0;
			}
		}
		return valid;
	}

	private boolean checkRequiredFile(File qsDir) throws NumberFormatException, IOException {
		
		File reqFile = new File(qsDir.getPath() + "/required");
		return checkTriplesFile(reqFile, true);
	}
	private boolean checkTriplesFile(File triplesFile) throws IOException {
		return checkTriplesFile(triplesFile, false);
	}
	private boolean checkTriplesFile(File triplesFile, boolean skipNotice) throws IOException {
		boolean triplesFileInSample = true;
		if (triplesFile.exists()) {
			String line;
			BufferedReader br = new BufferedReader(new FileReader(triplesFile));
			while ((line = br.readLine()) != null) {
				if (line.length() > 0) {
					Double weight = sampleWeights.get(line);
					if (weight != null) {
//						if (weight == 1.0 && sample.toLowerCase().contains("path_indegree")) {
//							System.out.println("heyyy!!!");
//						}
						if (weight >= cutoffWeight) {
//							//this triple has a weight which is high enough, and should be included in our sample! continue checking other triples
							continue;
						} 
						if (cutoffSize != null && cutoffSize < maxSampleSize && !sample.toLowerCase().contains("random")) {
//							System.out.println(cutoffWeight + " vs. " + cutoffWeightPlusOne);
							//ok, so we have a lower cutoff size than expected (as the triples just above this cutoff size have the same weight)
							//we want smooth results, so we want to simulate creating a sample of size 'maxSampleSize'
							//therefore, we need to check whether this triple has the weight which has been cut off. If it has, we randomly select some of these triples
							if ((double)weight == (double)cutoffWeightPlusOne) {
//								System.out.println("yes?");
//								System.out.println(line);
//								System.exit(1);
								double bridgeSizeGaph = maxSampleSize - cutoffSize;//7
								//how big is this gap wrt to number of triples for this cutoff weight?
								double sizeWithPlusOneWeight = cutoffSizePlusOne - cutoffSize;//9
								
								double randomCutoff = bridgeSizeGaph / sizeWithPlusOneWeight;
								double randomWeight = CalcRecall.queryTriplesWithRandomWeight.get(line);
								if (randomWeight < randomCutoff) {
									//fine, continue checking others
									continue;
								}
							}
						}
						//1 of our required files would not be in the sample.
						//no need checking the others. stop!
						triplesFileInSample = false;
						break;
						
					} else {
//						br.close();
//						throw new IllegalStateException("tried to find sample weight for triple " + line + " from file " + triplesFile.getPath() + ", but could not match them!");
						System.out.println("found no weight for triple " + line);
						System.out.println(triplesFile.getPath());
						triplesFileInSample = false;
					}
				}
			}
			br.close();
		} else {
			if (!skipNotice) System.out.println("notice: file does not exist for querysolutiondir " + triplesFile.getPath());
		}
		
		return triplesFileInSample;
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
	public static Query calc(ExperimentSetup experimentSetup, String sample, Map<String, Double> map, File queryDir, Double cutoffWeight, Double cutoffSize, Double cutoffWeightPlusOne, Double cutoffSizePlusOne, double maxSampleSize, int totalSampleSize) throws IOException {
		CalcRecallForQuery calc = new CalcRecallForQuery(experimentSetup, sample, map, queryDir, cutoffWeight, cutoffSize, cutoffWeightPlusOne, cutoffSizePlusOne, maxSampleSize, totalSampleSize);
		calc.run();
		return calc.query;
	}

	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
		ExperimentSetup experimentSetup = new Bio2RdfExperimentSetup(true);
//		CalcCutoffWeight cutoffWeights = new CalcCutoffWeight(experimentSetup, 0.5, new File(""));
//		cutoffWeights.calcForFiles();
		File queryDir = new File("output/queryTriples/bio2rdf/query-2");
		
		CalcRecallForQuery calc = new CalcRecallForQuery(
				experimentSetup, 
				"resourceWithoutLit_outdegree", 
				Util.fetchSampleWeights(new File("input/qTripleWeights/bio2rdf/resourceWithoutLit_outdegree")), 
				queryDir, 
				1380.0,
				0.5,
				(double) 1381,
				0.51,
				0.5,
				11000010);
		calc.verbose = true;
		calc.run();
//		System.out.println()
	}




}

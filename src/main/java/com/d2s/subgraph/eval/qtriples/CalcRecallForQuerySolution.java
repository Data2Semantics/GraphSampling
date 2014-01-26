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

import org.xml.sax.SAXException;

public class CalcRecallForQuerySolution {
	
	File qsDir;
	private int requiredTriplesCorrect = 0;
	private int requiredTriplesIncorrect = 0;
	
	private int unionBlocksCorrect = 0;
	private int unionBlocksIncorrect = 0;
	
	private int optionalBlocksCorrect = 0;
	private int optionalBlocksIncorrect = 0;
	private HashMap<String, Double> sampleWeights;
	private Double cutoffWeight;
	
	private boolean verbose = false;
	public CalcRecallForQuerySolution(File qsDir, HashMap<String, Double> sampleWeights, Double cutoffWeight) {
		this.qsDir = qsDir;
		this.sampleWeights = sampleWeights;
		this.cutoffWeight = cutoffWeight;
	}
	
	private Double getRecall() throws NumberFormatException, IOException {
		double recall = 0.0;
		checkRequiredFile();
		if (requiredTriplesIncorrect == 0) {
			checkUnionFiles();
			getOptionalRecall();
			
//			recall = 
			
		} else {
//			if (verbose) System.out.println("req file check failed (" + collapsedDir.getPath() + ")");
		}
		return recall;
	}
	public static Double getRecallForQs(File qsDir, HashMap<String, Double> sampleWeights, Double cutoffWeight) throws NumberFormatException, IOException {
		CalcRecallForQuerySolution calc = new CalcRecallForQuerySolution(qsDir, sampleWeights, cutoffWeight);
		return calc.getRecall();
	}
	private boolean checkRequiredFile() throws NumberFormatException, IOException {
		boolean requiredTriplesInSample = true;
		File reqFile = new File(qsDir.getPath() + "/required");
		if (reqFile.exists()) {
			String line;
			BufferedReader br = new BufferedReader(new FileReader(reqFile));
			boolean lineRead = false;
			while ((line = br.readLine()) != null) {
				if (line.length() > 0) {
					lineRead = true;
					Double weight = sampleWeights.get(line);
					if (weight != null) {
						if (weight >= cutoffWeight) {
							if (verbose) System.out.print("+");
							//continue checking other triples
						} else {
							if (verbose) System.out.print("-");
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
			if (!lineRead) throw new IllegalStateException("tried to read triples from file " + reqFile.getPath() + ", but it contains none!");
			
		} else {
//			System.out.println("notice: required file does not exist for querysolutiondir " + qsDir.getPath());
		}
		
		return requiredTriplesInSample;
	}
	
	private void checkUnionFiles() throws NumberFormatException, IOException {
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
				checkUnionFiles(unionFilesWithSameDepth);
			}
		}
	}
	private void getOptionalRecall() throws NumberFormatException, IOException {
		File optionalDir = new File(qsDir.getPath() + "/optional");
		if (optionalDir.exists()) {
			ArrayList<File> optionalFiles = new ArrayList<File>(Arrays.asList(optionalDir.listFiles()));
			for (File optionalFile: optionalFiles) {
				if (checkTriplesFile(optionalFile)) {
					optionalBlocksCorrect++;
				} else {
					optionalBlocksCorrect++;
				}
			}
		}
//		return (double)correctOptionalBlocks / ((double)incorrectOptionalBlocks + (double)correctOptionalBlocks);
	}
	
	private boolean checkTriplesFile(File triplesFile) throws IOException {
		boolean checkOk = true;
		if (triplesFile.exists()) {
			String line;
			BufferedReader br = new BufferedReader(new FileReader(triplesFile));
			boolean lineRead = false;
			while ((line = br.readLine()) != null) {
				if (line.length() > 0) {
					lineRead = true;
					Double weight = sampleWeights.get(line);
					if (weight != null) {
						
						if (weight >= cutoffWeight) {
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
			if (!lineRead) throw new IllegalStateException("tried to read triples from file " + triplesFile.getPath() + ", but it contains none!");
		} else {
			System.out.println("notice: required file does not exist for querysolutiondir " + triplesFile.getPath());
		}
		
		return checkOk;
	}
	
	private void checkUnionFiles(ArrayList<File> unionFiles) throws NumberFormatException, IOException {
		for (File unionFile: unionFiles) {
			checkTriplesFile(unionFile);
		}
	}

	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
	}




}

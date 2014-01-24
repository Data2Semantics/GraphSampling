package com.d2s.subgraph.eval.qtriples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

import com.d2s.subgraph.eval.Config;
import com.d2s.subgraph.eval.experiments.ExperimentSetup;
import com.d2s.subgraph.eval.experiments.SwdfExperimentSetup;

public class CalcCutoffWeight {
	private Double maxSampleSize;
	@SuppressWarnings("unused")
	private ExperimentSetup experimentSetup;
	
	//sample, where each val is a treemap which contains the cutoffsizes and cutoff weights
//	private TreeMap<String, TreeMap<Double, Double>> cutoffs = new TreeMap<String, TreeMap<Double, Double>>();
	
	
	private TreeMap<Double, TreeMap<String, Double>> cutoffWeights = new TreeMap<Double, TreeMap<String, Double>>();
	private TreeMap<Double, TreeMap<String, Double>> cutoffSizes = new TreeMap<Double, TreeMap<String, Double>>();
//	private TreeMap<String, Double> cutoffWeights = new TreeMap<String, Double>();
//	private TreeMap<String, Double> cutoffSizes = new TreeMap<String, Double>();
	private File weightDistDir;
	private boolean verbose = true;
	
	public CalcCutoffWeight(ExperimentSetup experimentSetup, File cwd) throws IOException {
		this(experimentSetup, cwd, null);
	}
	public NavigableSet<Double> getMaxCutoffOptions() {
		return cutoffWeights.descendingKeySet();
	}
	
	
	public CalcCutoffWeight(ExperimentSetup experimentSetup, File cwd, Double maxSampleSize) throws IOException {
		this.experimentSetup = experimentSetup;
		this.maxSampleSize = maxSampleSize;
		
		weightDistDir = new File(cwd.getAbsolutePath() + "/" + Config.PATH_WEIGHT_DISTRIBUTION + experimentSetup.getId());
		if (!weightDistDir.exists()) throw new IOException("path to get weight distribution from does not exist: " + weightDistDir.getPath());
	}
	
	public void calcForFiles() throws IOException {
		for (File distFile: FileUtils.listFiles(weightDistDir, null, false)) {
			if (verbose) System.out.println("calcing cutoff weight for " + distFile.getName());
			calcCutoff(distFile);
		}
	}
	
	public TreeMap<String, Double> getCutoffWeights(double maxCutoff) {
		return cutoffWeights.get(maxCutoff);
	}
	public TreeMap<String, Double> getCutoffSizes(double maxCutoff) {
		return cutoffSizes.get(maxCutoff);
	}
	
	private void calcCutoff(File file) throws IOException {
		System.out.println(maxSampleSize);
		System.exit(1);
		if (maxSampleSize != null) {
			calcSingleCutoff(file);
		} else {
			calcMultipleCutoffs(file);
		}
		
	}
	
	private TreeMap<Double, Integer> readWeightDistribution(File file) throws IOException {
		TreeMap<Double, Integer> dist = new TreeMap<Double, Integer>();
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		//load file in memory
		while ((line = br.readLine()) != null) {
			if (line.length() > 0) {
				String[] fields = line.split("\t");
				if (fields.length < 2) {
					br.close();
					throw new IOException("tried to get size from weight dist file, but unable to detect # triples for this weight, as we couldnt split the line by tab: " + line);
				}
				
				double weight = Double.parseDouble(fields[0]);
				int count = Integer.parseInt(fields[1]);
				dist.put(weight, count);
			}
		}
		br.close();
		return dist;
	}
	
	private int getTotalSampleSize(Map<Double, Integer> weightDistribution) {
		int totalSize = 0;
		for (Integer size: weightDistribution.values()) {
			totalSize += size;
		}
		if (verbose) System.out.println("totalsize triples based on query dist: " + totalSize);
		return totalSize;
	}
	

	private void calcMultipleCutoffs(File file) throws IOException {
		TreeMap<Double, Integer> dist = readWeightDistribution(file);
		int totalSize = getTotalSampleSize(dist);
		
		//stepSizes of 1%
		
		int stepSize = (int) Math.round((double)totalSize / (double)100);
		if (verbose) System.out.println("step sizes (i.e. 1% of total): " + stepSize);
		
		//sort by weight
		NavigableSet<Double> weights = dist.descendingKeySet();
//		if (verbose) System.out.println("looping through " + weights.size() + " weights");
		
		int totalSampleSizeSoFar = 0;
		Double previousWeight = 0.0;
		int stepsDone = 0;
		for (Double weight: weights) {
			int tripleNumWithWeight = dist.get(weight);
			//if difference with last cutoff is larger than our stepsize, we want to add this one as a cutoff
			if (totalSampleSizeSoFar + tripleNumWithWeight > (stepSize * stepsDone)) {
				double maxCutoffOfThisIteration = (double)stepsDone / (double)100.0;
				addCutoffSize(file.getName(), maxCutoffOfThisIteration, (double)totalSampleSizeSoFar / (double)totalSize);
				addCutoffWeight(file.getName(), maxCutoffOfThisIteration, previousWeight);
			}
			totalSampleSizeSoFar += tripleNumWithWeight;
			System.out.println(totalSampleSizeSoFar);
			stepsDone ++;
			previousWeight = weight;
		}
		
		
//		System.out.println(cutoffWeights);
	}
	
	private void addCutoffWeight(String sample, Double maxSampleSize, Double weight) {
		TreeMap<String, Double> cutoffWeightsForMaxSampleSize = cutoffWeights.get(maxSampleSize);
		if (cutoffWeightsForMaxSampleSize == null) {
			cutoffWeightsForMaxSampleSize = new TreeMap<String, Double>();
			cutoffWeights.put(maxSampleSize, cutoffWeightsForMaxSampleSize);
		}
		cutoffWeightsForMaxSampleSize.put(sample, weight);
	}
	private void addCutoffSize(String sample, Double maxSampleSize, Double size) {
		TreeMap<String, Double> cutoffSizesForMaxSampleSize = cutoffSizes.get(maxSampleSize);
		if (cutoffSizesForMaxSampleSize == null) {
			cutoffSizesForMaxSampleSize = new TreeMap<String, Double>();
			cutoffSizes.put(maxSampleSize, cutoffSizesForMaxSampleSize);
		}
		cutoffSizesForMaxSampleSize.put(sample, size);
	}
	
	private void calcSingleCutoff(File file) throws IOException {
		TreeMap<Double, Integer> dist = readWeightDistribution(file);
		int totalSize = getTotalSampleSize(dist);
		
		int cutoffSize = (int) Math.round(totalSize * maxSampleSize);
		if (verbose) System.out.println("max size triples for cutoff " + cutoffSize);
		//sort by weight
		NavigableSet<Double> weights = dist.descendingKeySet();
		int previousSampleSize = 0;
		Double previousWeight = null;
		for (Double weight: weights) {
			int tripleNumWithWeight = dist.get(weight);
			if ((previousSampleSize + tripleNumWithWeight) > cutoffSize) {
				//ah, we don't want a bigger sample, but we'd prefer to be on the safe side
//				cutoffWeights.put(file.getName(), previousWeight);
				addCutoffSize(file.getName(), maxSampleSize, (double)previousSampleSize / (double)totalSize);
				addCutoffWeight(file.getName(), maxSampleSize, previousWeight);
				if (verbose) {
					System.out.println("we reached triple " + (previousSampleSize + tripleNumWithWeight) + " now. we should break!");
					System.out.println(cutoffWeights);
					System.out.println(cutoffSizes);
				}
				return;
			} else {
				previousSampleSize += tripleNumWithWeight;
				previousWeight = weight;
			}
		}
		throw new IOException("unable to find cutoff weight based on weight distribution: "  + file.getPath());
	}


	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
		CalcCutoffWeight cutoffWeights = new CalcCutoffWeight(new SwdfExperimentSetup(true, true), new File(""), 0.5);
		cutoffWeights.calcCutoff(new File("input/weightDistribution/swdf/resourceContext_indegree"));
	}
}

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
//		if (this.maxSampleSize != null) {
//			TreeMap<String, Double> cutoffWeightsForSampleSize = new TreeMap<String, Double>();
//			cutoffWeights.put(maxSampleSize, cutoffWeightsForSampleSize);
//			TreeMap<String, Double> cutoffSizesForSampleSize = new TreeMap<String, Double>();
//			cutoffSizes.put(maxSampleSize, cutoffSizesForSampleSize);
//		}
		
		weightDistDir = new File(cwd.getAbsolutePath() + "/" + Config.PATH_WEIGHT_DISTRIBUTION + experimentSetup.getId());
		if (!weightDistDir.exists()) throw new IOException("path to get weight distribution from does not exist: " + weightDistDir.getPath());
	}
	
	public void calcForFiles() throws IOException {
		for (File distFile: FileUtils.listFiles(weightDistDir, null, false)) {
			if (verbose) System.out.println("calcing cutoff weight for " + distFile.getName());
//			if (!distFile.getName().toLowerCase().contains("random")) continue;
			calcCutoff(distFile);
		}
	}
//	public int getDatasetSize() {
//		return this.totalSize;
//	}
	
//	public TreeMap<String, Double> getCutoffWeight(String sample) {
//		return cutoffWeights;
//	}
//	
//	public Double getCutoffSize(String sample) {
//		return cutoffSizes.get(name);
//	}
	
//	public TreeMap<String, TreeMap<Double, Double>> getCutoffs() {
//		return cutoffs;
//	}
	
//	private TreeMap<String, Double> getSingleCutoffInfo(boolean weightInfo) {
//		TreeMap<String, Double> singleCutoffInfo = new TreeMap<String, Double>();
//		for (String sample: cutoffs.keySet()) {
//			TreeMap<Double, Double> weightAndSizesForSample = cutoffs.get(sample);
//			if (weightAndSizesForSample.size() > 1) {
//				throw new IllegalStateException("we want to fetch just a single cutoff, but more than one has been calculated!");
//			}
//			//we just want the first value
//			if (weightInfo) {
//				singleCutoffInfo.put(sample, weightAndSizesForSample.firstKey());
//			} else {
//				//we want to calculated size, not the weight
//				singleCutoffInfo.put(sample, weightAndSizesForSample.get(weightAndSizesForSample.firstKey()));
//			}
//		}
//		
//		return singleCutoffInfo;
//	}
	
//	public TreeMap<String, Double> getSingleCutoffWeights() {
//		return getSingleCutoffInfo(true);
//	}
//	public TreeMap<String, Double> getSingleCutoffSizes() {
//		return getSingleCutoffInfo(false);
//	}
	
	public TreeMap<String, Double> getCutoffWeights(double maxCutoff) {
		return cutoffWeights.get(maxCutoff);
	}
	public TreeMap<String, Double> getCutoffSizes(double maxCutoff) {
		return cutoffSizes.get(maxCutoff);
	}
	
	private void calcCutoff(File file) throws IOException {
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
//		TreeMap<Double, Double> cutoffSizesForThisSample = new TreeMap<Double, Double>();
//		cutoffs.put(file.getName(), cutoffSizesForThisSample);
		int totalSize = getTotalSampleSize(dist);
		
		//stepSizes of 1%
		
		int stepSize = (int) Math.round((double)totalSize / (double)100);
		if (verbose) System.out.println("step sizes (i.e. 1% of total): " + stepSize);
		
		//sort by weight
		NavigableSet<Double> weights = dist.descendingKeySet();
		if (verbose) System.out.println("looping through " + weights.size() + " weights");
		
//		ArrayList<Integer> sampleSizes = new ArrayList<Integer>();
		int totalSampleSizeSoFar = 0;
		Double previousWeight = 0.0;
		int stepsDone = 0;
		for (Double weight: weights) {
			int tripleNumWithWeight = dist.get(weight);
//			System.out.println("totalSampleSizeSoFar: " + totalSampleSizeSoFar);
//			System.out.println("tripleNumWithWeight: " +tripleNumWithWeight);
//			System.out.println("stepsDone: " +stepsDone);
//			System.out.println("stepSize: " +stepSize);
			
			//if difference with last cutoff is larger than our stepsize, we want to add this one as a cutoff
			if (totalSampleSizeSoFar + tripleNumWithWeight > (stepSize * stepsDone)) {
				double maxCutoffOfThisIteration = (double)stepsDone / (double)100.0;
				addCutoffSize(file.getName(), maxCutoffOfThisIteration, (double)totalSampleSizeSoFar / (double)totalSize);
				addCutoffWeight(file.getName(), maxCutoffOfThisIteration, previousWeight);
				
//				cutoffSizesForThisSample.put(weight, ((double)sizeSincePrevSample / (double)totalSize));
//				cutoffWeights.put(file.getName(), weight);
//				sizeSincePrevSample = 0;
			}
			totalSampleSizeSoFar += tripleNumWithWeight;
			stepsDone ++;
			previousWeight = weight;
		}
		System.out.println(cutoffWeights);
//		throw new IOException("unable to find cutoff weight based on weight distribution: "  + file.getPath());
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
//		TreeMap<Double, Double> cutoffSizeForThisSample = new TreeMap<Double, Double>();//we are looking for a single cutoff, so we'll only store one
//		cutoffs.put(file.getName(), cutoffSizeForThisSample);
		int totalSize = getTotalSampleSize(dist);
		
//		if (this.totalSize == 0){
//			this.totalSize = totalSize;
//		} else {
//			if (totalSize != this.totalSize) {
//				throw new IllegalStateException("the total dataset size fetched from the weight distribution differs between different samples! There must be something wrong. Previous one: " + this.totalSize + ", current one: " + totalSize);
//			}
//		}
		
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
//				cutoffWeights.get(maxSampleSize).put(file.getName(), previousWeight);
//				cutoffWeights.get(maxSampleSize).put(file.getName(), (double)previousSampleSize / (double)totalSize);
//				cutoffSizes.put(maxSampleSize, value)
				if (verbose) {
					System.out.println("we reached triple " + (previousSampleSize + tripleNumWithWeight) + " now. we should break!");
					System.out.println(cutoffWeights);
					System.out.println(cutoffSizes);
//					System.out.println("cutoff weight: " + previousWeight);
//					System.out.println("cutoff " + file.getName() + ": " +  (double)previousSampleSize / (double)totalSize);
				}
//				cutoffSizes.put(file.getName(), (double)previousSampleSize / (double)totalSize);
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
//		resourceContext_indegree
	}
}

package com.d2s.subgraph.eval.qtriples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
	private boolean verbose = false;
	
	public CalcCutoffWeight(ExperimentSetup experimentSetup, File cwd) throws IOException {
		this(experimentSetup, cwd, null);
	}
	public NavigableSet<Double> getMaxCutoffs() {
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
			if (!distFile.getName().startsWith(".")) {
				if (verbose) System.out.println("calcing cutoff weight for " + distFile.getName());
				
				calcCutoff(distFile);
			}
		}
	}
	
	public TreeMap<String, Double> getCutoffWeights(double maxCutoff) {
		return cutoffWeights.get(maxCutoff);
	}
	public TreeMap<String, Double> getCutoffSizes(double maxCutoff) {
		return cutoffSizes.get(maxCutoff);
	}
	
	private void calcCutoff(File file) throws IOException {
//		System.out.println(maxSampleSize);
//		System.exit(1);
		TreeMap<Double, Integer> dist = readWeightDistribution(file);
		int totalSize = getTotalSampleSize(dist);
		if (maxSampleSize != null) {
			calcSingleCutoff(file, dist, totalSize, maxSampleSize);
		} else {
			calcMultipleCutoffs(file, dist, totalSize);
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
				try {
					double weight = Double.parseDouble(fields[0]);
					int count = Integer.parseInt(fields[1]);
					dist.put(weight, count);
				} catch (Exception e) {
					System.out.println("could not parse line:");
					System.out.println(line);
					System.out.println(e.getMessage());
					
				}
				
			}
		}
		br.close();
		if (dist.size() == 0) {
			throw new IllegalStateException("we just loaded an empty weight distribution file. something is wrong. file: " + file.getName());
		}
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
	

	private void calcMultipleCutoffs(File file, TreeMap<Double, Integer> dist, int totalSize) throws IOException {
		//stepSizes of 1%
		for (int step = 0; step <= 100; step++) {
//			System.out.println("step: " + step);
			calcSingleCutoff(file, dist, totalSize, (double)step / (double)100.0);
		}
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
	
	private void calcSingleCutoff(File file, TreeMap<Double, Integer> dist, int totalSize, Double maxSampleSize) throws IOException {
		if (maxSampleSize == 1.0) {
			addCutoffSize(file.getName(), maxSampleSize, (double)totalSize);
			addCutoffWeight(file.getName(), maxSampleSize, 0.0);
			return;
		} else if (maxSampleSize == 0.0) {
			addCutoffSize(file.getName(), maxSampleSize, 0.0);
//			System.out.println("sample size of zero! Use weight: " + (dist.descendingKeySet().first() + 1.0));
			addCutoffWeight(file.getName(), maxSampleSize, dist.descendingKeySet().first() + 1.0);
			return;
		}
		
		int cutoffSize = (int) Math.round(totalSize * maxSampleSize);
		if (verbose) System.out.println("max size triples for cutoff " + cutoffSize);
		//sort by weight
		NavigableSet<Double> weights = dist.descendingKeySet();
		int previousSampleSize = 0;
		Double previousWeight = dist.descendingKeySet().first() + 1.0;
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
				if (file.getName().equals("resourceContext_indegree")) {
					System.out.println("size (context indegree): " + ((double)previousSampleSize / (double)totalSize));
					System.out.println("weight (context indegree): " + previousWeight);
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
		cutoffWeights.calcCutoff(new File("input/weightDistribution/obm/resourceContext_indegree"));
	}
}

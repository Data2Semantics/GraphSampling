package com.d2s.subgraph.eval.qtriples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.NavigableSet;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

import com.d2s.subgraph.eval.Config;
import com.d2s.subgraph.eval.experiments.ExperimentSetup;
import com.d2s.subgraph.eval.experiments.SwdfExperimentSetup;

public class CalcCutoffWeight {
	private double maxSampleSize;
	@SuppressWarnings("unused")
	private ExperimentSetup experimentSetup;
	private HashMap<String, Double> cutoffWeights = new HashMap<String, Double>();
	private HashMap<String, Double> cutoffSizes = new HashMap<String, Double>();
	
	private File weightDistDir;
	public CalcCutoffWeight(ExperimentSetup experimentSetup, double maxSampleSize) throws IOException {
		this.experimentSetup = experimentSetup;
		this.maxSampleSize = maxSampleSize;
		
		weightDistDir = new File(Config.PATH_WEIGHT_DISTRIBUTION + experimentSetup.getId());
		if (!weightDistDir.exists()) throw new IOException("path to get weight distribution from does not exist: " + weightDistDir.getPath());
	}
	
	public void calcForFiles() throws IOException {
		for (File distFile: FileUtils.listFiles(weightDistDir, null, false)) {
			calcCutoff(distFile);
//			System.out.println(distFile.getName() + ":" + cutoffWeights.get(distFile.getName()));
		}
	}
	
	public HashMap<String, Double> getCutoffWeights() {
		return cutoffWeights;
	}
	
	public Double getCutoffSize(String name) {
		return cutoffSizes.get(name);
	}
	
	private void calcCutoff(File file) throws IOException {
		TreeMap<Double, Integer> dist = new TreeMap<Double, Integer>();
		int totalSize = 0;
		
		
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
				totalSize += count;
				dist.put(weight, count);
			}
		}
		br.close();
		int cutoffSize = (int) Math.round(totalSize * maxSampleSize);
		//sort by weight
		NavigableSet<Double> weights = dist.descendingKeySet();
		int sampleSize = 0;
		Double previousWeight = null;
		for (Double weight: weights) {
			int weightCount = dist.get(weight);
			if ((sampleSize + weightCount) > cutoffSize) {
				//ah, we don't want a bigger sample, but we'd prefer to be on the safe side
				cutoffWeights.put(file.getName(), previousWeight);
				cutoffSizes.put(file.getName(), (double)sampleSize / (double)totalSize);
				return;
			} else {
				sampleSize += weightCount;
				previousWeight = weight;
			}
		}
		throw new IOException("unable to find cutoff weight based on weight distribution!");
	}
	
//	public static HashMap<String, Double> get(ExperimentSetup experimentSetup, double maxSize) throws IOException {
//		CalcCutoffWeight calc = new CalcCutoffWeight(experimentSetup, maxSize);
//		calc.calcForFiles();
//		return calc.cutoffWeights;
//	}
	
	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
//		get(new SwdfExperimentSetup(true, true), 0.5);
	}
}

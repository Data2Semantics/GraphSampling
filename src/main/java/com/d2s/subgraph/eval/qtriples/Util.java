package com.d2s.subgraph.eval.qtriples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.d2s.subgraph.eval.Config;

public class Util {
	public static Map<String, Double> getQueryTriplesWithRandomWeightFromFile(String experimentSetupId) throws IOException {
		return fetchSampleWeights(new File(Config.PATH_RANDOM_WEIGHTED_QTRIPLES + experimentSetupId));
	}
	
	public static Map<String, Map<String, Double>> fetchAllSampleWeights(File cwd, String experimentSetupId) throws IOException {
		return fetchAllSampleWeights(cwd, experimentSetupId, false);
		
	}
	public static Map<String, Map<String, Double>> fetchAllSampleWeights(File cwd, String experimentSetupId, boolean andDelete) throws IOException {
		HashMap<String, Map<String, Double>> allSampleWeights = new HashMap<String, Map<String, Double>>();
		File weightedQueryTriplesDir = new File(cwd.getAbsolutePath() + "/" + Config.PATH_WEIGHTED_QUERY_TRIPLES + experimentSetupId);
		for (File weightedQueryTriplesOfSample: weightedQueryTriplesDir.listFiles()) {
			
			allSampleWeights.put(weightedQueryTriplesOfSample.getName(), fetchSampleWeights(weightedQueryTriplesOfSample));
			if (andDelete) {
				System.out.println("deleting " + weightedQueryTriplesOfSample.getPath());
				weightedQueryTriplesOfSample.delete();
			}
		}
		return allSampleWeights;
	}
	public static Map<String, Double> fetchSampleWeights(File sampleFile) throws IOException {
		System.out.println("fetching qtriple weights for " + sampleFile.getName());
//		File sampleFile = new File(cwd.getAbsolutePath() + "/" + Config.PATH_WEIGHTED_QUERY_TRIPLES + experimentSetup.getId() + "/" + sample);
//		if (!sampleFile.exists()) throw new IOException("tried to locate " + sampleFile.getPath() + ", but it isnt there. Unable to calc recall");
		HashMap<String, Double> sampleWeights = new HashMap<String, Double>();
		String line;
		BufferedReader br = new BufferedReader(new FileReader(sampleFile));
		while ((line = br.readLine()) != null) {
			if (line.length() > 0) {
				String[] splitLine = line.split("\t");
				StringBuilder triple = new StringBuilder();
				for (int i = 0; i < (splitLine.length -1); i++) {
					if (triple.length() > 0) triple.append("\t");
					triple.append(splitLine[i]);
				}
				sampleWeights.put(triple.toString().intern(), Double.parseDouble(splitLine[splitLine.length - 1]));
			}
		}
		br.close();
		return sampleWeights;
	}
}

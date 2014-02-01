package com.d2s.subgraph.eval.qtriples;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.d2s.subgraph.eval.experiments.Bio2RdfExperimentSetup;
import com.d2s.subgraph.eval.experiments.ExperimentSetup;
import com.d2s.subgraph.eval.experiments.ExperimentSetupHelper;

public class CalcRecallForAllPossibleCutoffs {
	
	
	public static void calc(ExperimentSetup experimentSetup, File cwd) throws IOException, InterruptedException {
		CalcCutoffWeight cutoffWeights = new CalcCutoffWeight(experimentSetup, cwd);
		cutoffWeights.calcForFiles();
		Map<String, Map<String, Double>> allSampleWeights = Util.fetchAllSampleWeights(cwd, experimentSetup.getId());
		Map<String, Double> randomWeights = Util.getQueryTriplesWithRandomWeightFromFile(experimentSetup.getId());
		CalcRecall.setTripleRandomWeights(randomWeights);
		for (Double maxCutoff: cutoffWeights.getMaxCutoffs()) {
			CalcRecall calc = new CalcRecall(
					experimentSetup, 
					maxCutoff, 
					cutoffWeights.getCutoffWeights(maxCutoff), 
					cutoffWeights.getCutoffSizes(maxCutoff), 
					cutoffWeights.getCutoffWeightsPlusOne(maxCutoff), 
					cutoffWeights.getCutoffSizesPlusOne(maxCutoff), 
					cutoffWeights.getTotalSampleSize(), 
					cwd);
			calc.setTripleWeights(allSampleWeights);
			calc.calcRecallForSamples();
			calc.concatAndWriteOutput();
//			cutoffWeights.getCutoffWeights(maxCutoff);
//			CalcRecall calc = new CalcRecall(experimentSetup, maxCutoff, cutoffWeights.getCutoffWeights(maxCutoff), cutoffWeights.getCutoffSizes(maxCutoff), cwd);
////			calc.maxSamples = 1;
////			calc.maxQueries = 2;
//			calc.calcRecallForSamples();
//			calc.concatAndWriteOutput();
		}
	}
	
	
	

	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, InterruptedException {
		File cwd = new File("");
		ExperimentSetup experimentSetup = null;
		if (args.length > 0) {
			experimentSetup = ExperimentSetupHelper.get(args[0]);
			calc(experimentSetup, cwd);
		} else {
			ExperimentSetup[] setups = {
//				new SwdfExperimentSetup(true),
				new Bio2RdfExperimentSetup(true),
			};
			
			for (ExperimentSetup setup: setups) {
				calc(setup, cwd);
			}
		}
	}
}

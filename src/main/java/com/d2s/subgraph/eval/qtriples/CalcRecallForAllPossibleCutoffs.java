package com.d2s.subgraph.eval.qtriples;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.d2s.subgraph.eval.experiments.ExperimentSetup;
import com.d2s.subgraph.eval.experiments.SwdfExperimentSetup;

public class CalcRecallForAllPossibleCutoffs {
	
	
	public static void calc(ExperimentSetup experimentSetup, File cwd) throws IOException, InterruptedException {
		CalcCutoffWeight cutoffWeights = new CalcCutoffWeight(experimentSetup, cwd);
		cutoffWeights.calcForFiles();
		
		for (Double maxCutoff: cutoffWeights.getMaxCutoffOptions()) {
			System.out.println(maxCutoff);
//			CalcRecall calc = new CalcRecall(experimentSetup, maxCutoff, cutoffWeights.getCutoffWeights(maxCutoff), cutoffWeights.getCutoffSizes(maxCutoff), cwd);
////			calc.maxSamples = 1;
////			calc.maxQueries = 2;
//			calc.calcRecallForSamples();
//			calc.concatAndWriteOutput();
		}
	}
	
	
	

	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, InterruptedException {
		File cwd = new File("");
		ExperimentSetup experimentSetup= new SwdfExperimentSetup(true);
		calc(experimentSetup, cwd);
		
//		ExperimentSetup experimentSetup, double maxSampleSize, TreeMap<String, Double> cutoffWeights, TreeMap<String, Double> cutoffSizes, File cwd) throws IOExceptio
//		CalcRecallForAllPossibleCutoffs calc = new CalcRecallForAllPossibleCutoffs(experimentSetup, maxSampleSize, cutoffWeights.getSingleCutoffWeights(), cutoffWeights.getSingleCutoffSizes(),  cwd);
//		calc.maxSamples = 1;
//		calc.maxQueries = 2;
//		calc.calcRecallForSamples();
//		calc.concatAndWriteOutput();
	}
}

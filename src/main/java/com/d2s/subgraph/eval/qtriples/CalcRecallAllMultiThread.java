package com.d2s.subgraph.eval.qtriples;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.d2s.subgraph.eval.experiments.Bio2RdfExperimentSetup;
import com.d2s.subgraph.eval.experiments.ExperimentSetup;
import com.d2s.subgraph.eval.experiments.ExperimentSetupHelper;

public class CalcRecallAllMultiThread implements Runnable {
	private static int MAX_THREADS = 10;
	Thread t;
	public static void calc(ExperimentSetup experimentSetup, File cwd) throws IOException, InterruptedException {
		CalcCutoffWeight cutoffWeights = new CalcCutoffWeight(experimentSetup, cwd);
		cutoffWeights.calcForFiles();
		ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
		
		
		
		
		for (Double maxCutoff: cutoffWeights.getMaxCutoffs()) {
			System.out.println("calculating for max cutoff " + maxCutoff);
			CalcRecall calc = new CalcRecall(experimentSetup, maxCutoff, cutoffWeights.getCutoffWeights(maxCutoff), cutoffWeights.getCutoffSizes(maxCutoff), cutoffWeights.getTotalSampleSize(), cwd);
			executor.execute(calc);
		}
	    // This will make the executor accept no new threads
	    // and finish all existing threads in the queue
	    executor.shutdown();
	    // Wait until all threads are finish
	    executor.awaitTermination(100, TimeUnit.HOURS);
	    System.out.println("Finished all threads");
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




	public void run() {
		// TODO Auto-generated method stub
		
	}
}

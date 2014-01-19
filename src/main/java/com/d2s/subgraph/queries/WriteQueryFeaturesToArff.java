package com.d2s.subgraph.queries;

import java.io.File;

import com.d2s.subgraph.eval.Config;
import com.d2s.subgraph.eval.experiments.ExperimentSetup;
import com.d2s.subgraph.util.Utils;

public class WriteQueryFeaturesToArff {
	
	
	private ExperimentSetup experimentSetup;
	File outputDir;
	public WriteQueryFeaturesToArff(ExperimentSetup experimentSetup) {
		this.experimentSetup = experimentSetup;
		initializeDirs();
	}
	
	public void initializeDirs() {
		Utils.mkdir(Config.PATH_QUERY_FEATURES);
		outputDir = Utils.mkdir(Config.PATH_QUERY_FEATURES + experimentSetup.getId());
	}
	
	
	public static void write(ExperimentSetup experimentSetup) {
		
	}
	
	public static void main(String[] args) {
		
	}
}

package com.d2s.subgraph.queries.qtriples;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.data2semantics.query.QueryCollection;

import com.d2s.subgraph.eval.Config;
import com.d2s.subgraph.eval.experiments.Bio2RdfExperimentSetup;
import com.d2s.subgraph.eval.experiments.ExperimentSetup;
import com.d2s.subgraph.eval.experiments.ExperimentSetupHelper;
import com.d2s.subgraph.eval.experiments.LgdExperimentSetup;
import com.d2s.subgraph.eval.experiments.ObmExperimentSetup;
import com.d2s.subgraph.queries.Query;


public class FetchTriplesFromQueries {
	private int maxQueries = Integer.MAX_VALUE;
	private ExperimentSetup experimentSetup;
	private File experimentDir;
	public FetchTriplesFromQueries(ExperimentSetup experimentSetup) throws IOException {
		this.experimentSetup = experimentSetup;
		setupDirStructure();
	}
	
	private void setupDirStructure() throws IOException {
		File outputDir = new File(Config.PATH_QUERY_TRIPLES);
		if (!outputDir.exists()) outputDir.mkdir();
		experimentDir = new File(outputDir.getPath() + "/" + experimentSetup.getId());
		
	}
	
	private void resetExperimentDir() throws IOException {
		if (experimentDir.exists()) {
			System.out.println("removed previous qtriples results");
			FileUtils.deleteDirectory(experimentDir);
		}
		experimentDir.mkdir();
	}
	
	private void processQueries() throws IOException {
		QueryCollection<Query> queries = experimentSetup.getQueryCollection();
		int count = 1;
		for (Query query: queries.getQueries()) {
			System.out.println(count + "/" + queries.getDistinctQueryCount());
			count++;
			FetchTriplesFromQuery.fetch(experimentSetup, query, experimentDir);
			if (count > maxQueries) break;
		}
	}
	
	public static void fetch(ExperimentSetup experimentSetup) throws IOException {
		FetchTriplesFromQueries fetch = new FetchTriplesFromQueries(experimentSetup);
		fetch.resetExperimentDir();
		fetch.processQueries();
//		fetch.createUniqueTripleFile();
	}
	
	
	public static void main(String[] args) throws Exception {
		FetchTriplesFromQueries fetch;
		if (args.length > 0) {
			fetch = new FetchTriplesFromQueries(ExperimentSetupHelper.get(args[0]));
		} else {
		
		
			boolean useCachedQueries = true;
			
	//		FetchTriplesFromQueries fetch = new FetchTriplesFromQueries(new SwdfExperimentSetup(useCachedQueries));
			fetch = new FetchTriplesFromQueries(new ObmExperimentSetup(useCachedQueries));
//			fetch = new FetchTriplesFromQueries(new Bio2RdfExperimentSetup(useCachedQueries));
		}
//		fetch.maxQueries = 20;
		fetch.resetExperimentDir();
		fetch.processQueries();

	}



}

package com.d2s.subgraph.querytriples;

import java.io.File;
import java.io.IOException;

import org.data2semantics.query.QueryCollection;

import com.d2s.subgraph.eval.Config;
import com.d2s.subgraph.eval.experiments.ExperimentSetup;
import com.d2s.subgraph.eval.experiments.SwdfExperimentSetup;
import com.d2s.subgraph.queries.Query;

public class FetchTriplesFromQueries {
	
	public static File setupDirStructure(ExperimentSetup experimentSetup) {
		File outputDir = new File(Config.PATH_QUERY_TRIPLES);
		if (!outputDir.exists()) outputDir.mkdir();
		File experimentSetupDir = new File(outputDir.getPath() + "/" + experimentSetup.getId());
		if (!experimentSetupDir.exists()) experimentSetupDir.mkdir();
		return experimentSetupDir;
	}
	
	public static void fetch(ExperimentSetup experimentSetup) throws IOException {
		File experimentDir = setupDirStructure(experimentSetup);
		QueryCollection<Query> queries = experimentSetup.getQueryCollection();
		for (Query query: queries.getQueries()) {
			FetchTriplesFromQuery.fetch(experimentSetup, query, experimentDir);
		}
	}

	public static void main(String[] args) throws Exception {
		
		
		boolean useCachedQueries = false;
		
		FetchTriplesFromQueries.fetch(new SwdfExperimentSetup(useCachedQueries));
		// new EvaluateGraphs(new
		// DbpoExperimentSetup(DbpoExperimentSetup.QALD_REMOVE_OPTIONALS)),
		// new EvaluateGraphs(new
		// DbpoExperimentSetup(DbpoExperimentSetup.QALD_KEEP_OPTIONALS)),
		// new EvaluateGraphs(new
		// DbpoExperimentSetup(DbpoExperimentSetup.QUERY_LOGS)),
		// new EvaluateGraphs(new Sp2bExperimentSetup()),
		// new EvaluateGraphs(new LmdbExperimentSetup()),
		// new EvaluateGraphs(new LgdExperimentSetup()),
		// new EvaluateGraphs(new DbpExperimentSetup()),
	}



}

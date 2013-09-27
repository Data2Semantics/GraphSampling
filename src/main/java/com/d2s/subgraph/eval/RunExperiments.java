package com.d2s.subgraph.eval;

import com.d2s.subgraph.eval.analysis.WriteAnalysis;
import com.d2s.subgraph.eval.experiments.ExperimentSetup;
import com.d2s.subgraph.eval.experiments.SwdfExperimentSetup;
import com.d2s.subgraph.eval.generation.FetchGraphsResults;


public class RunExperiments {
	
	public static void runExperiments(ExperimentSetup experimentSetup, boolean runQueries, boolean analyzeQueries) throws Exception {
		if (runQueries) FetchGraphsResults.doFetch(experimentSetup);
		if (analyzeQueries) WriteAnalysis.doWrite(experimentSetup);
	}
	
	public static void main(String[] args) throws Exception  {
		RunExperiments.runExperiments(new SwdfExperimentSetup(), true, true);
//		new EvaluateGraphs(new DbpoExperimentSetup(DbpoExperimentSetup.QALD_REMOVE_OPTIONALS)), 
//		new EvaluateGraphs(new DbpoExperimentSetup(DbpoExperimentSetup.QALD_KEEP_OPTIONALS)),
//		new EvaluateGraphs(new DbpoExperimentSetup(DbpoExperimentSetup.QUERY_LOGS)),
//		new EvaluateGraphs(new Sp2bExperimentSetup()),
//		new EvaluateGraphs(new LmdbExperimentSetup()),
//		new EvaluateGraphs(new LgdExperimentSetup()),
//		new EvaluateGraphs(new DbpExperimentSetup()),
	}

}

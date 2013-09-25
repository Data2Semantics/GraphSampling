package com.d2s.subgraph.eval.experiments;

import com.d2s.subgraph.queries.QueryFetcher;


public interface ExperimentSetup {
	
	
	
	public String getGoldenStandardGraph();
	public String getGraphPrefix();
	public QueryFetcher getQueries();
	public String getEvalResultsDir();
	public int getMaxNumQueries();
	public String getQueryTriplesDir();
	public String getQueryResultsDir();
	public boolean privateQueries();
}

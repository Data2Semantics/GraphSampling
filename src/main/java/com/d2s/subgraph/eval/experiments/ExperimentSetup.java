package com.d2s.subgraph.eval.experiments;

import com.d2s.subgraph.queries.GetQueries;


public interface ExperimentSetup {
	
	
	
	public String getGoldenStandardGraph();
	public String getGraphPrefix();
	public GetQueries getQueries();
	public String getResultsDir();
	public int getMaxNumQueries();
	public String getEndpoint();
}

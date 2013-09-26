package com.d2s.subgraph.eval.experiments;

import org.data2semantics.query.QueryCollection;
import com.d2s.subgraph.queries.Query;


public interface ExperimentSetup {
	
	
	
	public String getGoldenStandardGraph();
	public String getGraphPrefix();
	public QueryCollection<Query> getQueryCollection();
	public String getEvalResultsDir();
	public int getMaxNumQueries();
	public String getQueryTriplesDir();
	public String getQueryResultsDir();
	public boolean privateQueries();
	public boolean useUniqueQueries();
}

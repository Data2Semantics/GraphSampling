package com.d2s.subgraph.eval.experiments;

import java.io.IOException;

import com.d2s.subgraph.eval.EvaluateGraph;
import com.d2s.subgraph.queries.GetQueries;
import com.d2s.subgraph.queries.Sp2bQueries;
import com.d2s.subgraph.queries.filters.DescribeFilter;
import com.d2s.subgraph.queries.filters.GraphClauseFilter;
import com.d2s.subgraph.queries.filters.SimpleBgpFilter;



public class Sp2bExperimentSetup implements ExperimentSetup {
	public static String GOLDEN_STANDARD_GRAPH = "http://sp2b";
	private static String GRAPH_PREFIX = "sp2b_";
	private static String RESULTS_DIR = "sp2bResults";
	private static String QUERY_RESULTS_DIR = "sp2bQueryTriples";
	private static int MAX_NUM_QUERIES = 0;
	private GetQueries queries;
	
	public Sp2bExperimentSetup() throws IOException {
		queries = new Sp2bQueries(new DescribeFilter(), new SimpleBgpFilter(), new GraphClauseFilter());
		queries.setMaxNQueries(MAX_NUM_QUERIES);
	}
	
	public String getGoldenStandardGraph() {
		return GOLDEN_STANDARD_GRAPH;
	}
	public String getGraphPrefix() {
		return GRAPH_PREFIX;
	}
	public GetQueries getQueries() {
		return queries;
	}
	

	public String getResultsDir() {
		return RESULTS_DIR;
	}
	
	public String getQueryResultsDir() {
		return QUERY_RESULTS_DIR;
	}
	
	public int getMaxNumQueries() {
		return MAX_NUM_QUERIES;
	}
	public String getEndpoint() {
		return EvaluateGraph.OPS_VIRTUOSO;
	}
}

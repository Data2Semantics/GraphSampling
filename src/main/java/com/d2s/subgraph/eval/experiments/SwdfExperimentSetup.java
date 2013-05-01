package com.d2s.subgraph.eval.experiments;

import java.io.IOException;

import com.d2s.subgraph.eval.EvaluateGraph;
import com.d2s.subgraph.queries.GetQueries;
import com.d2s.subgraph.queries.SwdfQueries;
import com.d2s.subgraph.queries.filters.DescribeFilter;
import com.d2s.subgraph.queries.filters.GraphClauseFilter;
import com.d2s.subgraph.queries.filters.SimpleBgpFilter;



public class SwdfExperimentSetup implements ExperimentSetup {
	public static String GOLDEN_STANDARD_GRAPH = "http://swdf";
	private static String GRAPH_PREFIX = "df_";
	private static String QUERY_TRIPLES_DIR = "swdfQueryTriples";
	private static String QUERY_RESULTS_DIR = "swdfQueryResults";
	private static String EVAL_RESULTS_DIR = "swdfResults";
	private static int MAX_NUM_QUERIES = 100;
	private GetQueries queries;
	
	public SwdfExperimentSetup() throws IOException {
		queries = new SwdfQueries(false, MAX_NUM_QUERIES, new DescribeFilter(), new SimpleBgpFilter(), new GraphClauseFilter());
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
	

	public String getEvalResultsDir() {
		return EVAL_RESULTS_DIR;
	}
	public int getMaxNumQueries() {
		return MAX_NUM_QUERIES;
	}
	public String getEndpoint() {
		return EvaluateGraph.OPS_VIRTUOSO;
	}
	public String getQueryTriplesDir() {
		return QUERY_TRIPLES_DIR;
	}
	public String getQueryResultsDir() {
		return QUERY_RESULTS_DIR;
	}
}

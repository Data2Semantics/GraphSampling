package com.d2s.subgraph.eval.batch;

import java.io.IOException;

import com.d2s.subgraph.queries.GetQueries;
import com.d2s.subgraph.queries.SwdfQueries;
import com.d2s.subgraph.queries.filters.DescribeFilter;
import com.d2s.subgraph.queries.filters.SimpleBgpFilter;



public class SwdfExperimentSetup implements ExperimentSetup {
	public static String GOLDEN_STANDARD_GRAPH = "http://swdf";
	private static String GRAPH_PREFIX = "df_";
	private static String RESULTS_DIR = "swdfResults";
	private static int MAX_NUM_QUERIES = 100;
	private GetQueries queries;
	
	public SwdfExperimentSetup() throws IOException {
		queries = new SwdfQueries(true, new DescribeFilter(), new SimpleBgpFilter());
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
	public int getMaxNumQueries() {
		return MAX_NUM_QUERIES;
	}
}

package com.d2s.subgraph.eval.batch;

import java.io.IOException;

import com.d2s.subgraph.queries.GetQueries;
import com.d2s.subgraph.queries.SwdfQueries;
import com.d2s.subgraph.queries.filters.DescribeFilter;
import com.d2s.subgraph.queries.filters.SimpleBgpFilter;



public class SwdfExperimentSetup implements ExperimentSetup {
	private static String GOLDEN_STANDARD_GRAPH = "http://swdf";
	private static String GRAPH_PREFIX = "df_";
	private static String RESULTS_DIR = "swdfResults";
	private GetQueries queries;
	
	public SwdfExperimentSetup() throws IOException {
		queries = new SwdfQueries(new DescribeFilter(), new SimpleBgpFilter());
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

}

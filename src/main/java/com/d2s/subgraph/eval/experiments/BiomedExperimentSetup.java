package com.d2s.subgraph.eval.experiments;

import java.io.IOException;

import com.d2s.subgraph.eval.EvaluateGraph;
import com.d2s.subgraph.queries.BiomedQueries;
import com.d2s.subgraph.queries.GetQueries;
import com.d2s.subgraph.queries.SwdfQueries;
import com.d2s.subgraph.queries.filters.DescribeFilter;
import com.d2s.subgraph.queries.filters.GraphClauseFilter;
import com.d2s.subgraph.queries.filters.SimpleBgpFilter;



public class BiomedExperimentSetup implements ExperimentSetup {
	public static String GOLDEN_STANDARD_GRAPH = "http://biomed";
	private static String GRAPH_PREFIX = "bio_";
	private static String RESULTS_DIR = "biomedResults";
	private static String QUERY_RESULTS_DIR = "biomedQueryTriples";
	private static int MAX_NUM_QUERIES = 100;
	private GetQueries queries;
	
	public BiomedExperimentSetup() throws IOException {
		queries = new BiomedQueries(true, new DescribeFilter(), new SimpleBgpFilter(), new GraphClauseFilter());
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
	public String getEndpoint() {
		return EvaluateGraph.OPS_VIRTUOSO;
	}
	public String getQueryResultsDir() {
		return QUERY_RESULTS_DIR;
	}
}

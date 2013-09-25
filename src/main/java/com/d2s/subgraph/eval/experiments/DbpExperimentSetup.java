package com.d2s.subgraph.eval.experiments;

import java.io.IOException;

import org.data2semantics.query.filters.ConstructFilter;
import org.data2semantics.query.filters.DescribeFilter;
import org.data2semantics.query.filters.GraphClauseFilter;

import com.d2s.subgraph.eval.generation.EvaluateGraph;
import com.d2s.subgraph.queries.DbpQueries;
import com.d2s.subgraph.queries.QueryFetcher;
import com.d2s.subgraph.queries.filters.SimpleBgpFilter;



public class DbpExperimentSetup implements ExperimentSetup {
	public static String GOLDEN_STANDARD_GRAPH = "http://dbpl";
	private static String GRAPH_PREFIX = "dbpl_";
	private static String QUERY_TRIPLES_DIR = "dbplTriples";
	private static String QUERY_RESULTS_DIR = "dbplQueryResults";
	private static String EVAL_RESULTS_DIR = "dbplResults";
	private static boolean PRIVATE_QUERIES = false;
	private static int MAX_NUM_QUERIES = 100;
	private QueryFetcher queries;
	
	public DbpExperimentSetup() throws IOException {
		queries = new DbpQueries(false, MAX_NUM_QUERIES, new DescribeFilter(), new SimpleBgpFilter(), new GraphClauseFilter(), new ConstructFilter());
	}
	
	public String getGoldenStandardGraph() {
		return GOLDEN_STANDARD_GRAPH;
	}
	public String getGraphPrefix() {
		return GRAPH_PREFIX;
	}
	public QueryFetcher getQueries() {
		return queries;
	}
	

	public String getEvalResultsDir() {
		return EVAL_RESULTS_DIR;
	}
	public int getMaxNumQueries() {
		return MAX_NUM_QUERIES;
	}
	public String getQueryTriplesDir() {
		return QUERY_TRIPLES_DIR;
	}
	public String getQueryResultsDir() {
		return QUERY_RESULTS_DIR;
	}
	public boolean privateQueries() {
		return PRIVATE_QUERIES;
	}
}

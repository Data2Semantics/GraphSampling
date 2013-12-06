package com.d2s.subgraph.eval.experiments;

import java.io.IOException;

import org.data2semantics.query.QueryCollection;
import org.data2semantics.query.filters.ConstructFilter;
import org.data2semantics.query.filters.DescribeFilter;
import org.data2semantics.query.filters.GraphClauseFilter;
import com.d2s.subgraph.queries.DbpQueries;
import com.d2s.subgraph.queries.QueriesFetcher;
import com.d2s.subgraph.queries.Query;
import com.d2s.subgraph.queries.filters.SimpleBgpFilter;



public class DbpExperimentSetup extends ExperimentSetup {
	
	public static String GOLDEN_STANDARD_GRAPH = "http://dbpl";
	private static String GRAPH_PREFIX = "dbpl_";
	private static String QUERY_TRIPLES_DIR = "dbplTriples";
	private static String QUERY_RESULTS_DIR = "dbplQueryResults";
	private static String EVAL_RESULTS_DIR = "dbplResults";
	private static boolean UNIQUE_QUERIES = true;
	private static boolean PRIVATE_QUERIES = false;
	private static int MAX_NUM_QUERIES = 100;
	private QueriesFetcher qFetcher;
	
	public DbpExperimentSetup(boolean useCacheFile) throws IOException {
		super(useCacheFile);
		qFetcher = new DbpQueries(this, useCacheFile, MAX_NUM_QUERIES, new DescribeFilter(), new SimpleBgpFilter(), new GraphClauseFilter(), new ConstructFilter());
	}
	
	public String getGoldenStandardGraph() {
		return GOLDEN_STANDARD_GRAPH;
	}
	public String getGraphPrefix() {
		return GRAPH_PREFIX;
	}
	public QueryCollection<Query> getQueryCollection() {
		return qFetcher.getQueryCollection();
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
	public boolean useUniqueQueries() {
		return UNIQUE_QUERIES;
	}
	public String getId() {
		return this.getClass().getSimpleName();
		
	}


}

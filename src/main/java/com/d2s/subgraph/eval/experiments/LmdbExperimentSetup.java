package com.d2s.subgraph.eval.experiments;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

import com.d2s.subgraph.eval.EvaluateGraph;
import com.d2s.subgraph.queries.GetQueries;
import com.d2s.subgraph.queries.LmdbQueries;
import com.d2s.subgraph.queries.filters.DescribeFilter;
import com.d2s.subgraph.queries.filters.GraphClauseFilter;
import com.d2s.subgraph.queries.filters.SimpleBgpFilter;


public class LmdbExperimentSetup implements ExperimentSetup {
	public static String GOLDEN_STANDARD_GRAPH = "http://lmdb";
	private static String GRAPH_PREFIX = "lmdb_";
	private static String EVAL_RESULTS_DIR = "lmdbResults";
	private static String QUERY_TRIPLES_DIR = "lmdbQueryTriples";
	private static String QUERY_RESULTS_DIR = "lmdbQueryResults";
	private static int MAX_NUM_QUERIES = 0;//i.e. all
	private GetQueries queries;
	public LmdbExperimentSetup() throws SAXException, IOException, ParserConfigurationException {
		queries = new LmdbQueries(true, new GraphClauseFilter(), new SimpleBgpFilter(), new DescribeFilter());
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

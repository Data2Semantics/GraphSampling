package com.d2s.subgraph.eval.experiments;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.data2semantics.query.QueryCollection;
import org.data2semantics.query.filters.ConstructFilter;
import org.data2semantics.query.filters.DescribeFilter;
import org.data2semantics.query.filters.GraphClauseFilter;
import org.xml.sax.SAXException;

import com.d2s.subgraph.queries.Query;
import com.d2s.subgraph.queries.fetch.Bio2RdfQueries;
import com.d2s.subgraph.queries.fetch.DbpediaQueries;
import com.d2s.subgraph.queries.fetch.MetalexQueries;
import com.d2s.subgraph.queries.fetch.QueriesFetcher;
import com.d2s.subgraph.queries.filters.SimpleBgpFilter;
import com.hp.hpl.jena.query.QueryParseException;



public class MetalexExperimentSetup extends ExperimentSetup {
	
	public static String GOLDEN_STANDARD_GRAPH = "http://metalex";
	private static String GRAPH_PREFIX = "metalex_";
	private static String QUERY_RESULTS_DIR = "metalexQueryResults";
	private static String EVAL_RESULTS_DIR = "metalexResults";
	private static boolean UNIQUE_QUERIES = true;
	private static boolean PRIVATE_QUERIES = true;
	private static int MAX_NUM_QUERIES = 5000;
	private QueriesFetcher qFetcher;
	
	public MetalexExperimentSetup(boolean useCacheFile) throws IOException, QueryParseException, ParserConfigurationException, SAXException {
		this(useCacheFile, false);
	}
	
	public MetalexExperimentSetup(boolean useCacheFile, boolean skipLoadingFetcher) throws IOException, ParserConfigurationException, SAXException {
		super(useCacheFile);
		if (!skipLoadingFetcher) {
			qFetcher = new MetalexQueries(this, useCacheFile, new DescribeFilter(), new SimpleBgpFilter(), new GraphClauseFilter(), new ConstructFilter());
		}
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
	public String getQueryResultsDir() {
		return QUERY_RESULTS_DIR;
	}
	public boolean privateQueries() {
		return PRIVATE_QUERIES;
	}
	public boolean useUniqueQueries() {
		return UNIQUE_QUERIES;
	}

	public LogType getLogType() {
		return LogType.OTHER;
	}
	
	public static void main(String[] args) throws QueryParseException, IOException, ParserConfigurationException, SAXException {
		MetalexExperimentSetup setup = new MetalexExperimentSetup(true);
	}
}

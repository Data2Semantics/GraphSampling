package com.d2s.subgraph.eval.experiments;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.data2semantics.query.QueryCollection;
import org.data2semantics.query.filters.DescribeFilter;
import org.data2semantics.query.filters.GraphClauseFilter;
import org.xml.sax.SAXException;

import com.d2s.subgraph.queries.Query;
import com.d2s.subgraph.queries.fetch.LmdbQueries;
import com.d2s.subgraph.queries.fetch.QueriesFetcher;
import com.d2s.subgraph.queries.filters.SimpleBgpFilter;


public class LmdbExperimentSetup extends ExperimentSetup {
	public static String GOLDEN_STANDARD_GRAPH = "http://lmdb";
	private static String GRAPH_PREFIX = "lmdb_";
	private static String EVAL_RESULTS_DIR = "lmdbResults";
	private static String QUERY_RESULTS_DIR = "lmdbQueryResults";
	private static boolean PRIVATE_QUERIES = false;
	private static int MAX_NUM_QUERIES = 0;//i.e. all
	private QueriesFetcher queriesFetcher;
	private boolean UNIQUE_QUERIES = true;
	
	public LmdbExperimentSetup(boolean useCacheFile) throws IOException, ParserConfigurationException, SAXException {
		this(useCacheFile, false);
	}
	public LmdbExperimentSetup(boolean useCacheFile, boolean skipLoadingFetcher) throws IOException, ParserConfigurationException, SAXException {
		super(useCacheFile);
		if (!skipLoadingFetcher) {
			queriesFetcher = new LmdbQueries(this, useCacheFile, new GraphClauseFilter(), new SimpleBgpFilter(), new DescribeFilter());
		}
	}
	
	
	public String getGoldenStandardGraph() {
		return GOLDEN_STANDARD_GRAPH;
	}
	public String getGraphPrefix() {
		return GRAPH_PREFIX;
	}
	public QueryCollection<Query> getQueryCollection() {
		return queriesFetcher.getQueryCollection();
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
		return UNIQUE_QUERIES ;
	}

	public LogType getLogType() {
		return LogType.PLAIN_TEXT;
	}

}

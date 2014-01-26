package com.d2s.subgraph.eval.experiments;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.data2semantics.query.QueryCollection;
import org.data2semantics.query.filters.ConstructFilter;
import org.data2semantics.query.filters.DescribeFilter;
import org.data2semantics.query.filters.GraphClauseFilter;
import org.xml.sax.SAXException;

import com.d2s.subgraph.queries.Query;
import com.d2s.subgraph.queries.fetch.LgdQueries;
import com.d2s.subgraph.queries.fetch.QueriesFetcher;
import com.d2s.subgraph.queries.filters.SimpleBgpFilter;



public class LgdExperimentSetup extends ExperimentSetup {
	public static String GOLDEN_STANDARD_GRAPH = "http://lgd";
	private static String GRAPH_PREFIX = "lgd_";
	private static String QUERY_RESULTS_DIR = "lgdQueryResults";
	private static String EVAL_RESULTS_DIR = "lgdResults";
	private static boolean PRIVATE_QUERIES = false;
	private static int MAX_NUM_QUERIES = 5000;
	private QueriesFetcher queriesFetcher;
	private boolean UNIQUE_QUERIES = true;
	
	
	public LgdExperimentSetup(boolean useCacheFile) throws IOException, ParserConfigurationException, SAXException {
		this(useCacheFile, false);
	}
	public LgdExperimentSetup(boolean useCacheFile, boolean skipLoadingFetcher) throws IOException, ParserConfigurationException, SAXException {
		super(useCacheFile);
		if (!skipLoadingFetcher) {
			queriesFetcher = new LgdQueries(this, useCacheFile, new DescribeFilter(), new SimpleBgpFilter(), new GraphClauseFilter(), new ConstructFilter());
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
		return LogType.CLF;
	}
}

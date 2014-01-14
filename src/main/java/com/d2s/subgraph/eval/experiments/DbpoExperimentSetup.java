package com.d2s.subgraph.eval.experiments;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.data2semantics.query.QueryCollection;
import org.xml.sax.SAXException;

import com.d2s.subgraph.queries.Query;
import com.d2s.subgraph.queries.fetch.QaldDbpQueries;
import com.d2s.subgraph.queries.fetch.QueriesFetcher;
import com.hp.hpl.jena.query.QueryParseException;


public class DbpoExperimentSetup extends ExperimentSetup {
	public static int QALD_REMOVE_OPTIONALS = 1;
	public static int QALD_KEEP_OPTIONALS = 2;
	public static int QUERY_LOGS = 3;
	
	public static String GOLDEN_STANDARD_GRAPH = "http://dbpo";
	private static String GRAPH_PREFIX = "dbp_";
	private static String EVAL_RESULTS_DIR = "dbpResults";
	private static String QUERY_RESULTS_DIR = "dbpQueryResults";
	private static boolean PRIVATE_QUERIES = true;
	
	private static int MAX_NUM_QUERIES = 0;//i.e. all
	private QueriesFetcher queriesFetcher;
	private int querySelection;
	private boolean UNIQUE_QUERIES = true;
	
	
	public DbpoExperimentSetup(int querySelection, boolean useCacheFile) throws IOException, QueryParseException, ParserConfigurationException, SAXException {
		this(querySelection, useCacheFile, false);
	}
	
	public DbpoExperimentSetup(int querySelection, boolean useCacheFile, boolean skipLoadingFetcher) throws IOException, ParserConfigurationException, SAXException {
		super(useCacheFile);
		this.querySelection = querySelection;
		if (!skipLoadingFetcher) {
			if (querySelection == QALD_KEEP_OPTIONALS || querySelection == QALD_REMOVE_OPTIONALS) {
				queriesFetcher = new QaldDbpQueries(this, QaldDbpQueries.QALD_2_QUERIES, (querySelection == QALD_REMOVE_OPTIONALS? true: false), true);
			} else if (querySelection == QUERY_LOGS) {
				throw new IllegalStateException("No implemented yet");
			} else {
				throw new IllegalStateException("Illegal query selection mode passed to dbpo experiment setup");
			}
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
		String evalResultsDir = EVAL_RESULTS_DIR + "_";
		if (querySelection == QALD_KEEP_OPTIONALS) {
			evalResultsDir += "qaldWithOptionals";
		} else if (querySelection == QALD_REMOVE_OPTIONALS) {
			evalResultsDir += "qaldNoOptionals";
		} else if (querySelection == QUERY_LOGS) {
			evalResultsDir += "queryLogs";
		}
		return evalResultsDir;
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
		return LogType.OTHER;
	}

}

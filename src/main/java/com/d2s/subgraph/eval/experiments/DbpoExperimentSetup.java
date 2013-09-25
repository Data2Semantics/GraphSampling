package com.d2s.subgraph.eval.experiments;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.d2s.subgraph.eval.EvaluateGraph;
import com.d2s.subgraph.queries.GetQueries;
import com.d2s.subgraph.queries.QaldDbpQueries;


public class DbpoExperimentSetup implements ExperimentSetup {
	public static int QALD_REMOVE_OPTIONALS = 1;
	public static int QALD_KEEP_OPTIONALS = 2;
	public static int QUERY_LOGS = 3;
	
	public static String GOLDEN_STANDARD_GRAPH = "http://dbpo";
	private static String GRAPH_PREFIX = "dbp_";
	private static String EVAL_RESULTS_DIR = "dbpResults";
	private static String QUERY_TRIPLES_DIR = "dbpQueryTriples";
	private static String QUERY_RESULTS_DIR = "dbpQueryResults";
	private static boolean PRIVATE_QUERIES = true;
	
	private static int MAX_NUM_QUERIES = 0;//i.e. all
	private GetQueries queries;
	private int querySelection;
	
	public DbpoExperimentSetup(int querySelection) throws SAXException, IOException, ParserConfigurationException, IllegalStateException {
		this.querySelection = querySelection;
		if (querySelection == QALD_KEEP_OPTIONALS || querySelection == QALD_REMOVE_OPTIONALS) {
			queries = new QaldDbpQueries(QaldDbpQueries.QALD_2_QUERIES, (querySelection == QALD_REMOVE_OPTIONALS? true: false), true);
		} else if (querySelection == QUERY_LOGS) {
			throw new IllegalStateException("No implemented yet");
		} else {
			throw new IllegalStateException("Illegal query selection mode passed to dbpo experiment setup");
		}
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
	public String getEndpoint() {
		return EvaluateGraph.OPS_VIRTUOSO;
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

package com.d2s.subgraph.eval.experiments;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

import com.d2s.subgraph.eval.EvaluateGraph;
import com.d2s.subgraph.queries.GetQueries;
import com.d2s.subgraph.queries.QaldDbpQueries;
import com.d2s.subgraph.queries.filters.OnlyDboQueries;


public class DbpExperimentSetup implements ExperimentSetup {
	public static String GOLDEN_STANDARD_GRAPH = "http://dbpo";
	private static String GRAPH_PREFIX = "dbp_";
	private static String RESULTS_DIR = "dbpResults";
	private static int MAX_NUM_QUERIES = 0;//i.e. all
	private GetQueries queries;
	public DbpExperimentSetup() throws SAXException, IOException, ParserConfigurationException {
		queries = new QaldDbpQueries(QaldDbpQueries.QALD_2_QUERIES, new OnlyDboQueries());
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
}

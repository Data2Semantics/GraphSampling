package com.d2s.subgraph.eval.batch;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import com.d2s.subgraph.queries.GetQueries;
import com.d2s.subgraph.queries.QaldDbpQueries;


public class DbpExperimentSetup implements ExperimentSetup {
	private static String GOLDEN_STANDARD_GRAPH = "http://dbpo";
	private static String GRAPH_PREFIX = "dbp_";
	private static String RESULTS_DIR = "dbpResults";
	private GetQueries queries;
	public DbpExperimentSetup() throws SAXException, IOException, ParserConfigurationException {
		queries = new QaldDbpQueries(QaldDbpQueries.QALD_2_QUERIES);
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
	

}

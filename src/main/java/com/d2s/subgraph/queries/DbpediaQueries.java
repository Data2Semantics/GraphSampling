package com.d2s.subgraph.queries;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.data2semantics.query.filters.QueryFilter;
import org.xml.sax.SAXException;

import com.d2s.subgraph.eval.experiments.ExperimentSetup;
import com.hp.hpl.jena.query.QueryParseException;

public class DbpediaQueries extends QueriesFetcher {
	public static String QUERY_FILE = "src/main/resources/dbpl_queries.log";
	public static String CSV_COPY = "src/main/resources/dbpl_queries.csv";
	

	public DbpediaQueries(ExperimentSetup experimentSetup, QueryFilter... filters) throws IOException, QueryParseException, ParserConfigurationException, SAXException {
		this(experimentSetup, true, filters);
	}

	public DbpediaQueries(ExperimentSetup experimentSetup, boolean useCacheFile, QueryFilter... filters) throws IOException, QueryParseException, ParserConfigurationException, SAXException {
		super(experimentSetup, useCacheFile, filters);
		fetch();
	}
	
	


	protected void parseCustomLogFile(File textFile) throws IOException {
		//not needed (we use CLF, something we user for other parsers as well)
	}

	

	public static void main(String[] args) {

		try {

//			DbpQueries queries = new DbpQueries(false, 10, new SimpleDbpFilter(), new DescribeFilter(), new SimpleBgpFilter(), new GraphClauseFilter());
//			System.out.println(queries.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

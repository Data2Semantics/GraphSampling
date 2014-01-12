package com.d2s.subgraph.queries.fetch;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.data2semantics.query.filters.QueryFilter;
import org.xml.sax.SAXException;

import com.d2s.subgraph.eval.experiments.ExperimentSetup;
import com.d2s.subgraph.eval.experiments.TestExperimentSetup;
import com.hp.hpl.jena.query.QueryParseException;

public class TestFetcher extends QueriesFetcher {

	public TestFetcher(ExperimentSetup experimentSetup, QueryFilter... filters) throws IOException, ParserConfigurationException, SAXException {
		this(experimentSetup, true, filters);
	}

	public TestFetcher(ExperimentSetup experimentSetup, boolean useCacheFile, QueryFilter... filters) throws IOException, ParserConfigurationException,
			SAXException {
		super(experimentSetup, useCacheFile, filters);
		fetch();
	}
	
	protected void fetch() throws QueryParseException, IOException, ParserConfigurationException, SAXException {
		System.out.println("testing just 1 query");
		String query = "SELECT  ?geonames ?type ?name\n" + 
				"FROM <http://lgd>\n" + 
				"WHERE\n" + 
				"  { ?geonames <www.w3.org/TR/rdf-syntax/type> ?type .\n" + 
				"    ?name <bif:contains> \"Madrid\"\n" + 
				"  }";
		addQueryToList(query);
	}
	protected void parseCustomLogFile(File textFile) throws IOException {
		//
	}

	public static void main(String[] args) {

		try {
			new TestExperimentSetup(false);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

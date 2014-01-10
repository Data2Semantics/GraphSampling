package com.d2s.subgraph.queries;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.data2semantics.query.filters.QueryFilter;
import org.xml.sax.SAXException;

import com.d2s.subgraph.eval.experiments.ExperimentSetup;

public class SwdfQueries extends QueriesFetcher {

	public SwdfQueries(ExperimentSetup experimentSetup, QueryFilter... filters) throws IOException, ParserConfigurationException, SAXException {
		this(experimentSetup, true, filters);
	}

	public SwdfQueries(ExperimentSetup experimentSetup, boolean useCacheFile, QueryFilter... filters) throws IOException, ParserConfigurationException, SAXException {
		super(experimentSetup, useCacheFile, filters);
		fetch();
	}
	

	protected void parseCustomLogFile(File textFile) throws IOException {
		//not needed (we use CLF, something we user for other parsers as well)
	}

	public static void main(String[] args) {

		try {

//			SwdfQueries swdfQueries = new SwdfQueries(false, 100, new DescribeFilter(), new SimpleBgpFilter(), new GraphClauseFilter());
//			System.out.println(swdfQueries.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

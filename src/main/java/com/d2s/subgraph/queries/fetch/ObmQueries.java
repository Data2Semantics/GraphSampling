package com.d2s.subgraph.queries.fetch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.data2semantics.query.filters.QueryFilter;
import org.xml.sax.SAXException;

import com.d2s.subgraph.eval.experiments.ExperimentSetup;

public class ObmQueries extends QueriesFetcher {

	public ObmQueries(ExperimentSetup experimentSetup, QueryFilter... filters) throws IOException, ParserConfigurationException, SAXException {
		this(experimentSetup, true, filters);
	}

	public ObmQueries(ExperimentSetup experimentSetup, boolean useCacheFile, QueryFilter... filters) throws IOException, ParserConfigurationException,
			SAXException {
		super(experimentSetup, useCacheFile, filters);
		tryFetchingQueriesFromCache();
		
		if (queryCollection.getTotalQueryCount() == 0) {
			parseLogFiles("log");
			saveQueriesToCacheFile();
			saveQueriesToCsv();
		}
	}

	protected void parseCustomLogFile(File textFile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(textFile));
		String line;
		boolean inQuerySection = false;
		String query = "";
		while ((line = br.readLine()) != null) {
			if (line.startsWith("--------")) {
				inQuerySection = !inQuerySection;
				if (!inQuerySection) {
					//ok, so we used to be in one!
					//Store query, and reset query value;
					addQueryToList(query);
					if (experimentSetup.getMaxNumQueries() != 0 && queryCollection.getDistinctQueryCount() > experimentSetup.getMaxNumQueries()) {
						break;
					}
					query = "";
				}
				continue;
			}
			if (line.startsWith("#")) {
				//comment, continue
				continue;
			}
			if (inQuerySection) {
				query += line + "\n";
			}
		}
		br.close();
	}

	public static void main(String[] args) {

		try {

			// LgdQueries swdfQueries = new LgdQueries(false, 100, new
			// DescribeFilter(), new SimpleBgpFilter(), new
			// GraphClauseFilter());
			// System.out.println(swdfQueries.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

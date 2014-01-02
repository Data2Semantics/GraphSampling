package com.d2s.subgraph.queries;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;

import org.data2semantics.query.filters.QueryFilter;

import com.d2s.subgraph.eval.experiments.ExperimentSetup;

public class LgdQueries extends QueriesFetcher {
	public static String QUERY_FILE = "src/main/resources/lgd_queries.log";
	public static String CSV_COPY = "src/main/resources/lgd_queries.csv";

	public LgdQueries(ExperimentSetup experimentSetup, QueryFilter... filters) throws IOException {
		this(experimentSetup, true, 0, filters);
	}

	public LgdQueries(ExperimentSetup experimentSetup, boolean useCacheFile, int maxNumQueries, QueryFilter... filters) throws IOException {
		super(experimentSetup, useCacheFile);
		this.maxNumQueries = maxNumQueries;
		tryFetchingQueriesFromCache();
		if (queryCollection.getTotalQueryCount() == 0) {
			System.out.println("parsing SWDF query logs");
			this.filters = new ArrayList<QueryFilter>(Arrays.asList(filters));
			parseLogFile(new File(QUERY_FILE));
			saveQueriesToCacheFile();
			saveQueriesToCsv(CSV_COPY);
		}
		
	}

	
	


	private void parseLogFile(File textFile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(textFile));
		String line;
		while ((line = br.readLine()) != null) {
			String matchSubString = "/sparql?query=";
			if (line.contains(matchSubString)) {
				System.out.print(".");
				int startIndex = line.indexOf(matchSubString);
				startIndex += matchSubString.length();
				String firstString = line.substring(startIndex);
				String encodedUrlQuery = firstString.split(" ")[0];
				// remove other args
				String encodedSparqlQuery = encodedUrlQuery.split("&")[0];

				addQueryToList(URLDecoder.decode(encodedSparqlQuery, "UTF-8"));
				if (queryCollection.getDistinctQueryCount() > maxNumQueries) {
					break;
				}
			}
		}
		br.close();
	}

	public static void main(String[] args) {

		try {

//			LgdQueries swdfQueries = new LgdQueries(false, 100, new DescribeFilter(), new SimpleBgpFilter(), new GraphClauseFilter());
//			System.out.println(swdfQueries.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

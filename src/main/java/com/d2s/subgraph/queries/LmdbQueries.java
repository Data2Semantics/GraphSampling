package com.d2s.subgraph.queries;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.data2semantics.query.filters.QueryFilter;

import com.d2s.subgraph.eval.experiments.ExperimentSetup;

public class LmdbQueries extends QueriesFetcher {
	public static String QUERY_FILE = "src/main/resources/lmdbQueries.txt";
	public static String CSV_COPY = "src/main/resources/lmdb_queries.csv";
	public static String PARSE_QUERIES_FILE = "src/main/resources/lmdb_queries.arraylist";

	public LmdbQueries(ExperimentSetup experimentSetup, QueryFilter... filters) throws IOException {
		this(experimentSetup, true, filters);
	}

	public LmdbQueries(ExperimentSetup experimentSetup, boolean useCacheFile, QueryFilter... filters) throws IOException {
		super(experimentSetup);
		tryFetchingQueriesFromCache(PARSE_QUERIES_FILE);
		if (queryCollection.getTotalQueryCount() == 0) {
			System.out.println("parsing lmdb query logs");
			this.filters = new ArrayList<QueryFilter>(Arrays.asList(filters));
			parseLogFile(new File(QUERY_FILE));
			saveQueriesToCacheFile(PARSE_QUERIES_FILE);
			saveQueriesToCsv(CSV_COPY);
		}
	}

	

	private void parseLogFile(File textFile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(textFile));
		String query;
		while ((query = br.readLine()) != null) {
			if (query.length() > 0) {
				addQueryToList(query);
			}
			if (queryCollection.getDistinctQueryCount() > maxNumQueries) {
				break;
			}
		}
		br.close();
	}


	public static void main(String[] args) {

		try {

//			LmdbQueries lmdbQueries = new LmdbQueries(false, new GraphClauseFilter(), new SimpleBgpFilter(), new DescribeFilter());
//			System.out.println(lmdbQueries.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

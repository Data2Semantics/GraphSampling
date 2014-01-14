package com.d2s.subgraph.queries.fetch;

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

	public LmdbQueries(ExperimentSetup experimentSetup, QueryFilter... filters) throws IOException {
		this(experimentSetup, true, filters);
	}

	public LmdbQueries(ExperimentSetup experimentSetup, boolean useCacheFile, QueryFilter... filters) throws IOException {
		super(experimentSetup, useCacheFile);
		tryFetchingQueriesFromCache();
		if (queryCollection.getTotalQueryCount() == 0) {
			System.out.println("parsing lmdb query logs");
			this.filters = new ArrayList<QueryFilter>(Arrays.asList(filters));
			parseCustomLogFile(new File(QUERY_FILE));
			saveQueriesToCacheFile();
			saveQueriesToCsv();
		}
	}

	

	protected void parseCustomLogFile(File textFile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(textFile));
		String query;
		while ((query = br.readLine()) != null) {
			if (query.length() > 0) {
				addQueryToList(query);
			}
			if (queryCollection.getDistinctQueryCount() > experimentSetup.getMaxNumQueries()) {
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

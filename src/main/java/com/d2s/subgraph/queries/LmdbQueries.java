package com.d2s.subgraph.queries;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import org.data2semantics.query.QueryCollection;
import org.data2semantics.query.filters.DescribeFilter;
import org.data2semantics.query.filters.GraphClauseFilter;
import org.data2semantics.query.filters.QueryFilter;

import com.d2s.subgraph.queries.filters.SimpleBgpFilter;
import com.hp.hpl.jena.query.QueryBuildException;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;

public class LmdbQueries extends QueriesFetcher {
	public static String QUERY_FILE = "src/main/resources/lmdbQueries.txt";
	public static String CSV_COPY = "src/main/resources/lmdb_queries.csv";
	public static String PARSE_QUERIES_FILE = "src/main/resources/lmdb_queries.arraylist";

	public LmdbQueries(QueryFilter... filters) throws IOException {
		this(true, filters);
	}

	public LmdbQueries(boolean useCacheFile, QueryFilter... filters) throws IOException {
		super();
		File cacheFile = new File(PARSE_QUERIES_FILE);
		if (useCacheFile && cacheFile.exists()) {
			System.out.println("WATCH OUT! getting queries from cache file. might be outdated!");
			readQueriesFromCacheFile(PARSE_QUERIES_FILE);
		}
		if (queryCollection.getTotalQueryCount() == 0 || (maxNumQueries > 0 && maxNumQueries != queryCollection.getTotalQueryCount())) {
			System.out.println("parsing lmdb query logs");
			this.filters = new ArrayList<QueryFilter>(Arrays.asList(filters));
			parseLogFile(new File(QUERY_FILE));
			saveQueriesToCacheFile(PARSE_QUERIES_FILE);
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

			LmdbQueries lmdbQueries = new LmdbQueries(false, new GraphClauseFilter(), new SimpleBgpFilter(), new DescribeFilter());
			System.out.println(lmdbQueries.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

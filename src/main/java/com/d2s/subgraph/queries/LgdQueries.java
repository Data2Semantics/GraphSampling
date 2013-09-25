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
import java.util.Date;
import java.util.Scanner;

import org.data2semantics.query.QueryCollection;
import org.data2semantics.query.filters.DescribeFilter;
import org.data2semantics.query.filters.GraphClauseFilter;
import org.data2semantics.query.filters.QueryFilter;

import com.d2s.subgraph.queries.filters.SimpleBgpFilter;
import com.hp.hpl.jena.query.QueryParseException;

public class LgdQueries extends GetQueries {
	public static String QUERY_FILE = "src/main/resources/lgd_queries.log";
	public static String CSV_COPY = "src/main/resources/lgd_queries.csv";
	public static String PARSE_QUERIES_FILE = "src/main/resources/lgd_queries.arraylist";
	private static boolean ONLY_UNIQUE = true;

	public LgdQueries(QueryFilter... filters) throws IOException {
		this(true, 0, filters);
	}

	public LgdQueries(boolean useCacheFile, int maxNumQueries, QueryFilter... filters) throws IOException {
		this.maxNumQueries = maxNumQueries;
		File cacheFile = new File(PARSE_QUERIES_FILE);
		if (useCacheFile && cacheFile.exists()) {
			System.out.println("WATCH OUT! getting queries from cache file. might be outdated!");
			readQueriesFromCacheFile(cacheFile);
		}
		if (queries == null || queries.size() == 0 || (maxNumQueries > 0 && maxNumQueries != queries.size())) {
			System.out.println("parsing SWDF query logs");
			this.filters = new ArrayList<QueryFilter>(Arrays.asList(filters));
			parseLogFile(new File(QUERY_FILE));
			if (ONLY_UNIQUE) {
				// we have stored stuff in hashmap to keep queries unique. now get them as regular queries
				queries = new ArrayList<Query>(queriesHm.values());
				queriesHm.clear();
			}
			saveCsvCopy(new File(CSV_COPY));
			saveQueriesToCacheFile();
		}
		
	}

	private void saveQueriesToCacheFile() throws IOException {
		FileWriter writer = new FileWriter(PARSE_QUERIES_FILE);
		for (Query query : queries) {
			writer.write(URLEncoder.encode(query.toString(), "UTF-8") + "\n");
		}
		writer.close();
	}
	
	private void readQueriesFromCacheFile(File cacheFile) throws QueryParseException, IOException {
		Scanner sc = new Scanner(cacheFile);
		int queryIndex = 0;
		while(sc.hasNext()) {
			String line = sc.next();
			String queryString = line.trim();
			if (queryString.length() > 0) {
				Query query = Query.create(URLDecoder.decode(queryString, "UTF-8"), new QueryCollection());
				query.setQueryId(queryIndex);
				queries.add(query);
				queryIndex++;
			}
		}
		sc.close();
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
				if (queries.size() > maxNumQueries || queriesHm.size() > maxNumQueries) {
					break;
				}
			}
		}
		br.close();
	}

	private void addQueryToList(String queryString) throws IOException {
		try {
			Query query = Query.create(queryString, new QueryCollection());
			if (checkFilters(query)) {
				if (ONLY_UNIQUE) {
					if (queriesHm.containsKey(query)) {
						duplicateQueries++;
					} else {
						Date timeStart = new Date();
						if (hasResults(query)) {
							query.setQueryId(validQueries);
							queriesHm.put(query, query);
							validQueries++;
							System.out.println(validQueries);
						} else {
							noResultsQueries++;
						}
						Date timeEnd = new Date();
						if ((timeEnd.getTime() - timeStart.getTime()) > 5000) {
							//longer than 5 seconds
							System.out.println("taking longer than 5 seconds:");
							System.out.println(query.toString());
						}
					}
				} else {
					queries.add(query);
					validQueries++;
				}
				try {
					query.generateQueryStats();
				} catch (Exception e) {
					System.out.println(query.toString());
					e.printStackTrace();
					System.exit(1);
				}
			} else {
				filteredQueries++;
			}
			
		} catch (QueryParseException e) {
			// could not parse query, probably a faulty one. ignore!
			invalidQueries++;
		}
	}

	public static void main(String[] args) {

		try {

			LgdQueries swdfQueries = new LgdQueries(false, 100, new DescribeFilter(), new SimpleBgpFilter(), new GraphClauseFilter());
			System.out.println(swdfQueries.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

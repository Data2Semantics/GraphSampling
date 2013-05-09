package com.d2s.subgraph.queries;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

import org.apache.commons.httpclient.HttpException;

import au.com.bytecode.opencsv.CSVWriter;

import com.d2s.subgraph.eval.EvaluateGraph;
import com.d2s.subgraph.eval.experiments.SwdfExperimentSetup;
import com.d2s.subgraph.helpers.Helper;
import com.d2s.subgraph.queries.filters.DescribeFilter;
import com.d2s.subgraph.queries.filters.GraphClauseFilter;
import com.d2s.subgraph.queries.filters.QueryFilter;
import com.d2s.subgraph.queries.filters.SimpleBgpFilter;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;

public class SwdfQueries implements GetQueries {
	public static String QUERY_FILE = "src/main/resources/swdf_queries.log";
	public static String CSV_COPY = "src/main/resources/swdf_queries.csv";
	public static String PARSE_QUERIES_FILE = "src/main/resources/swdf_queries.arraylist";
	private static boolean ONLY_UNIQUE = true;
	private int invalidQueries = 0;
	private int validQueries = 0;
	private int filteredQueries = 0;
	private int duplicateQueries = 0;
	private int noResultsQueries = 0;
	private ArrayList<QueryFilter> filters;
	private HashMap<QueryWrapper, QueryWrapper> queriesHm = new HashMap<QueryWrapper, QueryWrapper>();// to avoid duplicates
	ArrayList<QueryWrapper> queries = new ArrayList<QueryWrapper>();
	private int maxNumQueries = 0;

	public SwdfQueries(QueryFilter... filters) throws IOException {
		this(true, 0, filters);
	}

	public SwdfQueries(boolean useCacheFile, int maxNumQueries, QueryFilter... filters) throws IOException {
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
				queries = new ArrayList<QueryWrapper>(queriesHm.values());
				queriesHm.clear();
			}
			saveCsvCopy(new File(CSV_COPY));
			saveQueriesToCacheFile();
		}
		
	}

	private void saveQueriesToCacheFile() throws IOException {
		FileWriter writer = new FileWriter(PARSE_QUERIES_FILE);
		for (QueryWrapper query : queries) {
			writer.write(URLEncoder.encode(query.getQuery().toString(), "UTF-8") + "\n");
		}
		writer.close();
	}
	
	private void readQueriesFromCacheFile(File cacheFile) throws FileNotFoundException, QueryParseException, UnsupportedEncodingException {
		Scanner sc = new Scanner(cacheFile);
		int queryIndex = 0;
		while(sc.hasNext()) {
			String line = sc.next();
			String queryString = line.trim();
			if (queryString.length() > 0) {
				QueryWrapper query = new QueryWrapper(URLDecoder.decode(queryString, "UTF-8"));
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

	private void addQueryToList(String queryString) {
		try {
			QueryWrapper query = new QueryWrapper(QueryFactory.create(queryString));
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
					query.generateStats();
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

	private boolean hasResults(QueryWrapper queryWrapper) {
		if (queryWrapper.toString().contains("contains(lcase(?title), \"algorithm\") || contains(lcase(?abstract), \"algorithm\")")) {
			return false;
			//this query keeps crashing virtuoso! just skip it
		}
		try {
			Query query = QueryFactory.create(queryWrapper.getQueryString(SwdfExperimentSetup.GOLDEN_STANDARD_GRAPH));
			QueryExecution queryExecution = QueryExecutionFactory.sparqlService(EvaluateGraph.OPS_VIRTUOSO, query);
			ResultSetRewindable result = ResultSetFactory.copyResults(queryExecution.execSelect());
			if (Helper.getResultSize(result) > 0) {
				return true;
			}
		} catch (QueryExceptionHTTP e) {
			System.out.println(queryWrapper.toString());
			e.printStackTrace();
			System.exit(1);
		} catch (Exception e) {
			//query wrong or something. ignore

		}
		return false;
	}

	public void setMaxNQueries(int maxNum) {
		this.maxNumQueries = maxNum;
	}

	public ArrayList<QueryWrapper> getQueries() {
		if (maxNumQueries > 0) {
			maxNumQueries = Math.min(maxNumQueries, queries.size());
			return new ArrayList<QueryWrapper>(this.queries.subList(0, maxNumQueries));
		} else {
			return this.queries;
		}
	}

	public String toString() {
		return "valids: " + validQueries + " invalids: " + invalidQueries + " filtered: " + filteredQueries + " duplicates: "
				+ duplicateQueries + " no results queries: " + noResultsQueries;
	}

	public void saveCsvCopy(File csvFile) throws IOException {
		CSVWriter writer = new CSVWriter(new FileWriter(csvFile), ';');
		writer.writeNext(new String[] { "queryId", "query" });
		for (QueryWrapper query : queries) {
			writer.writeNext(new String[] { Integer.toString(query.getQueryId()), query.toString() });
		}
		writer.close();
	}

	/**
	 * 
	 * @param query
	 * @return True if this query passed through all the filters, false if one of the filters matches
	 */
	private boolean checkFilters(QueryWrapper query) {
		boolean passed = true;
		try {
			for (QueryFilter filter : filters) {
				if (filter.filter(query)) {
					passed = false;
					break;
				}
			}
		} catch (Exception e) {
			System.out.println(query.toString());
			e.printStackTrace();
			System.exit(1);
		}
		return passed;
	}

	public static void main(String[] args) {

		try {

			SwdfQueries swdfQueries = new SwdfQueries(false, 100, new DescribeFilter(), new SimpleBgpFilter(), new GraphClauseFilter());
			System.out.println(swdfQueries.toString());
			// ArrayList<QueryWrapper> queries = qaldQueries.getQueries();

			// for (QueryWrapper query: queries) {
			// System.out.println(Integer.toString(query.getQueryId()));
			// }

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

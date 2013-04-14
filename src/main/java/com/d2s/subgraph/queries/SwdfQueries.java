package com.d2s.subgraph.queries;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import au.com.bytecode.opencsv.CSVWriter;

import com.d2s.subgraph.eval.QueryWrapper;
import com.d2s.subgraph.queries.filters.DescribeFilter;
import com.d2s.subgraph.queries.filters.QueryFilter;
import com.d2s.subgraph.queries.filters.SimpleBgpFilter;
import com.hp.hpl.jena.mem.StoreTripleIterator;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;

public class SwdfQueries implements GetQueries {
	public static String QUERY_FILE = "src/main/resources/swdf_queries.log";
	public static String CSV_COPY = "src/main/resources/swdf_queries.csv";
	private static boolean ONLY_UNIQUE = true;
	private int invalidQueries = 0;
	private int validQueries = 0;
	private int filteredQueries = 0;
	private int duplicateQueries = 0;
	private ArrayList<QueryFilter> filters;
	private HashMap<QueryWrapper,QueryWrapper> queriesHm = new HashMap<QueryWrapper, QueryWrapper>();//to avoid duplicates
	ArrayList<QueryWrapper> queries = new ArrayList<QueryWrapper>();
	
	public SwdfQueries(QueryFilter... filters) throws IOException  {
		this(QUERY_FILE, filters);
	}
	
	public SwdfQueries(String logFile, QueryFilter... filters) throws IOException  {
		this.filters = new ArrayList<QueryFilter>(Arrays.asList(filters));
		parseLogFile(new File(logFile));
		if (ONLY_UNIQUE) {
			//we have stored stuff in hashmap to keep queries unique. now get them as regular queries
			queries = new ArrayList<QueryWrapper>(queriesHm.values());
			queriesHm.clear();
		}
		saveCopyAsCsv();
	}

	
	
	private void parseLogFile(File textFile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(textFile));
		String line;
		while ((line = br.readLine()) != null) {
			String matchSubString = "/sparql?query=";
			if (line.contains(matchSubString)) {
				int startIndex = line.indexOf(matchSubString);
				startIndex += matchSubString.length();
				String firstString = line.substring(startIndex);
				String encodedUrlQuery = firstString.split(" ")[0];
				//remove other args
				String encodedSparqlQuery = encodedUrlQuery.split("&")[0];
				
				addQueryToList(URLDecoder.decode(encodedSparqlQuery, "UTF-8"));
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
						queriesHm.put(query, query);
						validQueries++;
					}
				} else {
					queries.add(query);
					validQueries++;
				}
			} else {
				filteredQueries++;
			}
			
		} catch (QueryParseException e) {
			//could not parse query, probably a faulty one. ignore!
			invalidQueries++;
		}
	}
	
	

	public ArrayList<QueryWrapper> getQueries() {
		return this.queries;
	}
	
	public String toString() {
		return "valids: " + validQueries + " invalids: " + invalidQueries + " filtered: " + filteredQueries + " duplicates: " + duplicateQueries;
	}
	
	public void saveCopyAsCsv() throws IOException {
		File csvFile = new File(CSV_COPY);
		CSVWriter writer = new CSVWriter(new FileWriter(csvFile), ';');
		writer.writeNext(new String[]{"queryId", "query"});
		int index = 0;
		for (QueryWrapper query: queries) {
			writer.writeNext(new String[]{Integer.toString(index), query.toString()});
			index++;
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
			for (QueryFilter filter: filters) {
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
	
	
	
	public static void main(String[] args)  {
		
		try {
			
			SwdfQueries swdfQueries = new SwdfQueries(new DescribeFilter(), new SimpleBgpFilter());
			System.out.println(swdfQueries.toString());
//			ArrayList<QueryWrapper> queries = qaldQueries.getQueries();
			
//			for (QueryWrapper query: queries) {
//				System.out.println(Integer.toString(query.getQueryId()));
//			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

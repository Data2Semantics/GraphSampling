package com.d2s.subgraph.queries;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Scanner;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.jena.atlas.web.HttpException;
import org.data2semantics.query.QueryCollection;
import org.data2semantics.query.filters.QueryFilter;
import org.xml.sax.SAXException;

import au.com.bytecode.opencsv.CSVWriter;

import com.d2s.subgraph.eval.Config;
import com.d2s.subgraph.eval.experiments.ExperimentSetup;
import com.d2s.subgraph.util.QueryUtils;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;


public abstract class QueriesFetcher {
	protected ArrayList<QueryFilter> filters;
	protected QueryCollection<Query> queryCollection;
	protected int invalidQueries;
	protected int filteredQueries;
	protected int duplicateQueries;
	protected int noResultsQueries;
	protected boolean useCacheFile = true;
	protected ExperimentSetup experimentSetup;
	public QueriesFetcher(ExperimentSetup experimentSetup, boolean useCacheFile, QueryFilter... filters) throws IOException {
		this.filters = new ArrayList<QueryFilter>(Arrays.asList(filters));
		queryCollection = new QueryCollection<Query>();
		this.experimentSetup = experimentSetup;
		this.useCacheFile = useCacheFile;
	}
	
	protected void fetch() throws QueryParseException, IOException, ParserConfigurationException, SAXException {
		tryFetchingQueriesFromCache();
		
		if (queryCollection.getTotalQueryCount() == 0) {
			parseLogFiles();
			saveQueriesToCacheFile();
			saveQueriesToCsv();
		}
	}
	/**
	 * 
	 * @param query
	 * @return True if this query passed through all the filters, false if one of the filters matches
	 */
	public boolean checkFilters(Query query) {
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
	public QueryCollection<Query> getQueryCollection() {
		return queryCollection;
		
	}
	
	/**
	 * executes query, and stores the query duration in query object as well!
	 * @param query
	 * @return
	 * @throws IllegalStateException
	 */
	public Query execQueryToTest(Query query) throws IllegalStateException {
		try {
			QueryExecution queryExecution = QueryExecutionFactory.sparqlService(Config.EXPERIMENT_ENDPOINT, query);
			Date start = new Date();
			ResultSet resultSet = queryExecution.execSelect();
			Date end = new Date();
			query.setGoldenStandardDuration(new Date(end.getTime() - start.getTime()));
			ResultSetRewindable resultSetRewindable = ResultSetFactory.copyResults(resultSet);
			if (QueryUtils.getResultSize(resultSetRewindable) > 0) {
				return query;
			} else {
				throw new IllegalStateException("no results for query");
			}
		} catch (QueryExceptionHTTP e) {
			e.printStackTrace();
			System.exit(1);
		} catch (Exception e) {
			throw new IllegalStateException("no results for query");

		}
		throw new IllegalStateException("no results for query");
	}
	
	public String toString() {
		return "valids: " + queryCollection.getTotalQueryCount() + " invalids: " + invalidQueries + " filtered: " + filteredQueries + " duplicates: "
				+ duplicateQueries + " no results queries: " + noResultsQueries;
	}
	

	
	/**
	 * @todo: hasresults check should only check one named graph, not all
	 * @param queryString
	 * @throws IOException
	 */
	protected void addQueryToList(String queryString) throws IOException {
		try {
			
			Query query = Query.create(queryString, queryCollection);
			if (checkFilters(query)) {
				if (queryCollection.containsQuery(query)) {
					//already added. no need to do 'hasresults' again
					queryCollection.addQuery(query);
				} else {
					System.out.print("+");
					Date timeStart = new Date();
					try {
						query = execQueryToTest(query.getQueryWithFromClause(experimentSetup.getGoldenStandardGraph()));
						System.out.println("adding");
						System.out.println(queryString);
						queryCollection.addQuery(query);
					} catch (IllegalStateException e) {
						//no results for this query!
						noResultsQueries++;
					}
					Date timeEnd = new Date();
					if ((timeEnd.getTime() - timeStart.getTime()) > 5000) {
						//longer than 5 seconds
						System.out.println("taking longer than 5 seconds:");
					}
				}
			} else {
				filteredQueries++;
			}
			
		} catch (QueryParseException e) {
			// could not parse query, probably a faulty one. ignore!
			invalidQueries++;
		}  catch (HttpException e) {
			// hmm, might be something like a time-out. ignore for now, but do show this for debugging purposes
			System.out.println(e.getMessage());
			invalidQueries++;
		}
	}
	protected void saveQueriesToCacheFile() throws IOException {
		File cacheDir = new File(Config.PATH_QUERY_CACHE);
		if (!cacheDir.exists()) cacheDir.mkdir();
		FileWriter writer = new FileWriter(Config.PATH_QUERY_CACHE + "/" + experimentSetup.getId());
		System.out.println("storing " + queryCollection.getQueries().size() + " queries to cache file");
		for (Query query : queryCollection.getQueries()) {
			writer.write(URLEncoder.encode(query.toString(), "UTF-8") + "\n");
		}
		writer.close();
	}
	protected void saveQueriesToCsv() throws IOException {
		CSVWriter writer = new CSVWriter(new FileWriter(getQueryCsvCopyLocation()), ';');
		writer.writeNext(new String[]{"query", "goldenStandardTiming"});
		for (Query query : queryCollection.getQueries()) {
			writer.writeNext(new String[]{URLEncoder.encode(query.toString(), "UTF-8"), Long.toString(query.getGoldenStandardDuration().getTime())});
		}
		writer.close();
	}
	protected void readQueriesFromCacheFile(String file) throws QueryParseException, IOException {
		Scanner sc = new Scanner(new File(file));
		while(sc.hasNext()) {
			String line = sc.next();
			String queryString = line.trim();
			if (queryString.length() > 0) {
				Query query = Query.create(URLDecoder.decode(queryString, "UTF-8"), queryCollection);
				queryCollection.addQuery(query);
			}
		}
		System.out.println("loaded " + queryCollection.getQueries().size() + " queries from cache!");
		sc.close();
	}
	
//	protected void tryFetchingQueriesFromCache(String path) throws QueryParseException, IOException {
//		if (useCacheFile(path)) {
//			System.out.println("using cache file");
//			readQueriesFromCacheFile(path);
//		} else {
//			System.out.println("not using cache file");
//		}
//	}
	protected void tryFetchingQueriesFromCache() throws QueryParseException, IOException {
		if (useCacheFile(Config.PATH_QUERY_CACHE + "/" + experimentSetup.getId())) {
			System.out.println("using cache file");
			readQueriesFromCacheFile(Config.PATH_QUERY_CACHE + "/" + experimentSetup.getId());
		} else {
			System.out.println("not using cache file");
		}
	}
	protected boolean useCacheFile(String path) {
		boolean useCacheFile = false;
		if (this.useCacheFile) { 
			System.out.println("this set");
			File file = new File(path);
			if (file.exists()) {
				System.out.println("file exists");
				try {
					BufferedReader br = new BufferedReader(new FileReader(file));
					String line;
					while ((line = br.readLine()) != null) {
						System.out.println("has file length");
						if (line.length() != 0) {
							useCacheFile = true;
							break;
						}
						
					}
					br.close();
				} catch (Exception e) {
					System.out.println("exception");
					//do nothing. just dont use cache file
				}
			}
			if (useCacheFile) {
				System.out.println("using queries from our cache file!!!");
			}
		}
		
		return useCacheFile;
	}
	public Collection<File> getQueryLogFiles(String... extensions) throws IOException {
		File logDir = new File(Config.PATH_QUERY_LOGS + "/" + experimentSetup.getId());
		if (!logDir.exists()) throw new IOException("path to fetch log files from does not exist:" + logDir.getPath());
		return FileUtils.listFiles(logDir, extensions, false);
	}
	
	public File getQueryCsvCopyLocation() {
		File dir = new File(Config.PATH_QUERY_CSV_COPY);
		if (!dir.exists()) dir.mkdir();
		return new File(dir.getPath() + "/" + experimentSetup.getId() + ".csv");
	}
	
	protected void parseLogFiles(String... etensionsArray) throws IOException, ParserConfigurationException, SAXException {
		ArrayList<String> extensions = new ArrayList<String>(Arrays.asList(etensionsArray));
		if (extensions.size() == 0) {
			//try to guess, based on query log type
			if (experimentSetup.getLogType() == ExperimentSetup.LogType.CLF) {
				extensions.add("log");
			}
		}
		for (File logFile: getQueryLogFiles(extensions.toArray(new String[extensions.size()]))) {
			try {
				parseLogFile(logFile, experimentSetup.getLogType());
			} catch (IllegalArgumentException e) {
				parseCustomLogFile(logFile);
			}
		}
	}
	protected void parseLogFile(File logFile, ExperimentSetup.LogType logType) throws UnsupportedEncodingException, IOException {
		if (logType == ExperimentSetup.LogType.CLF) {
			parseClfLogFile(logFile);
		} else if (logType == ExperimentSetup.LogType.PLAIN_TEXT) {
			parsePlainTextLogFile(logFile);
		} else {
			throw new IllegalArgumentException("we don't have a parser for this log file");
		}
		
	}
	private void parsePlainTextLogFile(File logFile) {
		// TODO Auto-generated method stub
		
	}
	private void parseClfLogFile(File logFile) throws UnsupportedEncodingException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(logFile));
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
				if (queryCollection.getDistinctQueryCount() > experimentSetup.getMaxNumQueries()) {
					break;
				}
			}
		}
		br.close();
		
	}
	public static void main(String[] args) throws IOException {
		String query = "SELECT DISTINCT ?p ?o\n" + 
				"FROM <http://swdf>\n" + 
				"WHERE\n" + 
				"  { <http://data.semanticweb.org/ns/swc/ontology> ?p ?o }\n" + 
				"GROUP BY ?o";
		Query.create(query);
	}
	
	protected abstract void parseCustomLogFile(File logFile) throws IOException, ParserConfigurationException, SAXException, IllegalArgumentException;
}

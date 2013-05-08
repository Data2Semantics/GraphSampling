package com.d2s.subgraph.queries;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import au.com.bytecode.opencsv.CSVWriter;

import com.d2s.subgraph.eval.EvaluateGraph;
import com.d2s.subgraph.eval.experiments.Sp2bExperimentSetup;
import com.d2s.subgraph.helpers.Helper;
import com.d2s.subgraph.queries.filters.QueryFilter;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;

public class Sp2bQueries implements GetQueries {
	private static String QUERY_DIR = "src/main/resources/sp2bQueries";
	private static String QUERY_FILE_EXTENSION = "sparql";
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


	public Sp2bQueries(QueryFilter... filters) throws IOException {
		System.out.println("parsing sp2b query logs");
		this.filters = new ArrayList<QueryFilter>(Arrays.asList(filters));
		parseQueryDir();
		if (ONLY_UNIQUE) {
			// we have stored stuff in hashmap to keep queries unique. now get them as regular queries
			queries = new ArrayList<QueryWrapper>(queriesHm.values());
			queriesHm.clear();
		}
	}

	private void parseQueryDir() throws IOException {
		File queryDir = new File(QUERY_DIR);
		if (!queryDir.exists()) {
			throw new IOException("Query dir " + QUERY_DIR + " does not exist");
		}
		
		
		File [] queryFiles = queryDir.listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.endsWith("." + QUERY_FILE_EXTENSION);
		    }
		});
		
		for (File queryFile: queryFiles) {
			addQueryFileToList(queryFile);
			if (queries.size() > maxNumQueries) {
				break;
			}
			
		}
	}

	private void addQueryFileToList(File queryFile) throws IOException {
		
		String queryString = FileUtils.readFileToString(queryFile);
		String basename = FilenameUtils.removeExtension(queryFile.getName());
		int queryId = Integer.parseInt(basename.substring(1));
		try {
			QueryWrapper query = new QueryWrapper(QueryFactory.create(queryString));
			query.setQueryId(queryId);
			if (checkFilters(query)) {
				if (ONLY_UNIQUE) {
					if (queriesHm.containsKey(query)) {
						duplicateQueries++;
					} else {
						if (hasResults(query)) {
							query.setQueryId(validQueries);
							queriesHm.put(query, query);
							validQueries++;
						} else {
							noResultsQueries++;
						}
					}
				} else {
					queries.add(query);
					validQueries++;
				}
				query.generateStats();
			} else {
				filteredQueries++;
			}
		} catch (QueryParseException e) {
			// could not parse query, probably a faulty one. ignore!
			invalidQueries++;
		}
	}

	private boolean hasResults(QueryWrapper queryWrapper) {
		try {
			Query query = QueryFactory.create(queryWrapper.getQueryString(Sp2bExperimentSetup.GOLDEN_STANDARD_GRAPH));
			QueryExecution queryExecution = QueryExecutionFactory.sparqlService(EvaluateGraph.OPS_VIRTUOSO, query);
			ResultSetRewindable result = ResultSetFactory.copyResults(queryExecution.execSelect());
			if (Helper.getResultSize(result) > 0) {
				return true;
			}
		} catch (QueryExceptionHTTP e) {
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
			Sp2bQueries swdfQueries = new Sp2bQueries();
//			Sp2bQueries swdfQueries = new Sp2bQueries(new DescribeFilter(), new SimpleBgpFilter(), new GraphClauseFilter());
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

package com.d2s.subgraph.queries;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import org.data2semantics.query.QueryCollection;
import org.data2semantics.query.filters.QueryFilter;
import com.d2s.subgraph.eval.Config;
import com.d2s.subgraph.helpers.Helper;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;


public abstract class QueryFetcher {
	protected ArrayList<QueryFilter> filters;
	protected int maxNumQueries = 0;
	protected QueryCollection<Query> queryCollection;
	protected int invalidQueries;
	protected int filteredQueries;
	protected int duplicateQueries;
	protected int noResultsQueries;
	
	public QueryFetcher() throws IOException {
		queryCollection = new QueryCollection<Query>();
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
	public void setMaxNQueries(int maxNum) {
		this.maxNumQueries = maxNum;
	}
	
	public boolean hasResults(Query query) {
		try {
			QueryExecution queryExecution = QueryExecutionFactory.sparqlService(Config.EXPERIMENT_ENDPOINT, query);
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
					if (hasResults(query)) {
						queryCollection.addQuery(query);
					} else {
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
		}
	}
	protected void saveQueriesToCacheFile(String file) throws IOException {
		FileWriter writer = new FileWriter(file);
		for (Query query : queryCollection.getQueries()) {
			for (int i = 0; i < query.getCount(); i++) {
				writer.write(URLEncoder.encode(query.toString(), "UTF-8") + "\n");
			}
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
		sc.close();
	}
}

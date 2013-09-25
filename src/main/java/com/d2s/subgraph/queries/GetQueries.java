package com.d2s.subgraph.queries;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.data2semantics.query.filters.QueryFilter;
import au.com.bytecode.opencsv.CSVWriter;
import com.d2s.subgraph.eval.EvaluateGraph;
import com.d2s.subgraph.helpers.Helper;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;


public abstract class GetQueries {
	protected ArrayList<QueryFilter> filters;
	protected int maxNumQueries = 0;
	protected ArrayList<Query> queries = new ArrayList<Query>();
	protected HashMap<Query, Query> queriesHm = new HashMap<Query, Query>();// to avoid duplicates
	protected int validQueries;
	protected int invalidQueries;
	protected int filteredQueries;
	protected int duplicateQueries;
	protected int noResultsQueries;
	
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
	public ArrayList<Query> getQueries() {
		if (maxNumQueries > 0) {
			maxNumQueries = Math.min(maxNumQueries, queries.size());
			return new ArrayList<Query>(this.queries.subList(0, maxNumQueries));
		} else {
			return this.queries;
		}
	}
	public void setMaxNQueries(int maxNum) {
		this.maxNumQueries = maxNum;
	}
	
	public boolean hasResults(Query query) {
		try {
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
	public void saveCsvCopy(File csvFile) throws IOException {
		CSVWriter writer = new CSVWriter(new FileWriter(csvFile), ';');
		writer.writeNext(new String[] { "queryId", "query" });
		for (Query query : queries) {
			writer.writeNext(new String[] { Integer.toString(query.getQueryId()), query.toString() });
		}
		writer.close();
	}
	public String toString() {
		return "valids: " + validQueries + " invalids: " + invalidQueries + " filtered: " + filteredQueries + " duplicates: "
				+ duplicateQueries + " no results queries: " + noResultsQueries;
	}
}

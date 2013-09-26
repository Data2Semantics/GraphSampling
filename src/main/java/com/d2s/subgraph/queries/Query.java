package com.d2s.subgraph.queries;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.data2semantics.query.QueryCollection;

import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.sparql.core.Prologue;

public class Query extends org.data2semantics.query.Query {
	private int queryId;
	private boolean onlyDbo;
	private ResultSetRewindable goldenStandardResults = null;
	private ArrayList<HashMap<String, String>> answers = new ArrayList<HashMap<String, String>>();
	public Query(){}
	public Query(Prologue prologue) {
		super(prologue);
	}
	
	public int getQueryId() {
		return queryId;
	}

	public void setQueryId(int queryId) {
		this.queryId = queryId;
	}
	
	public boolean isOnlyDbo() {
		return onlyDbo;
	}
	public void setOnlyDbo(boolean onlyDbo) {
		this.onlyDbo = onlyDbo;
	}
	
	
	public static Query create(String queryString, QueryCollection<Query> queryCollection) {
		Query query = new Query();
		query = (Query)(QueryFactory.parse(query, queryString, null, Syntax.defaultQuerySyntax));
		query.setQueryCollection(queryCollection);
		query.generateQueryStats();
		return query;
	}
	public ArrayList<HashMap<String, String>> getAnswers() {
		return answers;
	}
	public void setAnswers(ArrayList<HashMap<String, String>> answers) {
		this.answers = answers;
	}
	public ResultSetRewindable getGoldenStandardResults() {
		return goldenStandardResults;
	}
	public void setGoldenStandardResults(ResultSetRewindable goldenStandardResults) {
		this.goldenStandardResults = goldenStandardResults;
	}
	public static Query create(String queryString) throws IOException {
		return create(queryString, new QueryCollection<Query>());
	}
}

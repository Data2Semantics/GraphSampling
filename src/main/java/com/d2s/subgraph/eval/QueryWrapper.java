package com.d2s.subgraph.eval;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;

public class QueryWrapper {
	private boolean isSelect = true;
	private boolean isAsk = false;
	private boolean aggregation;
	private boolean onlyDbo;
	private String answerType; 
	private String query;
	private ArrayList<HashMap<String, String>> answers = new ArrayList<HashMap<String, String>>();
	private static String SELECT_REGEX = ".*SELECT.*";
	private static String ASK_REGEX = ".*ASK.*";
	public QueryWrapper(String query) {
		setQuery(query);
	}
	public QueryWrapper() {
		
	}
	
	private boolean evalRegex(String string, String regexString, int modifiers) {
		Pattern regex = Pattern.compile(regexString, modifiers);
		Matcher regexMatcher = regex.matcher(string);
		return regexMatcher.find();
	}
	
	public boolean isSelect() {
		return isSelect;
	}
	
	public boolean isAsk() {
		return isAsk;
	}
	
	public String getQuery() {
		return this.query;
	}
	
	public String getQuery(String fromGraph) {
		Query query = QueryFactory.create(this.query);
		query.addGraphURI(fromGraph);
		return query.toString();
	}
	
	public void setQuery(String query) {
		this.query = query;
		isSelect = evalRegex(query, SELECT_REGEX, Pattern.MULTILINE);
		isAsk = evalRegex(query, ASK_REGEX, Pattern.MULTILINE);
		if (!(isSelect ^ isAsk)) {
			throw new RuntimeException("Unable to detect whether query is select or ask. Select: " + (isSelect? "yes":"no") + ", ASK: " + (isAsk? "yes": "no") + ". Query: " + query);
		}
	}
	
	public String toString() {
		return "It is " + (isSelect()? "": "not ") + "a select query, and it is " + (isAsk()? "": "not ") + "an ask query"; 
	}
	
	public ArrayList<HashMap<String, String>> getAnswers() {
		return answers;
	}

	public void setAnswers(ArrayList<HashMap<String, String>> answers) {
		this.answers = answers;
	}
	public static void main(String[] args)  {
		String query = "			PREFIX rdf: &lt;http://www.w3.org/1999/02/22-rdf-syntax-ns#&gt;\n" + 
				"			PREFIX onto: &lt;http://dbpedia.org/ontology/&gt;\n" + 
				"			PREFIX rdfs: &lt;http://www.w3.org/2000/01/rdf-schema#&gt;\n" + 
				"			ASK\n" + 
				"			WHERE\n" + 
				"			{\n" + 
				"			?software rdf:type onto:Software .\n" + 
				"			?software rdfs:label ?name .\n" + 
				"			FILTER (regex(?name, 'Battle Chess'))\n" + 
				"			}";
		QueryWrapper evalQuery = new QueryWrapper(query);
		System.out.println(evalQuery.toString());
		
	}
	public boolean isAggregation() {
		return aggregation;
	}
	public void setAggregation(boolean aggregation) {
		this.aggregation = aggregation;
	}
	public String getAnswerType() {
		return answerType;
	}
	public void setAnswerType(String answerType) {
		this.answerType = answerType;
	}
	public boolean isOnlyDbo() {
		return onlyDbo;
	}
	public void setOnlyDbo(boolean onlyDbo) {
		this.onlyDbo = onlyDbo;
	}
	
	
}

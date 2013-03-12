package com.d2s.partialreplication.queries;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EvalQuery {
	private boolean isSelect = true;
	private boolean isAsk = false;
	private String query;
	private HashMap<String, String> answers = new HashMap<String, String>();
	private static String SELECT_REGEX = ".*SELECT.*";
	private static String ASK_REGEX = ".*ASK.*";
	public EvalQuery(String query) {
		this.query = query;
		isSelect = evalRegex(query, SELECT_REGEX, Pattern.MULTILINE);
		isAsk = evalRegex(query, ASK_REGEX, Pattern.MULTILINE);
		if (!(isSelect ^ isAsk)) {
			throw new RuntimeException("Unable to detect whether query is select or ask. Select: " + (isSelect? "yes":"no") + ", ASK: " + (isAsk? "yes": "no") + ". Query: " + query);
		}
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
	
	public String toString() {
		return "It is " + (isSelect()? "": "not ") + "a select query, and it is " + (isAsk()? "": "not ") + "an ask query"; 
	}
	
	public HashMap<String, String> getAnswers() {
		return answers;
	}

	public void setAnswers(HashMap<String, String> answers) {
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
		EvalQuery evalQuery = new EvalQuery(query);
		System.out.println(evalQuery.toString());
		
	}
}

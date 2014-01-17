package com.d2s.subgraph.eval.results;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import com.d2s.subgraph.queries.Query;

public class SampleResultsRandom extends SampleResults{
	public SampleResultsRandom() throws IOException {
		super();
	}

	private ArrayList<SampleResults> allGraphResults = new ArrayList<SampleResults>();
	
	public void add(Query result) {
		//do nothing. (this object is more of a wrapper for other graph results objects)
		throw new UnsupportedOperationException("Adding query(results) to this method is not supported (this object is more of a wrapper for other graph results objects)");
	}
	
	/**
	 * aggregate these individual sample results to this one sample resultset
	 * @param individualSampleResults
	 */
	public void add(ArrayList<SampleResults> allGraphResults) {
		this.allGraphResults = allGraphResults;
		aggregateToSingleObject();
	}
	
	public void aggregateToSingleObject() {
		//set graph name
		String exampleGraphName = allGraphResults.get(0).getGraphName();
		String pattern = "(.*sample)(-\\d+)(.*)"; //remove the appended number from this run
		setGraphName(exampleGraphName.replaceAll(pattern, "$1$3")); 
		
		ArrayList<Query> queries = new ArrayList<Query>(allGraphResults.get(0).getQueryCollection().getQueries());
		for (Query query: queries) {
			QueryResultsSample queryResultsSample = new QueryResultsSample();
			ArrayList<Query> allQueryResults = new ArrayList<Query>();
			for (SampleResults graphResults: allGraphResults) {
				allQueryResults.add(graphResults.getQueryCollection().getQuery(query.toString()));
			}
			queryResultsSample.add(allQueryResults);
			query.setResults(queryResultsSample);
//			results.put(queryId, queryResultsSample);
			queryCollection.addQuery(query);
		}
	}
	
	


	public String toString() {
		return getGraphName();
	}
	public String getShortGraphName() {
		ArrayList<String> parts = new ArrayList<String>(Arrays.asList( graphName.split("_")));
		parts.remove(0); //http://dbp
		String shortGraphname = "";
		for (String part: parts) {
			shortGraphname += part;
		}
		shortGraphname = shortGraphname.replace(".nt", "");
		return shortGraphname;
	}
	
	public String getProperName() {
		if (graphName.contains("0.5")) {
			return "sample 50%";
		} else {
			return "sample 20%";
		}
		
	}

	public String getRewriteMethod() {
		return "Baseline";
	}
	public String getAlgorithm() {
		return "sample";
	}
	
	public String getPercentage() {
		return "50%";
	}

	public static void main(String[] args)  {
		try {
//			EvaluateGraphs evaluate = new EvaluateGraphs(new DbpExperimentSetup());
			SampleResultsRandom results = new SampleResultsRandom();
			results.aggregateToSingleObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
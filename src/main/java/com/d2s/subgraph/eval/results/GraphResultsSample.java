package com.d2s.subgraph.eval.results;

import java.util.ArrayList;
import java.util.Arrays;

public class GraphResultsSample extends GraphResults{
	private ArrayList<GraphResults> allGraphResults = new ArrayList<GraphResults>();
	
	public void add(QueryResults result) {
		//do nothing. (this object is more of a wrapper for other graph results objects)
	}
	
	/**
	 * aggregate these indivudal sample results to this one sample resultset
	 * @param individualSampleResults
	 */
	public void add(ArrayList<GraphResults> allGraphResults) {
		this.allGraphResults = allGraphResults;
		aggregateToSingleObject();
	}
	
	public void aggregateToSingleObject() {
		//set graph name
		String exampleGraphName = allGraphResults.get(0).getGraphName();
		String pattern = "(.*sample)(-\\d+)(.*)"; //remove the appended number from this run
		setGraphName(exampleGraphName.replaceAll(pattern, "$1$3")); 
		
		ArrayList<Integer> queryIds = allGraphResults.get(0).getQueryIds();
		for (int queryId: queryIds) {
			QueryResultsSample queryResultsSample = new QueryResultsSample();
			ArrayList<QueryResults> allQueryResults = new ArrayList<QueryResults>();
			for (GraphResults graphResults: allGraphResults) {
				allQueryResults.add(graphResults.get(queryId));
			}
			queryResultsSample.add(allQueryResults);
			results.put(queryId, queryResultsSample);
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
	
	public int getPercentage() {
		return 50;
	}

	public static void main(String[] args)  {
		try {
//			EvaluateGraphs evaluate = new EvaluateGraphs(new DbpExperimentSetup());
			GraphResultsSample results = new GraphResultsSample();
			results.aggregateToSingleObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
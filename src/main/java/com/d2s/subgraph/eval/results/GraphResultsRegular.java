package com.d2s.subgraph.eval.results;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import com.d2s.subgraph.helpers.Helper;
import com.d2s.subgraph.queries.Query;

public class GraphResultsRegular extends GraphResults {
	public GraphResultsRegular() throws IOException {
		super();
	}
	public void add(Query query) {
		queryCollection.addQuery(query);
	}
	protected String getRewriteMethod() {
		String rewriteMethod = Helper.getRewriteMethodAsString(graphName);
		if (rewriteMethod.length() == 0) {
			if (graphName.contains("Baseline")) {
				rewriteMethod = "Baseline";
			} else {
				rewriteMethod = graphName;
			}
		}
		return rewriteMethod;
	}
	
	protected String getProperName() {
		int rewriteMethod = Helper.getRewriteMethod(getGraphName());
		int analysisAlg = Helper.getAnalysisAlgorithm(getGraphName());
		String properName = "";
		if (rewriteMethod == Helper.REWRITE_NODE1) {
			properName += "Node1";
		} else if (rewriteMethod == Helper.REWRITE_NODE2) {
			properName += "Node2";
		} else if (rewriteMethod == Helper.REWRITE_NODE3) {
			properName += "Node3";
		} else if (rewriteMethod == Helper.REWRITE_NODE4) {
			properName += "Node4";
		} else if (rewriteMethod == Helper.REWRITE_PATH) {
			properName += "Path";
		}
		if (analysisAlg == Helper.ALG_BETWEENNESS) {
			properName += " betweenness";
		} else if (analysisAlg == Helper.ALG_EIGENVECTOR) {
			properName += " eigenvector";
		} else if (analysisAlg == Helper.ALG_INDEGREE) {
			properName += " indegree";
		} else if (analysisAlg == Helper.ALG_OUTDEGREE) {
			properName += " outdegree";
		} else if (analysisAlg == Helper.ALG_PAGERANK) {
			properName += " pagerank";
		}
		properName += " " + getPercentage() + "%";
		return properName;
	}
	protected String getShortGraphName() {
		String graphName = getGraphName();
		graphName = graphName.replace("unweighted_", "");
		graphName = graphName.replace(".nt", "");
		ArrayList<String> parts = new ArrayList<String>(Arrays.asList( graphName.split("_")));
		parts.remove(0); //http://dbp
		String shortGraphname = "";
		for (String part: parts) {
			shortGraphname += part + "_";
		}
		shortGraphname = shortGraphname.substring(0, shortGraphname.length()-1);//remove trailing _
		return shortGraphname;
		
	}
	protected String getAlgorithm() {
		String algorithm = Helper.getAlgorithmAsString(graphName);
		if (algorithm.length() == 0) {
			algorithm = graphName;
			if (graphName.contains("Baseline")) {
				algorithm = "resource frequency";
			} else {
				algorithm = graphName;
			}
		}
		return algorithm;
	}
	protected int getPercentage() {
		String graphName = getGraphName();
		ArrayList<String> parts = new ArrayList<String>(Arrays.asList( graphName.split("-")));
		String trailingBit = parts.get(parts.size()-1);
		trailingBit = trailingBit.replace(".nt", "");
		return Integer.parseInt(trailingBit);
	}

}
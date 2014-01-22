package com.d2s.subgraph.eval.results;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.data2semantics.query.QueryCollection;

import com.d2s.subgraph.queries.Query;
import com.d2s.subgraph.util.StringUtils;

public class SampleResultsRegular extends SampleResults {
	public SampleResultsRegular() throws IOException {
		super();
	}
	public void add(Query query) {
		queryCollection.addQuery(query);
	}
	public String getRewriteMethod() {
		String rewriteMethod = StringUtils.getRewriteMethodAsString(graphName);
		if (rewriteMethod.length() == 0) {
			if (graphName.contains("Baseline")) {
				rewriteMethod = "Baseline";
			} else {
				rewriteMethod = graphName;
			}
		}
		return rewriteMethod;
	}
	
	public String getProperName() {
		String properName = "";
		properName += StringUtils.getRewriteMethodAsString(getGraphName());
		properName += " " + StringUtils.getAlgorithmAsString(getGraphName());
		properName += " " + getPercentage();
		return properName;
	}
	public String getShortGraphName() {
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
	public String getAlgorithm() {
		String algorithm = StringUtils.getAlgorithmAsString(graphName);
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
//	public int getPercentage() {
//		String graphName = getGraphName();
//		ArrayList<String> parts = new ArrayList<String>(Arrays.asList( graphName.split("-")));
//		String trailingBit = parts.get(parts.size()-1);
//		trailingBit = trailingBit.replace(".nt", "");
//		return Integer.parseInt(trailingBit);
//	}
	public void setQueryCollection(QueryCollection<Query> queryCollection) {
		this.queryCollection = queryCollection;
	}
	@Override
	public SampleType getSampleType() {
		if (getGraphName().toLowerCase().contains("freqbaseline")) {
			return SampleType.BASELINE_FREQ;
		} else {
			return SampleType.REGULAR;
		}
	}
}
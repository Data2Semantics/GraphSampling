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
		String rewriteMethod = "";
		if (getSampleType() == SampleType.REGULAR) {
			rewriteMethod = StringUtils.getRewriteMethodAsString(graphName);
		}
		return rewriteMethod;
	}
	
	public String getProperName() {
		String properName = "";
		if (getSampleType() == SampleType.BASELINE_FREQ) {
			properName = "resource frequency " + getPercentage();
		} else {
			properName += StringUtils.getRewriteMethodAsString(getGraphName());
			properName += " " + StringUtils.getAlgorithmAsString(getGraphName());
			properName += " " + getPercentage();
		}
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
		if (shortGraphname.length() > 0) {
			shortGraphname = shortGraphname.substring(0, shortGraphname.length()-1);//remove trailing _
		}
		return shortGraphname;
		
	}
	public String getAlgorithm() {
		String algorithm = "";
		if (getSampleType() == SampleType.REGULAR) {
			algorithm = StringUtils.getAlgorithmAsString(graphName);
			
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
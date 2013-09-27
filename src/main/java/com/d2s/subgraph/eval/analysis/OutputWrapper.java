package com.d2s.subgraph.eval.analysis;

import java.io.File;
import java.util.ArrayList;

import org.data2semantics.query.QueryCollection;

import com.d2s.subgraph.eval.experiments.ExperimentSetup;
import com.d2s.subgraph.eval.results.GraphResults;
import com.d2s.subgraph.queries.Query;

public abstract class OutputWrapper {
	protected ArrayList<GraphResults> allGraphResults = new ArrayList<GraphResults>();
	protected QueryCollection<Query> queryCollection;
	protected ExperimentSetup experimentSetup;
	protected File resultsDir;
	public OutputWrapper(ExperimentSetup experimentSetup, ArrayList<GraphResults> allGraphResults, QueryCollection<Query> queryCollection, File resultsDir) {
		this.experimentSetup = experimentSetup;
		this.allGraphResults = allGraphResults;
		this.queryCollection = queryCollection;
		this.resultsDir = resultsDir;
		
	}
	
}

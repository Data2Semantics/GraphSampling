package com.d2s.subgraph.eval.experiments;

import org.data2semantics.query.QueryCollection;

import com.d2s.subgraph.queries.Query;

public abstract class ExperimentSetup {
	public enum LogType {
		CLF,
		PLAIN_TEXT,
		OTHER
	}
	protected boolean useCacheFile = true;
	
	public ExperimentSetup(boolean useCacheFile) {
		this.useCacheFile = useCacheFile;
	}
	
	public abstract String getGoldenStandardGraph();

	public abstract String getGraphPrefix();

	public abstract QueryCollection<Query> getQueryCollection();

	public abstract String getEvalResultsDir();

	public abstract int getMaxNumQueries();

	public abstract String getQueryResultsDir();

	public abstract boolean privateQueries();

	public abstract boolean useUniqueQueries();

	public String getId() {
		return this.getClass().getSimpleName().toLowerCase().replace("experimentsetup", "");
	}
	public abstract LogType getLogType();
	

}

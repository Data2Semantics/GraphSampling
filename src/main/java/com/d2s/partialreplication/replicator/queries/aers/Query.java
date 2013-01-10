package com.d2s.partialreplication.replicator.queries.aers;


public interface Query {

	public String getInsertQuery();
	
	public String getSelectAllQuery();
	
	public String getSelectExampleQuery();

	public String getConstructQuery();

}

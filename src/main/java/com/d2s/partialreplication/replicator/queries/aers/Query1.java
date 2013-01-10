package com.d2s.partialreplication.replicator.queries.aers;

import com.d2s.partialreplication.helpers.Prefixes;


public class Query1  implements Query {
	private static String BGP = "?patient rdf:type patient:Patient.\n"
			+ "?patient rdfs:label ?patientID.\n";
	public String getInsertQuery() {
		return null;
	}
	
	public String getSelectAllQuery() {
		String query = Prefixes.PATIENT;
		query += "SELECT ?patientID {\n" + BGP + "}";
		return query;
	}
	
	//same as select all
	public String getSelectExampleQuery() {
		return getSelectAllQuery();
	}

	public String getConstructQuery() {
		String query = Prefixes.PATIENT;
		query += "CONSTRUCT {" +
				BGP +
				"} WHERE {" + 
				BGP +
				"}";
		return query;
	}

}

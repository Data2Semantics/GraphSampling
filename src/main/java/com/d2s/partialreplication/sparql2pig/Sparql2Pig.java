package com.d2s.partialreplication.sparql2pig;

import java.io.IOException;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;

public class Sparql2Pig {
	

	public static void main(String[] args)  {
		String queryString = "SELECT * {?x ?y ?h} LIMIT 1000";
		String endpoint = "http://aers.data2semantics.org/sparql/";

		Sparql2Csv rdf2csv = new Sparql2Csv(endpoint, queryString, false, Sparql2Csv.DEFAULT_CSV_FILE, '\t');
		try {
			rdf2csv.start();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MalformedQueryException e) {
			e.printStackTrace();
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		}
	}
}

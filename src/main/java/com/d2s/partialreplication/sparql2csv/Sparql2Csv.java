package com.d2s.partialreplication.sparql2csv;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

import au.com.bytecode.opencsv.CSVWriter;

public class Sparql2Csv {
	private static String DEFAULT_CSV_FILE = "output.csv";
	private static char DEFAULT_DELIMITER = ',';
	private static boolean DEFAULT_INCLUDE_HEADERS = false;

	private String csvFile;
	private char delimiter;
	private String endpoint;
	private String queryString;
	private boolean includeHeaders;
	
	CSVWriter writer;
	List<String> varNames;
	public Sparql2Csv(String endpoint, String query) {
		this(endpoint, query, DEFAULT_INCLUDE_HEADERS, DEFAULT_CSV_FILE, DEFAULT_DELIMITER);
	}

	public Sparql2Csv(String endpoint, String query, boolean includeHeaders) {
		this(endpoint, query, includeHeaders, DEFAULT_CSV_FILE, DEFAULT_DELIMITER);
	}

	public Sparql2Csv(String endpoint, String query, boolean includeHeaders, String file) {
		this(endpoint, query, includeHeaders, file, DEFAULT_DELIMITER);
	}

	public Sparql2Csv(String endpoint, String queryString, boolean includeHeaders, String file, char delimiter) {
		this.csvFile = file;
		this.delimiter = delimiter;
		this.endpoint = endpoint;
		this.queryString = queryString;
		this.includeHeaders = includeHeaders;
	}

	public void start() throws IOException, MalformedQueryException, QueryEvaluationException {
		createCsv();
	}

	private void createCsv() throws IOException, MalformedQueryException, QueryEvaluationException {

		// HTTPRepository endpoint = new HTTPRepository(this.endpoint);
		// endpoint.initialize();
		//
		// RepositoryConnection conn =
		// endpoint.getConnection();
		// try {
		// TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		// TupleQueryResult result = query.evaluate();
		// while (result.hasNext()) {
		// System.out.println("woei");
		// }
		// }
		// finally {
		// conn.close();
		// }
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);

		ResultSet results = qexec.execSelect();
		if (results.hasNext()) {
			writer = new CSVWriter(new FileWriter(csvFile), delimiter);
			varNames = results.getResultVars();
			if (includeHeaders) {
				writeHeaders();
			}
			while (results.hasNext()) {
				writeSolution(results.next());
			}
			writer.close();
			System.out.println("done");
		} else {
			System.out.println("No query results");
		}
		// ResultSetFormatter.out(System.out, results, query);
		qexec.close();
	}

	private void writeHeaders() {
		writer.writeNext(varNames.toArray(new String[varNames.size()]));
	}

	private void writeSolution(QuerySolution solution) {
		List<String> row = new ArrayList<String>();
		for (String varName: varNames) {
			RDFNode value = solution.get(varName);
			if (value == null) {
				//in projection, but not in query results
				row.add("");
			} else {
				row.add(value.toString());
			}
		}
		writer.writeNext(row.toArray(new String[row.size()]));
	}

	public static void main(String[] args)  {
		String queryString = "SELECT * {?x ?y ?h} LIMIT 10";
		String endpoint = "http://aers.data2semantics.org/sparql/";

		Sparql2Csv rdf2csv = new Sparql2Csv(endpoint, queryString, true);
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

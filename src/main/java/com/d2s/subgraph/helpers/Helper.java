package com.d2s.subgraph.helpers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryException;

import com.d2s.subgraph.eval.EvaluateGraphs;
import com.d2s.subgraph.eval.experiments.SwdfExperimentSetup;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.resultset.ResultSetRewindable;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementVisitorBase;
import com.hp.hpl.jena.sparql.syntax.ElementWalker;
import com.hp.hpl.jena.sparql.syntax.Template;

public class Helper {
	private static String HEADER_CONTENT = "application/x-www-form-urlencoded";
	private static String HEADER_ACCEPT_QUERY = "application/sparql-results+json";
	private static String HEADER_ACCEPT_CONSTRUCT = "text/turtle";
	private static String ADDITIONAL_ARGS = "&soft-limit=-1";

	public static InputStream executeQuery(String endpoint, String queryString) {
		InputStream result = execHttpPost(endpoint, "query=" + queryString + ADDITIONAL_ARGS, HEADER_ACCEPT_QUERY);
		return result;
	}

	public static InputStream executeConstruct(String endpoint, String queryString) {
		InputStream result = execHttpPost(endpoint, "query=" + queryString + ADDITIONAL_ARGS, HEADER_ACCEPT_CONSTRUCT);
		return result;
	}

	public static InputStream execHttpPost(String url, String content, String acceptHeader) {
		try {
			StringEntity e = new StringEntity(content, "UTF-8");
			e.setContentType(HEADER_CONTENT);
			HttpPost httppost = new HttpPost(url);
			httppost.setHeader("Accept", acceptHeader);
			// Execute
			HttpClient httpclient = new DefaultHttpClient();
			httppost.setEntity(e);
			HttpResponse response = httpclient.execute(httppost);

			return response.getEntity().getContent();

		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
		return null;
	}

	public static void writeStreamToOutput(InputStream stream) throws IOException {
		writeStreamToOutput(stream, new CleanTurtle() {

			public String processLine(String input) {
				return input;
			}
		});
	}

	public static void writeStreamToOutput(InputStream stream, CleanTurtle cleanTurtle) throws IOException {
		BufferedReader rd = new BufferedReader(new InputStreamReader(stream));
		String line;
		while ((line = rd.readLine()) != null) {
			System.out.println(cleanTurtle.processLine(line));
		}
	}

	public static String getStreamAsString(InputStream stream) throws IOException {
		return getStreamAsString(stream, new CleanTurtle() {

			public String processLine(String input) {
				return input;
			}
		});
	}

	public static String getStreamAsString(InputStream stream, CleanTurtle cleanTurtle) throws IOException {
		BufferedReader rd = new BufferedReader(new InputStreamReader(stream));
		String line;
		String result = "";
		while ((line = rd.readLine()) != null) {
			result += cleanTurtle.processLine(line) + "\n";
		}
		return result;
	}

	public static void writeStreamToFile(InputStream stream, String fileLocation) throws IOException {
		writeStreamToFile(stream, fileLocation, new CleanTurtle() {

			public String processLine(String input) {
				return input;
			}
		});
	}

	public static void writeStreamToFile(InputStream stream, String fileLocation, CleanTurtle cleanTurtle) {
		BufferedWriter writer = null;
		try {
			// write the inputStream to a FileOutputStream
			writer = new BufferedWriter(new FileWriter(fileLocation));
			BufferedReader rd = new BufferedReader(new InputStreamReader(stream));
			String line;
			while ((line = rd.readLine()) != null) {
				writer.write(cleanTurtle.processLine(line));
				writer.newLine();
			}
			System.out.println("New file created!");
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} finally {
			// Close the BufferedWriter
			try {
				if (writer != null) {
					writer.flush();
					writer.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public static int getResultSize(ResultSetRewindable resultSet) {
		resultSet.reset();
		int result = 0;
		while (resultSet.hasNext()) {
			resultSet.next();
			result++;
		}
		return result;
	}

	public static int getResultSize(TupleQueryResult resultSet) throws QueryEvaluationException {
		int result = 0;
		while (resultSet.hasNext()) {
			resultSet.next();
			result++;
		}
		return result;
	}

	public static String boolAsString(boolean bool) {
		if (bool) {
			return "1";
		} else {
			return "0";
		}
	}

	public static Query getAsConstructQuery(Query origQuery) {
		Query constructQuery = origQuery.cloneQuery();
		constructQuery.setQueryConstructType();

		final BasicPattern constructBp = new BasicPattern();

		Element queryPattern = origQuery.getQueryPattern();
		ElementWalker.walk(queryPattern, new ElementVisitorBase() {
			public void visit(ElementPathBlock el) {
				Iterator<TriplePath> triples = el.patternElts();
				while (triples.hasNext()) {
					constructBp.add(triples.next().asTriple());
				}
			}
		});
		constructQuery.setConstructTemplate(new Template(constructBp));
		return constructQuery;
	}

	public static ArrayList<String> getIntAsString(ArrayList<Integer> intArrayList) {
		ArrayList<String> stringArrayList = new ArrayList<String>();
		for (Integer integer : intArrayList) {
			stringArrayList.add(Integer.toString(integer));
		}
		return stringArrayList;
	}

	public static String getAsRoundedString(double value, int precision) {
		// formatting numbers upto 3 decimal places in Java
		String decimalFormat = "#,###,##0.";
		for (int i = 0; i < precision; i++) {
			decimalFormat += "0";
		}
		DecimalFormat df = new DecimalFormat(decimalFormat);
		return df.format(value);
	}

	public static void main(String[] args) {
		System.out.println(Helper.getAsRoundedString(0.0149, 4));
	}

	public static String getDoubleAsFormattedString(double value) {
		String doubleString = Helper.getAsRoundedString(value, 3);
		if (value == 0.0) {
			doubleString = "<span style='color:red;font-weight:bold;'>" + doubleString + "</span>";
		} else if (value == 1.0) {
			doubleString = "<span style='color:green;font-weight:bold;'>" + doubleString + "</span>";
		} else if (value < 0.5) {
			doubleString = "<span style='color:red;'>" + doubleString + "</span>";
		} else {
			doubleString = "<span style='color:green;'>" + doubleString + "</span>";
		}

		return doubleString;

	}

	public static ArrayList<QuerySolution> getAsSolutionArrayList(ResultSetRewindable resultSet) {
		ArrayList<QuerySolution> arrayList = new ArrayList<QuerySolution>();
		while (resultSet.hasNext()) {
			arrayList.add(resultSet.next());
		}
		return arrayList;
	}

	public static ArrayList<Binding> getAsBindingArrayList(ResultSetRewindable resultSet) {
		ArrayList<Binding> arrayList = new ArrayList<Binding>();
		while (resultSet.hasNext()) {
			arrayList.add(resultSet.nextBinding());
		}
		return arrayList;
	}

	public static boolean partialStringMatch(String haystack, ArrayList<String> needles) {
		boolean foundMatch = false;
		for (String needle : needles) {
			if (haystack.contains(needle)) {
				foundMatch = true;
				break;
			}
		}
		return foundMatch;
	}

	public static Model executeConstruct(String endpoint, Query query) throws RepositoryException,
			MalformedQueryException, QueryEvaluationException {
		QueryExecution queryExecution = QueryExecutionFactory.sparqlService(endpoint, query);
		return queryExecution.execConstruct();
	}
	
	public static Query addFromClause(Query query, String fromGraph) {
		Query queryWithFromClause = query.cloneQuery();
		queryWithFromClause.addGraphURI(fromGraph);
		return queryWithFromClause;
	}
}

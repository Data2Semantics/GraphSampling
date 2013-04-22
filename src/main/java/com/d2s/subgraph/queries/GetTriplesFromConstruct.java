package com.d2s.subgraph.queries;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.io.FileUtils;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import com.d2s.subgraph.eval.batch.ExperimentSetup;
import com.d2s.subgraph.eval.batch.SwdfExperimentSetup;
import com.d2s.subgraph.helpers.Helper;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class GetTriplesFromConstruct {
	private ExperimentSetup experimentSetup;
	private static String RESULTS_PATH = "constructResources";
	boolean validateMode = true;

	public GetTriplesFromConstruct(ExperimentSetup experimentSetup) {
		this.experimentSetup = experimentSetup;
		File resultsPath = new File(RESULTS_PATH);
		if (!resultsPath.exists()) {
			resultsPath.mkdir();
		}

	}

	public ArrayList<ArrayList<String>> resourcesPerQuery = new ArrayList<ArrayList<String>>();

	public void run() throws RepositoryException, MalformedQueryException, QueryEvaluationException, IOException {
		for (QueryWrapper query : experimentSetup.getQueries().getQueries()) {

			Query queryWithFromClause = Helper.addFromClause(query.getQuery(), experimentSetup.getGoldenStandardGraph());
			Query constructQuery = Helper.getAsConstructQuery(queryWithFromClause);
			// System.out.println(constructQuery.toString());
			Model model = Helper.executeConstruct(experimentSetup.getEndpoint(), constructQuery);
			File resultsFile = new File(RESULTS_PATH + "/" + experimentSetup.getGraphPrefix() + "q" + Integer.toString(query.getQueryId())
					+ ".csv");
			storeTriples(resultsFile, model);
		}
	}

	private void storeTriples(File file, Model model) throws IOException {
		// resultsWriter = new CSVWriter(new FileWriter(file), '\t');
		// store as hashmap, so we don't have double triples in our list
		HashMap<String, String> resources = new HashMap<String, String>();
		StmtIterator statements = model.listStatements();
		while (statements.hasNext()) {
			Statement statement = statements.next();
			Triple triple = statement.asTriple();
			String subject = processResource(triple.getSubject());
			String predicate = processResource(triple.getPredicate());
			String object = processResource(triple.getObject());

			resources.put(subject + predicate + object, subject + '\t' + predicate + '\t' + object);
		}
		FileUtils.writeLines(file, resources.values());
		// for (ArrayList<String> triple: resources.values()) {
		//
		// resultsWriter.writeNext(triple.toArray(new String[triple.size()]));
		// }
		// resultsWriter.close();
	}

	private String processResource(Node resource) throws IOException {
		String resourceAsString = "";
		if (resource.isURI()) {
			resourceAsString = "<" + resource.toString() + ">";
		} else if (resource.isLiteral()) {
			resourceAsString = resource.toString(false);
			resourceAsString = resourceAsString.replace("\"", "\\\"");
			resourceAsString = resourceAsString.replace("\n", "\\n");
			resourceAsString = "\"" + resourceAsString + "\"";
		} else {
			resourceAsString = resource.toString();
		}

		if (validateMode) {
			FileInputStream fstream = new FileInputStream(new File("df.nt"));
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			boolean valid = false;
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				if (strLine.contains(resourceAsString)) {
					valid = true;
					break;
				}
			}
			br.close();
			if (!valid) {
				System.out.println("kut. did not find extracted resource in original file: " + resourceAsString);
			}
		}

		return resourceAsString;
	}

	public static void main(String[] args) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException {
		// GetTriplesFromConstruct getTriples = new GetTriplesFromConstruct(new DbpExperimentSetup());
		GetTriplesFromConstruct getTriples = new GetTriplesFromConstruct(new SwdfExperimentSetup());
		getTriples.run();
	}
}

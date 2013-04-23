package com.d2s.subgraph.queries;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

import com.d2s.subgraph.eval.experiments.ExperimentSetup;
import com.d2s.subgraph.eval.experiments.SwdfExperimentSetup;
import com.d2s.subgraph.helpers.Helper;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.Model;

public class GetTriplesFromConstruct {
	private ExperimentSetup experimentSetup;
	private static String RESULTS_PATH = "constructResources";
	
	public GetTriplesFromConstruct(ExperimentSetup experimentSetup) {
		this.experimentSetup = experimentSetup;
		File resultsPath = new File(RESULTS_PATH);
		if (!resultsPath.exists()) {
			resultsPath.mkdir();
		}
	}


	public void run() throws RepositoryException, MalformedQueryException, QueryEvaluationException, IOException {
		experimentSetup.getQueries().saveCsvCopy(new File(RESULTS_PATH + "/queries.csv"));
		for (QueryWrapper query : experimentSetup.getQueries().getQueries()) {

			Query queryWithFromClause = Helper.addFromClause(query.getQuery(), experimentSetup.getGoldenStandardGraph());
			Query constructQuery = Helper.getAsConstructQuery(queryWithFromClause);
			// System.out.println(constructQuery.toString());
			Model model = Helper.executeConstruct(experimentSetup.getEndpoint(), constructQuery);
			
			File resultsFile = new File(RESULTS_PATH + "/" + experimentSetup.getGraphPrefix() + "q" + Integer.toString(query.getQueryId())
					+ ".nt");
			FileOutputStream fop = new FileOutputStream(resultsFile);
			model.write(fop, "N-TRIPLE");
		}
	}

	
	
	public static void main(String[] args) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException {
		// GetTriplesFromConstruct getTriples = new GetTriplesFromConstruct(new DbpExperimentSetup());
		GetTriplesFromConstruct getTriples = new GetTriplesFromConstruct(new SwdfExperimentSetup());
		getTriples.run();
	}
}

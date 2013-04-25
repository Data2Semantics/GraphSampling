package com.d2s.subgraph.queries;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

import com.d2s.subgraph.eval.experiments.ExperimentSetup;
import com.d2s.subgraph.eval.experiments.Sp2bExperimentSetup;
import com.d2s.subgraph.eval.experiments.SwdfExperimentSetup;
import com.d2s.subgraph.helpers.Helper;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.Model;

public class GetTriplesFromConstruct {
	private ExperimentSetup experimentSetup;
	private File resultsPath;
	public GetTriplesFromConstruct(ExperimentSetup experimentSetup) {
		this.experimentSetup = experimentSetup;
		resultsPath = new File(experimentSetup.getQueryResultsDir());
		if (!resultsPath.exists()) {
			resultsPath.mkdir();
		}
	}


	public void run() throws RepositoryException, MalformedQueryException, QueryEvaluationException, IOException {
		experimentSetup.getQueries().saveCsvCopy(new File(experimentSetup.getQueryResultsDir() + "/queries.csv"));
		for (QueryWrapper query : experimentSetup.getQueries().getQueries()) {

			Query queryWithFromClause = Helper.addFromClause(query.getQuery(), experimentSetup.getGoldenStandardGraph());
			Query constructQuery = Helper.getAsConstructQuery(queryWithFromClause);
			// System.out.println(constructQuery.toString());
			Model model = Helper.executeConstruct(experimentSetup.getEndpoint(), constructQuery);
			
			File resultsFile = new File(experimentSetup.getQueryResultsDir() + "/" + experimentSetup.getGraphPrefix() + "q" + Integer.toString(query.getQueryId())
					+ ".nt");
			FileOutputStream fop = new FileOutputStream(resultsFile);
			model.write(fop, "N-TRIPLE");
		}
		System.out.println("done. Now execute:");
		System.out.println("hadoop fs -put " + resultsPath.getAbsolutePath() + "/* " + experimentSetup.getGraphPrefix().substring(0, experimentSetup.getGraphPrefix().length()-1) + "/queryStatsInput");
	}


	
	
	public static void main(String[] args) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException {
		// GetTriplesFromConstruct getTriples = new GetTriplesFromConstruct(new DbpExperimentSetup());
//		GetTriplesFromConstruct getTriples = new GetTriplesFromConstruct(new SwdfExperimentSetup());
		GetTriplesFromConstruct getTriples = new GetTriplesFromConstruct(new Sp2bExperimentSetup());
		getTriples.run();
	}
}

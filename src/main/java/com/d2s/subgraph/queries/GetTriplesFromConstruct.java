package com.d2s.subgraph.queries;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

import com.d2s.subgraph.eval.Config;
import com.d2s.subgraph.eval.experiments.ExperimentSetup;
import com.d2s.subgraph.eval.experiments.Sp2bExperimentSetup;
import com.d2s.subgraph.helpers.Helper;
import com.hp.hpl.jena.rdf.model.Model;

public class GetTriplesFromConstruct {
	private ExperimentSetup experimentSetup;
	private File resultsPath;
	public GetTriplesFromConstruct(ExperimentSetup experimentSetup) {
		this.experimentSetup = experimentSetup;
		resultsPath = new File(experimentSetup.getQueryTriplesDir());
		if (!resultsPath.exists()) {
			resultsPath.mkdir();
		}
	}


	public void run() throws RepositoryException, MalformedQueryException, QueryEvaluationException, IOException {
		File allTriples = new File(experimentSetup.getQueryTriplesDir() + "/allQueries.nt");
		FileOutputStream allTripleOutputStream = new FileOutputStream(allTriples);
		for (Query query : experimentSetup.getQueries().getQueryCollection().getQueries()) {

			Query queryWithFromClause = Helper.addFromClauseToQuery(query, experimentSetup.getGoldenStandardGraph());
			Query constructQuery = Helper.getAsConstructQuery(queryWithFromClause);
			// System.out.println(constructQuery.toString());
			Model model = Helper.executeConstruct(Config.EXPERIMENT_ENDPOINT, constructQuery);
			
			File resultsFile = new File(experimentSetup.getQueryTriplesDir() + "/" + experimentSetup.getGraphPrefix() + "q" + Integer.toString(query.getQueryId())
					+ ".nt");
			FileOutputStream outputStream = new FileOutputStream(resultsFile);
			model.write(outputStream, "N-TRIPLE");
			model.write(allTripleOutputStream, "N-TRIPLE");
			
			outputStream.close();
		}
		allTripleOutputStream.close();
		System.out.println("done. Now execute:");
		System.out.println("hadoop fs -put " + resultsPath.getAbsolutePath() + "/* " + experimentSetup.getGraphPrefix().substring(0, experimentSetup.getGraphPrefix().length()-1) + "/queryStatsInput");
	}


	
	
	public static void main(String[] args) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException {
		// GetTriplesFromConstruct getTriples = new GetTriplesFromConstruct(new DbpExperimentSetup());
//		GetTriplesFromConstruct getTriples = new GetTriplesFromConstruct(new SwdfExperimentSetup());
		GetTriplesFromConstruct[] getTripleCollection = null;
		try {
			getTripleCollection = new GetTriplesFromConstruct[]{
//					new EvaluateGraphs(new DbpoExperimentSetup(DbpoExperimentSetup.QALD_REMOVE_OPTIONALS)), 
//					new GetTriplesFromConstruct(new DbpoExperimentSetup(DbpoExperimentSetup.QALD_KEEP_OPTIONALS)),
//					new EvaluateGraphs(new DbpoExperimentSetup(DbpoExperimentSetup.QUERY_LOGS)),
//					new GetTriplesFromConstruct(new SwdfExperimentSetup()),
					new GetTriplesFromConstruct(new Sp2bExperimentSetup()),
//					new GetTriplesFromConstruct(new LmdbExperimentSetup()),
			};
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (GetTriplesFromConstruct getTriples: getTripleCollection) {
			getTriples.run();
		}
	}
}

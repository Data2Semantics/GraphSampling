package com.d2s.subgraph.queries.eval;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

import com.d2s.subgraph.eval.Config;
import com.d2s.subgraph.eval.experiments.Bio2RdfExperimentSetup;
import com.d2s.subgraph.eval.experiments.DbpediaExperimentSetup;
import com.d2s.subgraph.eval.experiments.ExperimentSetup;
import com.d2s.subgraph.eval.experiments.LgdExperimentSetup;
import com.d2s.subgraph.eval.experiments.ObmExperimentSetup;
import com.d2s.subgraph.eval.experiments.SwdfExperimentSetup;
import com.d2s.subgraph.queries.Query;
import com.d2s.subgraph.util.QueryUtils;
import com.hp.hpl.jena.rdf.model.Model;

public class GetTriplesFromConstruct {
	private ExperimentSetup experimentSetup;
	private File resultsPath;
	public GetTriplesFromConstruct(ExperimentSetup experimentSetup) throws IOException {
		this.experimentSetup = experimentSetup;
		resultsPath = new File(Config.PATH_QUERY_CONSTRUCT_TRIPLES);
		if (!resultsPath.exists()) {
			resultsPath.mkdir();
		}
		resultsPath = new File(resultsPath.getPath() + "/" + experimentSetup.getId() + "/");
		if (resultsPath.exists()) {
			FileUtils.deleteDirectory(resultsPath);
		}
		resultsPath.mkdir();
	}


	public void run() throws RepositoryException, MalformedQueryException, QueryEvaluationException, IOException {
		File allTriples = new File(resultsPath.getPath() + "/allQueries.nt");
		FileOutputStream allTripleOutputStream = new FileOutputStream(allTriples);
		
		int totalCount = experimentSetup.getQueryCollection().getDistinctQueryCount();
		int itCount = 1;
		for (Query query : experimentSetup.getQueryCollection().getQueries()) {
			System.out.println(itCount + "/" + totalCount);
			itCount++;
			Query queryWithFromClause = query.getQueryWithFromClause(experimentSetup.getGoldenStandardGraph());
			Query constructQuery = QueryUtils.getAsConstructQuery(queryWithFromClause);
			// System.out.println(constructQuery.toString());
			Model model = QueryUtils.executeConstruct(Config.EXPERIMENT_ENDPOINT, constructQuery);
			
			File resultsFile = new File(resultsPath.getPath() + "/" + experimentSetup.getGraphPrefix() + "q" + itCount
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
//					new GetTriplesFromConstruct(new DbpoExperimentSetup(DbpoExperimentSetup.QALD_KEEP_OPTIONALS)),
//					new GetTriplesFromConstruct(new SwdfExperimentSetup(true)),
//					new GetTriplesFromConstruct(new Sp2bExperimentSetup(true)),
//					new GetTriplesFromConstruct(new ObmExperimentSetup(true)),
					new GetTriplesFromConstruct(new LgdExperimentSetup(true)),
//					new GetTriplesFromConstruct(new Bio2RdfExperimentSetup(true)),
			};
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (GetTriplesFromConstruct getTriples: getTripleCollection) {
//			getTriples.run();
		}
	}
}

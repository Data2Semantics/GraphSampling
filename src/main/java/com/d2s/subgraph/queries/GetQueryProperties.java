package com.d2s.subgraph.queries;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.data2semantics.query.QueryCollection;

import au.com.bytecode.opencsv.CSVWriter;

import com.d2s.subgraph.eval.experiments.DbpoExperimentSetup;
import com.d2s.subgraph.eval.experiments.ExperimentSetup;
import com.d2s.subgraph.eval.experiments.LmdbExperimentSetup;
import com.d2s.subgraph.eval.experiments.Sp2bExperimentSetup;
import com.d2s.subgraph.eval.experiments.SwdfExperimentSetup;


public class GetQueryProperties {
	private ExperimentSetup[] experimentSetups;
	private String QUERY_PROPERTIES = "queryProperties.csv";
	public GetQueryProperties(ExperimentSetup[] experimentSetups) throws IOException {
		this.experimentSetups = experimentSetups;
		writeCsv();
	}
	private void writeCsv() throws IOException {
		File csvFile = new File(QUERY_PROPERTIES);
		CSVWriter writer = new CSVWriter(new FileWriter(csvFile), ';');
		
		writer.writeNext(new String[]{"graph", "numJoins", "numNonOptTriplePatterns", "numCcv", "numCvv", "numVcc"});
		for (ExperimentSetup experimentSetup: experimentSetups) {
			QueryCollection<Query> queries = experimentSetup.getQueryCollection();
			int numJoins = 0;
			int numLeftJoins = 0;
			int numNonOptTriplePatterns = 0;
			int patternCountCcv = 0;
			int patternCountCvv = 0;
			int patternCountVcc = 0;
			for (Query query: queries.getQueries()) {
				numJoins += query.joinCount.getTotalJoins();
				numNonOptTriplePatterns += query.getNumberOfNonOptionalTriplePatterns();
				patternCountCcv += query.triplePatternCountCcv;
				patternCountCvv += query.triplePatternCountCvv;
				patternCountVcc += query.triplePatternCountVcc;
			}
			writer.writeNext(new String[]{
					experimentSetup.getGraphPrefix(), 
					Integer.toString(numJoins), 
					Integer.toString(numLeftJoins), 
					Integer.toString(numNonOptTriplePatterns),
					Integer.toString(patternCountCcv),
					Integer.toString(patternCountCvv),
					Integer.toString(patternCountVcc)});
		}
		writer.close();
	}
	
	
	

	public static void main(String[] args) throws IOException, InterruptedException  {
		ExperimentSetup[] experimentSetups = null;
		try {
			experimentSetups = new ExperimentSetup[]{
					new DbpoExperimentSetup(DbpoExperimentSetup.QALD_KEEP_OPTIONALS, true),
					new SwdfExperimentSetup(true),
					new Sp2bExperimentSetup(true),
					new LmdbExperimentSetup(true),
//					new LgdExperimentSetup(),
//					new DbpExperimentSetup(),
			};
		} catch (Exception e) {
			e.printStackTrace();
		}
		new GetQueryProperties(experimentSetups);
	}

}

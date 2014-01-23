package com.d2s.subgraph.eval.analysis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.data2semantics.query.QueryCollection;
import org.xml.sax.SAXException;

import au.com.bytecode.opencsv.CSVWriter;

import com.d2s.subgraph.eval.Config;
import com.d2s.subgraph.eval.experiments.ExperimentSetup;
import com.d2s.subgraph.eval.results.SampleResults;
import com.d2s.subgraph.queries.Query;

public class OutputWekaCsv extends OutputWrapper {
	public OutputWekaCsv(ExperimentSetup experimentSetup, ArrayList<SampleResults> allGraphResults, QueryCollection<Query> queryCollection, File resultsDir) {
		super(experimentSetup, allGraphResults, queryCollection, resultsDir);
	}
	
	public void store() throws IOException {
		storeMappingFile();
		for (SampleResults results: allGraphResults) {
			storeFeaturesFile(results);
		}
	}


	private void storeMappingFile() throws IOException {
		File mappingFile = new File(resultsDir, Config.FILE_QUERY_MAPPING);

		Writer writer = new FileWriter(mappingFile);
		int count = 0;
		for (Query query: getQueryCollection().getQueries()) {
			writer.write("####" + count + "\n");
			writer.write(query.toString() + "\n\n");
			count++;
		}
		writer.close();
	}

	private void storeFeaturesFile(SampleResults results) throws IOException {
		CSVWriter writer = new CSVWriter(new FileWriter(new File(resultsDir, Config.FILE_CSV_QUERY_FEATURES)), ',');
		// write header
		writer.writeNext(new String[] { "queryId", "recall", "triplePatCount", "triplePatCountCcv", "triplePatCountCvv", "triplePatCountVcc", "hasLimit", "joinCount",
				"joinCountOo", "joinCountPo", "joinCountPp", "joinCountSo", "joinCountSp", "joinCountSs", "unions", "optionalBlocks", "optionalTriples", });
		int count = 0;
		for (Query query: getQueryCollection().getQueries()) {
//			Query query = queries.get(i);

			ArrayList<String> row = new ArrayList<String>();
			row.add(Integer.toString(count));
			row.add(Double.toString(results.getQueryCollection().getQuery(query.toString()).getResults().getRecall()));
			row.add(Integer.toString(query.triplePatternCount));
			row.add(Integer.toString(query.triplePatternCountCcv));
			row.add(Integer.toString(query.triplePatternCountCvv));
			row.add(Integer.toString(query.triplePatternCountVcc));
			row.add((query.hasLimit() ? "1" : "0"));
			row.add(Integer.toString(query.joinCount.getTotalJoins()));
			row.add(Integer.toString(query.joinCount.getOojoin()));
			row.add(Integer.toString(query.joinCount.getPojoin()));
			row.add(Integer.toString(query.joinCount.getPpjoin()));
			row.add(Integer.toString(query.joinCount.getSojoin()));
			row.add(Integer.toString(query.joinCount.getSpjoin()));
			row.add(Integer.toString(query.joinCount.getSsjoin()));
			row.add(Integer.toString(query.numUnions.getVal()));
			row.add(Integer.toString(query.numOptionalBlocks.getVal()));
			row.add(Integer.toString(query.numOptionalTriples.getVal()));
			writer.writeNext(row.toArray(new String[row.size()]));
			count++;
		}

		writer.close();
	}

	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
//		store(new SwdfExperimentSetup(true));
	}
}

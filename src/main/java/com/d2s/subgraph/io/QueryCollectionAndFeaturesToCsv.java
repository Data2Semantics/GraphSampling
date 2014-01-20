package com.d2s.subgraph.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

import au.com.bytecode.opencsv.CSVWriter;

import com.d2s.subgraph.eval.Config;
import com.d2s.subgraph.eval.experiments.ExperimentSetup;
import com.d2s.subgraph.eval.experiments.SwdfExperimentSetup;
import com.d2s.subgraph.queries.Query;
import com.d2s.subgraph.util.Utils;

public class QueryCollectionAndFeaturesToCsv {

	private ExperimentSetup experimentSetup;
	File outputDir;
	ArrayList<Query> queries = new ArrayList<Query>();

	public QueryCollectionAndFeaturesToCsv(ExperimentSetup experimentSetup) throws IOException {
		this.experimentSetup = experimentSetup;
		initializeDirs();
		queries.addAll(experimentSetup.getQueryCollection().getQueries());
	}

	private void initializeDirs() throws IOException {
		Utils.mkdir(Config.PATH_QUERY_FEATURES);
		outputDir = new File(Config.PATH_QUERY_FEATURES + experimentSetup.getId());
		if (outputDir.exists()) FileUtils.deleteDirectory(outputDir);
		outputDir.mkdir();
	}

	private void storeMappingFile() throws IOException {
		File mappingFile = new File(outputDir.getPath() + "/" + Config.FILE_QUERY_MAPPING);

		Writer writer = new FileWriter(mappingFile);

		for (int i = 0; i < queries.size(); i++) {
			Query query = queries.get(i);
			writer.write("####" + i + "\n");
			writer.write(query.toString() + "\n\n");
		}
		writer.close();
	}

	private void storeFeaturesFile() throws IOException {
		CSVWriter writer = new CSVWriter(new FileWriter(new File(outputDir.getPath() + "/" + Config.FILE_CSV_QUERY_FEATURES)), ',');
		//write header
		writer.writeNext(new String[]{
			"queryId",
			"triplePatCount",
			"triplePatCountCcv",
			"triplePatCountCvv",
			"triplePatCountVcc",
			"hasLimit",
			"joinCount",
			"joinCountOo",
			"joinCountPo",
			"joinCountPp",
			"joinCountSo",
			"joinCountSp",
			"joinCountSs",
			"unions",
			"optionalBlocks",
			"optionalTriples",
		});
		for (int i = 0; i < queries.size(); i++) {
			Query query = queries.get(i);
			
			ArrayList<String> row = new ArrayList<String>();
			row.add(Integer.toString(i));
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
		}

		writer.close();
	}

	public static void store(ExperimentSetup experimentSetup) throws IOException {
		QueryCollectionAndFeaturesToCsv writer = new QueryCollectionAndFeaturesToCsv(experimentSetup);
		writer.storeMappingFile();
		writer.storeFeaturesFile();
	}

	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
		store(new SwdfExperimentSetup(true));
	}
}

package com.d2s.subgraph.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import au.com.bytecode.opencsv.CSVWriter;

import com.d2s.subgraph.eval.Config;
import com.d2s.subgraph.eval.generation.FetchGraphResults;
import com.d2s.subgraph.eval.results.QueryResults;
import com.d2s.subgraph.queries.Query;
import com.d2s.subgraph.util.StringUtils;
import com.d2s.subgraph.util.Utils;

public class QResultsSaver {
	private FetchGraphResults fetcher;
	private CSVWriter writer;

	public QResultsSaver(FetchGraphResults fetcher) {
		this.fetcher = fetcher;
	}



	public void doWrite() throws IOException {
		String csvFileString = Config.PATH_QUERY_RESULTS + "/" + StringUtils.getFileSystemGraphName(fetcher.getResults().getGraphName()) + ".csv";
		File csvFile = new File(csvFileString);
		if (csvFile.exists()) {
			System.out.println("query results file already exists. deleting/overwriting");
			csvFile.delete();
		}
		writer = new CSVWriter(new FileWriter(csvFile), ';');

		writeMetaRow();
		writeHeaderRow();
		writeContent();
		
		writer.close();
		
	}
	
	private void writeContent() {
		for (Query query: fetcher.getResults().getQueryCollection().getQueries()) {
			QueryResults result = query.getResults();
			ArrayList<String> row = new ArrayList<String>();
			row = addColToRow(row, QResultCols.QUERY_STRING, query.toString());
			row = addColToRow(row, QResultCols.QUERY_COUNT, query.getCount());
			row = addColToRow(row, QResultCols.RECALL, result.getRecall());
			row = addColToRow(row, QResultCols.PRECISION, result.getPrecision());
			row = addColToRow(row, QResultCols.GOLDEN_STANDARD_SIZE, result.getGoldenStandardSize());
			row = addColToRow(row, QResultCols.QUERY_DURATION, Long.toString(result.getSubgraphDuration().getTime()));
			Utils.writeRow(row, writer);
		}
	}
	
	private void writeMetaRow() {
		ArrayList<String> row = new ArrayList<String>();
		row = addColToRow(row, QResultCols.META_GRAPH_NAME, fetcher.getResults().getGraphName());
		row = addColToRow(row, QResultCols.META_EXPERIMENT_SETUP_ID, fetcher.getExperimentSetup().getId());
		row = addColToRow(row, QResultCols.META_EXPERIMENT_SETUP_ID, fetcher.getExperimentSetup().getId());
		Utils.writeRow(row, writer);
	}
	
	private void writeHeaderRow() {
		ArrayList<String> row = new ArrayList<String>();
		row = addColToRow(row, QResultCols.QUERY_STRING, "query");
		row = addColToRow(row, QResultCols.QUERY_COUNT, "query count");
		row = addColToRow(row, QResultCols.RECALL, "recall");
		row = addColToRow(row, QResultCols.PRECISION, "precision");
		row = addColToRow(row, QResultCols.GOLDEN_STANDARD_SIZE, "golden standard size");
		row = addColToRow(row, QResultCols.QUERY_DURATION, "query duration");
		Utils.writeRow(row, writer);
	}
	
	private ArrayList<String> addColToRow(ArrayList<String> row, int colIndex, String value) {
		while (row.size() <= colIndex) {
			row.add(null);
		}
		row.set(colIndex, value);
		return row;
	}
	private ArrayList<String> addColToRow(ArrayList<String> row, int colIndex, int value) {
		return addColToRow(row, colIndex, Integer.toString(value));
	}
	private ArrayList<String> addColToRow(ArrayList<String> row, int colIndex, double value) {
		return addColToRow(row, colIndex, Double.toString(value));
	}
	
	
	public static void write(FetchGraphResults fetcher) throws IOException {
		QResultsSaver fileHandler = new QResultsSaver(fetcher);
		fileHandler.doWrite();
	}
}

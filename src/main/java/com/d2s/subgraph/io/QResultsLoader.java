package com.d2s.subgraph.io;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.data2semantics.query.QueryCollection;

import au.com.bytecode.opencsv.CSVReader;

import com.d2s.subgraph.eval.Config;
import com.d2s.subgraph.eval.analysis.WriteAnalysis;
import com.d2s.subgraph.eval.experiments.ExperimentSetup;
import com.d2s.subgraph.eval.results.GraphResults;
import com.d2s.subgraph.eval.results.GraphResultsRegular;
import com.d2s.subgraph.eval.results.GraphResultsSample;
import com.d2s.subgraph.eval.results.QueryResultsRegular;
import com.d2s.subgraph.queries.Query;

public class QResultsLoader {
	private ExperimentSetup experimentSetup;
	private WriteAnalysis analyseResults;
	ArrayList<GraphResultsRegular> allGraphResults;
	public QResultsLoader(ExperimentSetup experimentSetup) {
		this.experimentSetup = experimentSetup;

	}
	
	public WriteAnalysis doRead() throws IOException {
		readFiles();
		addToBatchResults();
		return analyseResults;
	}



	private void readFiles() throws IOException {
		for (File file: getFiles()) {
			CSVReader reader = new CSVReader(new FileReader(file));
			String[] nextLine = reader.readNext();//meta row
			nextLine = reader.readNext();//header row
			
			if (nextLine == null) {
				reader.close();
				throw new IOException("unable to get first row from query results csv file");
			}
			QueryCollection<Query> queryCollection = new QueryCollection<Query>();
			while ((nextLine = reader.readNext()) != null) {
				Query query = Query.create(nextLine[QResultCols.QUERY_STRING], queryCollection);
				query.setCount(Integer.parseInt(nextLine[QResultCols.QUERY_COUNT]));
				QueryResultsRegular results = new QueryResultsRegular();
				results.setRecall(Double.parseDouble(nextLine[QResultCols.RECALL]));
				results.setPrecision(Double.parseDouble(nextLine[QResultCols.PRECISION]));
				results.setGoldenStandardSize(Integer.parseInt(nextLine[QResultCols.GOLDEN_STANDARD_SIZE]));
				
				
				query.setResults(results);
				queryCollection.addQuery(query);
				
		    }
			GraphResultsRegular regularGraphResults = new GraphResultsRegular();
			regularGraphResults.setQueryCollection(queryCollection);
			
			allGraphResults.add(regularGraphResults);
			
			reader.close();
			if (queryCollection.getTotalQueryCount() == 0) {
				throw new IOException("queries retrieved from CSV is 0!!: " + file.getName());
			}
		}
	}
	

	private ArrayList<File> getFiles() throws IOException {
		ArrayList<File> relevantFiles = new ArrayList<File>();
		for (File file : FileUtils.listFiles(new File(Config.PATH_QUERY_RESULTS), new String[] { "csv" }, false)) {
			if (isRelevantFile(file)) relevantFiles.add(file);
		}
		if (relevantFiles.size() == 0) {
			throw new IOException("unable to find any query results file for experiment " + experimentSetup.getId() + ". Looked in path " + Config.PATH_QUERY_RESULTS);
		}
		return relevantFiles;

	}

	private boolean isRelevantFile(File file) throws IOException {
		boolean isRelevantFile = false;
		CSVReader reader = new CSVReader(new FileReader(file));
		String[] firstLine = reader.readNext();
		if (firstLine == null) {
			reader.close();
			throw new IOException("unable to get first row from query results csv file");
		}
		int experimentIdCol = QResultCols.META_EXPERIMENT_SETUP_ID;
		if (experimentIdCol < firstLine.length && firstLine[experimentIdCol].equals(experimentSetup.getId())) {
			isRelevantFile = true;
		}
		reader.close();
		return isRelevantFile;
	}


	private void addToBatchResults() throws IOException {
		HashMap<String, ArrayList<GraphResults>> groupedRandomSampleGraphs = groupRandomSampleGraphs();
		
		//the method called above already added all regular graph to batch results.
		//now just add our random sample results
		for (Entry<String, ArrayList<GraphResults>> entry: groupedRandomSampleGraphs.entrySet()) {
			GraphResultsSample sampleGraphResultsCombined = new GraphResultsSample();
			sampleGraphResultsCombined.add(entry.getValue());
			analyseResults.add(sampleGraphResultsCombined);
		}
	}
	
	/**
	 * loop through all graphs. if it is a regular graph, add to batch result.
	 * if it is a random sample graph, group them (by size), so we can combine them afterwards
	 */
	private HashMap<String, ArrayList<GraphResults>> groupRandomSampleGraphs() {
		HashMap<String, ArrayList<GraphResults>> groupedSampleGraphs = new HashMap<String, ArrayList<GraphResults>>();
		

		for(GraphResultsRegular graphResult: allGraphResults) {
			String randomSampleType = getRandomSampleType(graphResult.getGraphName());
			if (randomSampleType != null) {
				
				if (!groupedSampleGraphs.containsKey(randomSampleType)) {
					groupedSampleGraphs.put(randomSampleType, new ArrayList<GraphResults>());
				}
				groupedSampleGraphs.get(randomSampleType).add(graphResult);
			} else {
				//it is a regular sampled graph 
				//(not a random sample where we need to combine multiple sampled graphs to one result set)
				analyseResults.add(graphResult);
			}
		}
		
		return groupedSampleGraphs;
		
	}
	
	
	private String getRandomSampleType(String graph) {
		String sampleType = null; 
		if (graph.toLowerCase().contains("sample")) {
			for (String sampleSize: Config.SAMPLE_SIZES) {
				if (graph.contains(sampleSize)) {
					sampleType = sampleSize;
					break;
				}
			}
		}
		return sampleType;
	}
	
	public static WriteAnalysis read(ExperimentSetup experimentSetup) throws IOException {
		QResultsLoader loader = new QResultsLoader(experimentSetup);
		return loader.doRead();
	}
}

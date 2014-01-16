package com.d2s.subgraph.queries.qtriples;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.data2semantics.query.QueryCollection;

import com.d2s.subgraph.eval.Config;
import com.d2s.subgraph.eval.experiments.ExperimentSetup;
import com.d2s.subgraph.eval.experiments.SwdfExperimentSetup;
import com.d2s.subgraph.queries.Query;


public class FetchTriplesFromQueries {
	private int maxQueries = Integer.MAX_VALUE;
	private ExperimentSetup experimentSetup;
	private File experimentDir;
	public FetchTriplesFromQueries(ExperimentSetup experimentSetup) throws IOException {
		this.experimentSetup = experimentSetup;
		setupDirStructure();
	}
	
	private void setupDirStructure() throws IOException {
		File outputDir = new File(Config.PATH_QUERY_TRIPLES);
		if (!outputDir.exists()) outputDir.mkdir();
		experimentDir = new File(outputDir.getPath() + "/" + experimentSetup.getId());
		
	}
	
	private void resetExperimentDir() throws IOException {
		if (experimentDir.exists()) {
			System.out.println("removed previous qtriples results");
			FileUtils.deleteDirectory(experimentDir);
		}
		experimentDir.mkdir();
	}
	
//	public void createUniqueTripleFile() throws IOException {
////		System.out.println("creating unique triple file");
//		//We've fetch everything. Now make a list of all unique triples, so we can fetch their weight in our weighted triple set
//		Collection<File> tripleFiles = FileUtils.listFilesAndDirs(experimentDir, new IOFileFilter(){
//			public boolean myAccept(String filename) {
//				return (!filename.contains(".") && !filename.contains("-"));
//			}
//			public boolean accept(File filename) {
//				return myAccept(filename.getName());
//			}
//			public boolean accept(File dir, String filename) {
//				return myAccept(filename);
//			}}, TrueFileFilter.INSTANCE);
//		Set<String> triples = new HashSet<String>();
//		for (File file: tripleFiles) {
//			if (!file.isDirectory()) triples.addAll(FileUtils.readLines(file));
//		}
//		FileUtils.writeLines(new File(experimentDir.getPath() + "/allTriples.txt"), triples);
//	}
//	
	private void processQueries() throws IOException {
		QueryCollection<Query> queries = experimentSetup.getQueryCollection();
		int count = 1;
		for (Query query: queries.getQueries()) {
			System.out.println(count + "/" + queries.getDistinctQueryCount());
			count++;
			FetchTriplesFromQuery.fetch(experimentSetup, query, experimentDir);
			if (count > maxQueries) break;
		}
	}
	
	public static void fetch(ExperimentSetup experimentSetup) throws IOException {
		FetchTriplesFromQueries fetch = new FetchTriplesFromQueries(experimentSetup);
		fetch.resetExperimentDir();
		fetch.processQueries();
//		fetch.createUniqueTripleFile();
	}
	
	
	public static void main(String[] args) throws Exception {
		boolean useCachedQueries = true;
		
		FetchTriplesFromQueries fetch = new FetchTriplesFromQueries(new SwdfExperimentSetup(useCachedQueries));
		fetch.maxQueries = 20;
		fetch.resetExperimentDir();
		fetch.processQueries();
//		fetch.createUniqueTripleFile();
		// new EvaluateGraphs(new
		// DbpoExperimentSetup(DbpoExperimentSetup.QALD_REMOVE_OPTIONALS)),
		// new EvaluateGraphs(new
		// DbpoExperimentSetup(DbpoExperimentSetup.QALD_KEEP_OPTIONALS)),
		// new EvaluateGraphs(new
		// DbpoExperimentSetup(DbpoExperimentSetup.QUERY_LOGS)),
		// new EvaluateGraphs(new Sp2bExperimentSetup()),
		// new EvaluateGraphs(new LmdbExperimentSetup()),
		// new EvaluateGraphs(new LgdExperimentSetup()),
		// new EvaluateGraphs(new DbpExperimentSetup()),
	}



}

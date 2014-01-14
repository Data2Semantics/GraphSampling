package com.d2s.subgraph.queries.querytriples;

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
	
	public static File setupDirStructure(ExperimentSetup experimentSetup) {
		File outputDir = new File(Config.PATH_QUERY_TRIPLES);
		if (!outputDir.exists()) outputDir.mkdir();
		File experimentSetupDir = new File(outputDir.getPath() + "/" + experimentSetup.getId());
		if (!experimentSetupDir.exists()) experimentSetupDir.mkdir();
		return experimentSetupDir;
	}
	
	public static void fetch(ExperimentSetup experimentSetup) throws IOException {
		File experimentDir = setupDirStructure(experimentSetup);
		QueryCollection<Query> queries = experimentSetup.getQueryCollection();
		for (Query query: queries.getQueries()) {
			FetchTriplesFromQuery.fetch(experimentSetup, query, experimentDir);
		}
		createUniqueFile(experimentDir);
	}
	
	public static void createUniqueFile(final File experimentDir) throws IOException {
		//We've fetch everything. Now make a list of all unique triples, so we can fetch their weight in our weighted triple set
		Collection<File> tripleFiles = FileUtils.listFilesAndDirs(experimentDir, new IOFileFilter(){
			public boolean myAccept(String filename) {
				return (!filename.contains(".") && !filename.contains("-"));
			}
			public boolean accept(File filename) {
				return myAccept(filename.getName());
			}
			public boolean accept(File dir, String filename) {
				return myAccept(filename);
			}}, TrueFileFilter.INSTANCE);
		Set<String> triples = new HashSet<String>();
		for (File file: tripleFiles) {
			if (!file.isDirectory()) triples.addAll(FileUtils.readLines(file));
		}
		FileUtils.writeLines(new File(experimentDir.getPath() + "/allTriples.txt"), triples);
	}

	public static void main(String[] args) throws Exception {
		
		
		boolean useCachedQueries = true;
		
		FetchTriplesFromQueries.fetch(new SwdfExperimentSetup(useCachedQueries));
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

package com.d2s.subgraph.queries;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.data2semantics.query.filters.QueryFilter;

import com.d2s.subgraph.eval.experiments.ExperimentSetup;

public class Sp2bQueries extends QueriesFetcher {
	private static String QUERY_DIR = "src/main/resources/sp2bQueries";
	private static String QUERY_FILE_EXTENSION = "sparql";

	public Sp2bQueries(ExperimentSetup experimentSetup, QueryFilter... filters) throws IOException {
		super(experimentSetup, false);
		System.out.println("parsing sp2b query logs");
		this.filters = new ArrayList<QueryFilter>(Arrays.asList(filters));
		parseQueryDir();
		saveQueriesToCsv();
		saveQueriesToCacheFile();
	}
	
	private void parseQueryDir() throws IOException {
		File queryDir = new File(QUERY_DIR);
		if (!queryDir.exists()) {
			throw new IOException("Query dir " + QUERY_DIR + " does not exist");
		}
		
		
		File [] queryFiles = queryDir.listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.endsWith("." + QUERY_FILE_EXTENSION);
		    }
		});
		
		for (File queryFile: queryFiles) {
			parseCustomLogFile(queryFile);
			if (queryCollection.getDistinctQueryCount() > experimentSetup.getMaxNumQueries()) {
				break;
			}
		}
	}
	
	@Override
	protected void parseCustomLogFile(File queryFile) throws IOException {
		addQueryFileToList(queryFile);
		
		
	}
	
	private void addQueryFileToList(File queryFile) throws IOException {
		
		String queryString = FileUtils.readFileToString(queryFile);
		addQueryToList(queryString);
	}




	public static void main(String[] args) {

		try {
//			Sp2bQueries swdfQueries = new Sp2bQueries();
////			Sp2bQueries swdfQueries = new Sp2bQueries(new DescribeFilter(), new SimpleBgpFilter(), new GraphClauseFilter());
//			System.out.println(swdfQueries.toString());


		} catch (Exception e) {
			e.printStackTrace();
		}
	}




}

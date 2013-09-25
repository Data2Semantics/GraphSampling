package com.d2s.subgraph.queries;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.data2semantics.query.QueryCollection;
import org.data2semantics.query.filters.QueryFilter;

import com.d2s.subgraph.eval.experiments.Sp2bExperimentSetup;
import com.d2s.subgraph.helpers.Helper;
import com.hp.hpl.jena.query.QueryParseException;

public class Sp2bQueries extends GetQueries {
	private static String QUERY_DIR = "src/main/resources/sp2bQueries";
	private static String QUERY_FILE_EXTENSION = "sparql";
	private static boolean ONLY_UNIQUE = true;

	public Sp2bQueries(QueryFilter... filters) throws IOException {
		System.out.println("parsing sp2b query logs");
		this.filters = new ArrayList<QueryFilter>(Arrays.asList(filters));
		parseQueryDir();
		if (ONLY_UNIQUE) {
			// we have stored stuff in hashmap to keep queries unique. now get them as regular queries
			queries = new ArrayList<Query>(queriesHm.values());
			queriesHm.clear();
		}
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
			addQueryFileToList(queryFile);
			if (queries.size() > maxNumQueries) {
				break;
			}
			
		}
	}

	private void addQueryFileToList(File queryFile) throws IOException {
		
		String queryString = FileUtils.readFileToString(queryFile);
		String basename = FilenameUtils.removeExtension(queryFile.getName());
		int queryId = Integer.parseInt(basename.substring(1));
		try {
			Query query = Query.create(queryString, new QueryCollection());
			query.setQueryId(queryId);
			if (checkFilters(query)) {
				if (ONLY_UNIQUE) {
					if (queriesHm.containsKey(query)) {
						duplicateQueries++;
					} else {
						if (hasResults(Helper.addFromClauseToQuery(query, Sp2bExperimentSetup.GOLDEN_STANDARD_GRAPH))) {
							query.setQueryId(validQueries);
							queriesHm.put(query, query);
							validQueries++;
						} else {
							noResultsQueries++;
						}
					}
				} else {
					queries.add(query);
					validQueries++;
				}
				query.generateQueryStats();
			} else {
				filteredQueries++;
			}
		} catch (QueryParseException e) {
			// could not parse query, probably a faulty one. ignore!
			invalidQueries++;
		}
	}




	public static void main(String[] args) {

		try {
			Sp2bQueries swdfQueries = new Sp2bQueries();
//			Sp2bQueries swdfQueries = new Sp2bQueries(new DescribeFilter(), new SimpleBgpFilter(), new GraphClauseFilter());
			System.out.println(swdfQueries.toString());


		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

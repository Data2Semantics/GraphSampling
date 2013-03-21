package com.d2s.subgraph.query;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;

import au.com.bytecode.opencsv.CSVWriter;

import com.d2s.subgraph.eval.EvalQuery;
import com.d2s.subgraph.eval.dbpedia.QaldDbpQueries;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementVisitorBase;
import com.hp.hpl.jena.sparql.syntax.ElementWalker;
import com.hp.hpl.jena.sparql.syntax.Template;

public class RewriteToConstruct {
	
	private Query origQuery;
	private Query constructQuery;
	public RewriteToConstruct(String query) {
		this.origQuery = QueryFactory.create(query);
		
		createConstruct();
	}
	
	private void createConstruct() {
		constructQuery = origQuery.cloneQuery();
		constructQuery.setQueryConstructType();
		
		final BasicPattern constructBp = new BasicPattern();
		
		Element queryPattern = origQuery.getQueryPattern();
		ElementWalker.walk(queryPattern, new ElementVisitorBase() {
	        public void visit(ElementPathBlock el) {
	            Iterator<TriplePath> triples = el.patternElts();
	            while (triples.hasNext()) {
	                constructBp.add(triples.next().asTriple());
	            }
	        }
	    });
		constructQuery.setConstructTemplate(new Template(constructBp));
		
	}
	
	public String getConstructQuery() {
		return constructQuery.toString();
	}
	
	
	
	
	public static void main(String[] args)  {
		
		try {
			QaldDbpQueries getQueries = new QaldDbpQueries(QaldDbpQueries.QALD_1_QUERIES);
			ArrayList<EvalQuery> queries = getQueries.getQueries();
			
			File csvFile = new File("queries.csv");
			CSVWriter writer = new CSVWriter(new FileWriter(csvFile), ',');
			writer.writeNext(new String[]{"origQuery", "constructQuery"});
			if (queries.size() == 0) {
				System.out.println("no queries retrieved");
				System.exit(1);
			}
			ArrayList<EvalQuery> failedQueries = new ArrayList<EvalQuery>();
			for (EvalQuery query: queries) {
				try {
//					System.out.println(query.getQuery());
					RewriteToConstruct rewrite = new RewriteToConstruct(query.getQuery());
					
//					System.out.println(rewrite.getConstructQuery());
					writer.writeNext(new String[]{query.getQuery(), rewrite.getConstructQuery()});
				} catch (Exception e) {
					System.out.println(query.getQuery());
					e.printStackTrace();
					failedQueries.add(query);
				}
				
				
			}
			writer.close();
			System.out.println("Failed to parse queries as construct for " + failedQueries.size() + " queries");
//			if (failedQueries.size() > 0 ) {
//				File failedQueriesCsv = new File("failedQueries.csv");
//				CSVWriter failedQueriesWriter = new CSVWriter(new FileWriter(failedQueriesCsv), ',');
//				
//				
//				for (EvalQuery query: failedQueries) {
//					System.out.println(query.getQuery());
//					failedQueriesWriter.writeNext(new String[]{query.getQuery()});
//				}
//				failedQueriesWriter.close();
//				
//				
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

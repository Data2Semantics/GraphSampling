package com.d2s.subgraph.queries.filters;

import java.util.Iterator;

import com.d2s.subgraph.eval.QueryWrapper;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementVisitorBase;
import com.hp.hpl.jena.sparql.syntax.ElementWalker;

public class SimpleBgpFilter implements QueryFilter {
	boolean isSimpleBgp = true;
	public boolean filter(final QueryWrapper query) {
		isSimpleBgp = true;
		Element qPattern = query.getQuery().getQueryPattern();

		// This will walk through all parts of the query
		ElementWalker.walk(qPattern,
		    // For each element...
		    new ElementVisitorBase() {
		        // ...when it's a block of triples...
		        public void visit(ElementPathBlock el) {
		            // ...go through all the triples...
		            Iterator<TriplePath> triples = el.patternElts();
		            int index = 0;
		            while (triples.hasNext()) {
		            	index++;
		            	if (index > 2) {
//		            		System.out.println("> 2");
		            		isSimpleBgp = false;
		            		break;
		            	}
		            	TriplePath triplePath = triples.next();
		            	if (!(triplePath.getObject().isVariable() && 
		            			triplePath.getSubject().isVariable() &&
		            			triplePath.getPredicate().isVariable())) {
		            		//not everything in bgp is var
		            		isSimpleBgp = false;
//		            		System.out.println("not all vars");
//		            		System.out.println(triplePath.toString());
		            		break;
		            	}
		            }
		        }
		    }
		);
//		if (!isSimpleBgp && query.toString().contains("?s ?p ?o")) {
//    		System.out.println(query.toString());
//    		System.exit(1);
//    	}
		return isSimpleBgp;
	}
	
	public static void main(String[] args)  {
		try {
			QueryWrapper queryWrapper = new QueryWrapper("ASK\n" + 
					"WHERE\n" + 
					"  { ?s ?p ?o }");
			SimpleBgpFilter filter = new SimpleBgpFilter();

			System.out.println(filter.filter(queryWrapper));
//			ArrayList<QueryWrapper> queries = qaldQueries.getQueries();
			
//			for (QueryWrapper query: queries) {
//				System.out.println(Integer.toString(query.getQueryId()));
//			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

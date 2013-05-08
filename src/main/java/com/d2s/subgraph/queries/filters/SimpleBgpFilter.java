package com.d2s.subgraph.queries.filters;

import java.util.Iterator;

import com.d2s.subgraph.queries.QueryWrapper;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementVisitorBase;
import com.hp.hpl.jena.sparql.syntax.ElementWalker;

public class SimpleBgpFilter implements QueryFilter {
	boolean isSimpleBgp = true;
	boolean onlyVariables = true;
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
		            while (triples.hasNext()) {
		            	TriplePath triplePath = triples.next();
		            	if (!(triplePath.getObject().isVariable() && 
		            			triplePath.getSubject().isVariable() &&
		            			triplePath.getPredicate().isVariable())) {
		            		//not everything in bgp is var
		            		isSimpleBgp = false;
		            		break;
		            	}
		            }
		        }
		    }
		);
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

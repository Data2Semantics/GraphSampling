package com.d2s.subgraph.queries.filters;

import java.util.Iterator;

import org.data2semantics.query.Query;
import org.data2semantics.query.QueryCollection;
import org.data2semantics.query.filters.QueryFilter;

import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementVisitorBase;
import com.hp.hpl.jena.sparql.syntax.ElementWalker;

public class SimpleBgpFilter implements QueryFilter {
	boolean isSimpleBgp = true;
	boolean onlyVariables = true;
	public boolean filter(final Query query) {
		isSimpleBgp = true;
		Element qPattern = query.getQueryPattern();

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
			Query query = Query.create("ASK\n" + 
					"WHERE\n" + 
					"  { ?s ?p ?o }", new QueryCollection());
			SimpleBgpFilter filter = new SimpleBgpFilter();

			System.out.println(filter.filter(query));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

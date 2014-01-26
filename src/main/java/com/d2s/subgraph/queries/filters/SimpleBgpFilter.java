package com.d2s.subgraph.queries.filters;

import org.data2semantics.query.Query;
import org.data2semantics.query.QueryCollection;
import org.data2semantics.query.filters.QueryFilter;

import com.hp.hpl.jena.sparql.syntax.Element;

public class SimpleBgpFilter implements QueryFilter {
	boolean isSimpleBgp = true;
	boolean onlyVariables = true;
	public boolean filter(final Query query) {
		isSimpleBgp = true;
		Element qPattern = query.getQueryPattern();
		SimpleBgpVisitor visitor = new SimpleBgpVisitor();
		qPattern.visit(visitor);
		
		return visitor.isSimpleBgp();
	}
	
	public static void main(String[] args)  {
		try {
			Query query = Query.create("ASK\n" + 
					"WHERE\n" + 
					"  { ?s ?p ?sd. OPTIONAL {?x ?j <http://bla>}}", new QueryCollection());
			SimpleBgpFilter filter = new SimpleBgpFilter();
			
//			true: simple
//			false: not simple
			System.out.println(filter.filter(query));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

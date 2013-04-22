package com.d2s.subgraph.queries.filters;


import com.d2s.subgraph.queries.QueryWrapper;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementNamedGraph;
import com.hp.hpl.jena.sparql.syntax.ElementVisitorBase;
import com.hp.hpl.jena.sparql.syntax.ElementWalker;

public class GraphClauseFilter implements QueryFilter {
	boolean hasGraphClause;
	public boolean filter(final QueryWrapper query) {
		hasGraphClause = false;
		Element qPattern = query.getQuery().getQueryPattern();

		// This will walk through all parts of the query
		ElementWalker.walk(qPattern,
		    // For each element...
		    new ElementVisitorBase() {
		        // ...when it's a block of triples...
		        public void visit(ElementNamedGraph el) {
		        	hasGraphClause = true;
		        }
		    }
		);
		return hasGraphClause;
	}
	
	public static void main(String[] args)  {
		try {
			QueryWrapper queryWrapper = new QueryWrapper("PREFIX  rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
					"PREFIX  foaf: <http://xmlns.com/foaf/0.1/>\n" + 
					"PREFIX  bibo: <http://purl.org/ontology/bibo/>\n" + 
					"PREFIX  skos: <http://www.w3.org/2004/02/skos/core#>\n" + 
					"PREFIX  swrcext: <http://www.cs.vu.nl/~mcaklein/onto/swrc_ext/2005/05#>\n" + 
					"\n" + 
					"SELECT DISTINCT  ?pred ?author_url ?author_name ?author_pref_label\n" + 
					"FROM <http://df_sample_0.5.nt>\n" + 
					"WHERE\n" + 
					"  {    { <http://data.semanticweb.org/conference/eswc/2008/paper/209> bibo:authorList ?authorList }\n" + 
					"        UNION\n" + 
					"          { <http://data.semanticweb.org/conference/eswc/2008/paper/209> swrcext:authorList ?authorList }\n" + 
					"        ?authorList ?pred ?author_url\n" + 
					"          { ?author_url foaf:name ?author_name }\n" + 
					"        UNION\n" + 
					"          { ?author_url rdfs:label ?author_name }\n" + 
					"        OPTIONAL\n" + 
					"          { ?author_url skos:prefLabel ?author_pref_label }\n" + 
//					"      }\n" + 
					"  }\n" + 
					"ORDER BY ?pred\n" + 
					"");
			GraphClauseFilter filter = new GraphClauseFilter();

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

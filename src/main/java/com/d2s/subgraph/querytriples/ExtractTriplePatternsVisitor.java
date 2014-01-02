package com.d2s.subgraph.querytriples;

import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementAssign;
import com.hp.hpl.jena.sparql.syntax.ElementBind;
import com.hp.hpl.jena.sparql.syntax.ElementData;
import com.hp.hpl.jena.sparql.syntax.ElementDataset;
import com.hp.hpl.jena.sparql.syntax.ElementExists;
import com.hp.hpl.jena.sparql.syntax.ElementFetch;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementMinus;
import com.hp.hpl.jena.sparql.syntax.ElementNamedGraph;
import com.hp.hpl.jena.sparql.syntax.ElementNotExists;
import com.hp.hpl.jena.sparql.syntax.ElementOptional;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementService;
import com.hp.hpl.jena.sparql.syntax.ElementSubQuery;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.ElementUnion;
import com.hp.hpl.jena.sparql.syntax.ElementVisitor;

public class ExtractTriplePatternsVisitor implements ElementVisitor {
	Set<Triple> triples = new HashSet<Triple>();
	
	public Set<Triple> getTriples() {
		return this.triples;
	}

	public void visit(ElementTriplesBlock el) {
		// TODO Auto-generated method stub

	}

	public void visit(ElementPathBlock el) {
		// ListIterator<TriplePath> it = el.getPattern().iterator();
		// while ( it.hasNext() ) {
		// final TriplePath tp = it.next();
		// final Var d = Var.alloc( "d" );
		// if ( tp.getSubject().equals( d )) {
		// it.add( new TriplePath( new Triple( d, d, d )));
		// }
		// }

		ListIterator<TriplePath> it = el.getPattern().iterator();
		
		while (it.hasNext()) {
			final TriplePath origTriple = it.next();
			Triple triple = origTriple.asTriple();
			if (triple != null) {
				triples.add(triple);
			} else {
				System.out.println(origTriple.toString() + " is not a triple!");
			}
			
		}
	}
	

	public void visit(ElementFilter el) {
		// TODO Auto-generated method stub

	}

	public void visit(ElementAssign el) {
		// TODO Auto-generated method stub

	}

	public void visit(ElementBind el) {
		// TODO Auto-generated method stub

	}

	public void visit(ElementUnion el) {
		for (Element e : el.getElements()) {
			e.visit(this);
		}
	}

	public void visit(ElementOptional el) {
		el.getOptionalElement().visit(this);
	}

	public void visit(ElementGroup el) {
		for (Element e : el.getElements()) {
			e.visit(this);
		}
	}

	public void visit(ElementDataset el) {
		// TODO Auto-generated method stub

	}

	public void visit(ElementNamedGraph el) {
		el.getElement().visit(this);
	}

	public void visit(ElementExists el) {
		// TODO Auto-generated method stub

	}

	public void visit(ElementNotExists el) {
		// TODO Auto-generated method stub

	}

	public void visit(ElementMinus el) {
		// TODO Auto-generated method stub

	}

	public void visit(ElementService el) {
		// TODO Auto-generated method stub

	}

	public void visit(ElementFetch el) {
		// TODO Auto-generated method stub

	}

	public void visit(ElementSubQuery el) {
		// TODO Auto-generated method stub

	}

	public void visit(ElementData el) {
		// TODO Auto-generated method stub

	}
}

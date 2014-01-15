package com.d2s.subgraph.queries.qtriples.visitors;

import java.util.HashSet;
import java.util.Iterator;
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
	private int optionalDepth;
	private int unionDepth;
	
	public Set<Triple> getTriples() {
		return this.triples;
	}

	public void visit(ElementTriplesBlock el) {
		Iterator<Triple> it = el.getPattern().iterator();
		while (it.hasNext()) {
			fetchFromTriple(it.next());
		}
	}

	public void visit(ElementPathBlock el) {

		ListIterator<TriplePath> it = el.getPattern().iterator();
		
		while (it.hasNext()) {
			final TriplePath origTriple = it.next();
			fetchFromTriple(origTriple.asTriple());
//			if (!added) {
//				System.out.println(origTriple.toString() + " is not a triple!");
//			}
			
		}
	}
	
	public void fetchFromTriple(Triple triple) {
		if (triple != null) {
			if (tripleContainsVar(triple)) {
				if (optionalDepth > 0) {
					//this means we don't have an answer for this optional. We can safely ignore this
					//System.out.println("warn: variable found in optional triple pattern. " + triple.toString());
				} else if (unionDepth > 0) {
					//this means we don't have an answer for side of the union. We can safely ignore this
					//System.out.println("warn: variable found in union triple pattern. " + triple.toString());
				} else {
					throw new IllegalArgumentException("we still have a variable in our triple. this should not be the case");
				}
			} else {
				triples.add(triple);
			}
		} else {
			throw new IllegalArgumentException("we tried to fetch values from triple pattern. However, triple pattern is null!");
		}
	}
	
	public boolean tripleContainsVar(Triple triple) {
		return (triple.getSubject().isVariable() || triple.getPredicate().isVariable() || triple.getObject().isVariable());
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
		unionDepth++;
		for (Element e : el.getElements()) {
			e.visit(this);
		}
		unionDepth--;
	}

	public void visit(ElementOptional el) {
		optionalDepth++;
		el.getOptionalElement().visit(this);
		optionalDepth--;
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

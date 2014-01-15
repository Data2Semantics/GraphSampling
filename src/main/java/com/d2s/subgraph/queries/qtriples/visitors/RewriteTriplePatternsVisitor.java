package com.d2s.subgraph.queries.qtriples.visitors;

import java.util.ListIterator;

import com.d2s.subgraph.queries.Query;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.core.Var;
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

public class RewriteTriplePatternsVisitor implements ElementVisitor {
	private enum TripleLoc {SUB, PRED, OBJ};
	private RDFNode node;
	private Query query;
	// private String varName;
	private Var varName;

	public RewriteTriplePatternsVisitor(String varName, RDFNode node, Query query) {
		this.node = node;
		this.query = query;
		this.varName = Var.alloc(varName);
	}

	public void visit(ElementTriplesBlock el) {
		// TODO Auto-generated method stub

	}

	public void visit(ElementPathBlock el) {
		ListIterator<TriplePath> it = el.getPattern().iterator();
		
		while (it.hasNext()) {
			final TriplePath origTriple = it.next();
			if (origTriple.getSubject().equals(varName)) {
				setNodeInTriplePattern(TripleLoc.SUB, origTriple, it);
			}
			if (origTriple.isTriple() && origTriple.getPredicate().equals(varName)) {
				setNodeInTriplePattern(TripleLoc.PRED, origTriple, it);
			}
			if (origTriple.getObject().equals(varName)) {
				setNodeInTriplePattern(TripleLoc.OBJ, origTriple, it);
			}
			
		}
	}
	private void setNodeInTriplePattern(TripleLoc location, TriplePath origTriple, ListIterator<TriplePath> it) {
		TriplePath newPath;
		if (origTriple.isTriple() || location == TripleLoc.PRED) {
			newPath = new TriplePath(new Triple(
					(location == TripleLoc.SUB? node.asNode(): origTriple.getSubject()), 
					(location == TripleLoc.PRED? node.asNode(): origTriple.getPredicate()), 
					(location == TripleLoc.OBJ? node.asNode(): origTriple.getObject()) 
					));
		} else {
			//contains a path
			newPath = new TriplePath(
					(location == TripleLoc.SUB? node.asNode(): origTriple.getSubject()), 
					origTriple.getPath(), 
					(location == TripleLoc.OBJ? node.asNode(): origTriple.getObject()) 
					);
		}
		it.set(newPath);
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

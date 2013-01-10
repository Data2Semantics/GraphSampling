package com.d2s.partialreplication.exploration;
/**
 * Code to explore (e.g. random walk) a rdf graph using the sesame api
 */
import java.io.File;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.nativerdf.NativeStore;

public class Explore {
	
	private static String DATA_DIR = "/usr/share/tomcat7/.aduna/openrdf-sesame/repositories/replica";

	public void doExplore() throws RepositoryException {
		File dataDir = new File(DATA_DIR);
		Repository myRepository = new SailRepository(new NativeStore(dataDir));
		myRepository.initialize();
		RepositoryConnection repConnection = myRepository.getConnection();
		ValueFactory f = myRepository.getValueFactory();

		// create some resources and literals to make statements out of
		URI subj = f.createURI("http://example/egbook3");
		URI pred = null;
		Value obj = null;
		boolean includeInferred = false;
		Resource contexts = null;
		RepositoryResult<Statement> results = repConnection.getStatements(subj, pred, obj, includeInferred, contexts);
		
		while(results.hasNext()) {
			Statement result = results.next();
			System.out.println(result.toString());
		}
	}
	
	
    public static void main( String[] args ) throws RepositoryException
    {
    	Explore explore = new Explore();
    	explore.doExplore();
    }
}

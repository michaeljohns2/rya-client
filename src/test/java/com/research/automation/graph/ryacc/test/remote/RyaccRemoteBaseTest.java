package com.research.automation.graph.ryacc.test.remote;

import static org.junit.Assert.fail;

import org.apache.log4j.Logger;
import org.openrdf.model.Statement;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.sail.Sail;

import com.research.automation.graph.ryacc.AbstractRyaccBaseTest;

/**
 * Demonstrates how to interact with a remote instance of Rya via {@link Sail}.
 * 
 * Tested with rya-docker at https://github.com/michaeljohns2/rya-docker
 * 
 * Reference example from http://graphdb.ontotext.com/sesame/users/ch08.html#d0e929
 */

public class RyaccRemoteBaseTest extends AbstractRyaccBaseTest<Repository> {

	/**
	 * CHANGE THIS VALUE WHEN YOU NEED TO ADD TO THE REMOTE REPO
	 */
	public static final boolean SKIP_REMOTE_ADD = true;

	private static final Logger log = Logger.getLogger(RyaccRemoteBaseTest.class);

	final protected String sesameServer = "http://localhost:8080/openrdf-sesame/";
	final protected String repositoryID = "RyaAccumulo";	

	/**
	 * Default constructor.
	 * Assumes not using fluo.
	 */
	public RyaccRemoteBaseTest(){
		super(false,SKIP_REMOTE_ADD);
	}

	@Override
	public void beforeClassSetup() throws Exception { 

		//		log.info("1. Setting up the Logging used by this example.");
		//		//		setupLogging();

		log.info("1. Setting up the Repository object used by this example.");

		setRyaT(new HTTPRepository(sesameServer, repositoryID));
		ryaT.initialize();

		baseURI = "http://";
		//		file = Paths.get("","src/test/resources","add_data.sparql").toFile();

		vf = ryaT.getValueFactory();

		log.info("2. Generate Statements.");
		this.setTesterAndGenerateStatements(new RyaccRemoteTester(vf,fluo,ryaT));
		this.addStatementsForTests();//will handle `skipAdd`
	}

	@Override
	public void afterClassSetup() {
		try {
			ryaT.shutDown();
		} catch (RepositoryException e) {			
			e.printStackTrace();
		}
	}

	@Override
	public void addStatementsForTestsInternal() {

		log.info("TEST STEP 1 --> ADD STATEMENTS");
		try {
			rs.addStatements(
					ryaT,
					statements.toArray(new Statement[statements.size()]));
		} catch (RepositoryException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}	

	//	@Test
	//	public void openRdfTest(){
	//
	//		RepositoryConnection conn = null;
	//
	//		try {
	//
	//			conn = repo.getConnection();
	//			System.out.println("...have conn");
	//
	//
	//		} catch(Exception e){
	//			e.printStackTrace();
	//			fail(e.getMessage());
	//		} finally {
	//			if (conn != null){
	//				try {
	//					conn.close();
	//				} catch (RepositoryException e) {
	//					e.printStackTrace();
	//					fail(e.getMessage());
	//				}
	//			}
	//		}
	//
	//	}




	//	@Test
	//	public void addTest(){
	//
	//		RepositoryConnection conn = null;
	//		try {
	//			
	//			conn = repo.getConnection();
	//
	//			//				conn.add(file, baseURI, RDFFormat.RDFXML);
	//
	//			//				URL url = new URL("http://example.org/example/remote.rdf");
	//			//				conn.add(url, url.toString(), RDFFormat.RDFXML);
	//			
	//			
	//
	//		} catch (Exception e) {
	//			e.printStackTrace();
	//			fail(e.getMessage());
	//		} finally {
	//			if (conn != null){
	//				try {
	//					conn.close();
	//				} catch (RepositoryException e) {
	//					e.printStackTrace();
	//				}
	//			}
	//		}
	//
	//	}

}
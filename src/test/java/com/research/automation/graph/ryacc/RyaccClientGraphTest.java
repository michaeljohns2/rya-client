package com.research.automation.graph.ryacc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.junit.Test;

/**
 * This class encode a variety of Graph Tests.
 * 
 * THERE ARE SOME QUIRKS WITH HOW RYA JOINS GRAPHS UNDER THIS CONFIGURATION.
 * MOST NOTABLY, STATEMENTS MAY NOT ALL BE TESTED BASED ON CLAUSE ORDER. 
 * 
 * SOLUTION: 
 *   (1) RUN MULTIPLE QUERIES E.G. ONE WITH AND WITHOUT A TRULY OPTIONAL STATEMENT
 *   (2) ORDER STATEMENTS IN ORDER OF CONCERN WITH MOST REQUIRED AT THE TOP
 *   (3) MERGE MULTIPLE QUERIES (SEE TESTS FOR EXAMPLE)
 * 
 * 
 * 
 * @author mjohns
 *
 */
public class RyaccClientGraphTest extends RyaccBaseTest{

	private static final Logger log = Logger.getLogger(RyaccClientTest.class);

	@Test
	public void ryaccClientGraphTest(){	
		try {
			log.info("TEST STEP 2 --> EXECUTE SPARQL QUERY (DISTINGUISHING GRAPHS)");


			int expectedCnt = 3;
			JSONArray results = rs.sparqlQuery(Wrap.sparqlQueryGraphsLinked(selectParams,whereArr,graph1,graph3));			
			assertEquals(String.format("Expected count %d (%s,%s)",expectedCnt,graph1,graph3),expectedCnt,results.length());

			log.info("...VALIDATE RESULTS");
			log.info(results.toString(4));			

			log.info("TEST STEP 3 --> EXECUTE SPARQL QUERY (DISTINGUISHING GRAPHS FROM FILE)");

			expectedCnt = 3;
			String queryFile = "graphs_options.sparql";
			try(InputStream is = RyaccClientGraphTest.class.getClassLoader().getResourceAsStream(queryFile)){
				results = rs.sparqlQuery(IOUtils.toString(is));
			}			
			assertEquals(String.format("Expected count %d (file %s)",expectedCnt,queryFile),expectedCnt,results.length());

			log.info("...VALIDATE RESULTS");
			log.info(results.toString(4));

		} catch(Exception e){
			e.printStackTrace();
			fail(e.getMessage());
		} 
	}
}
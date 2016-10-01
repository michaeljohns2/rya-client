package com.research.automation.graph.ryacc.test.testee;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;

import com.research.automation.graph.ryacc.AbstractRyaccTester;
import com.research.automation.graph.ryacc.RyaccStatementer;
import com.research.automation.graph.ryacc.Wrap;
import com.research.automation.graph.ryacc.test.mini.RyaccMiniClientGraphTest;

/**
 * This class encode a Graph Context Tests.
 * 
 * THERE ARE SOME QUIRKS WITH HOW RYA JOINS GRAPHS UNDER THIS CONFIGURATION.
 * MOST NOTABLY, STATEMENTS MAY NOT ALL BE TESTED BASED ON CLAUSE ORDER. 
 * 
 * @author mjohns
 *
 */
public class RyaccClientGraphTestee <T> implements Testee<T>{
	
	private static final Logger log = Logger.getLogger(RyaccClientGraphTestee.class);
	
	@Override
	public void doTest(AbstractRyaccTester<T> tester) throws Exception{
		
		RyaccStatementer<T> rs = tester.getRs(); 
	    T ryaT = tester.getRyaT();
		
	    log.info("TEST STEP 2 --> EXECUTE SPARQL QUERY (DISTINGUISHING GRAPHS)");


		int expectedCnt = 3;
		JSONArray results = rs.sparqlQueryT(ryaT,Wrap.sparqlQueryGraphsLinked(selectParams,whereArr,graph1,graph3));			
		assertEquals(String.format("Expected count %d (%s,%s)",expectedCnt,graph1,graph3),expectedCnt,results.length());

		log.info("...VALIDATE RESULTS");
		log.info(results.toString(4));			

		log.info("TEST STEP 3 --> EXECUTE SPARQL QUERY (DISTINGUISHING GRAPHS FROM FILE)");

		expectedCnt = 3;
		String queryFile = "graphs_options.sparql";
		try(InputStream is = RyaccMiniClientGraphTest.class.getClassLoader().getResourceAsStream(queryFile)){
			results = rs.sparqlQueryT(ryaT,IOUtils.toString(is));
		}			
		assertEquals(String.format("Expected count %d (file %s)",expectedCnt,queryFile),expectedCnt,results.length());

		log.info("...VALIDATE RESULTS");
		log.info(results.toString(4));
	}
}

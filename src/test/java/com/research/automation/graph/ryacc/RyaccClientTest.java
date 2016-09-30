package com.research.automation.graph.ryacc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.junit.Test;

public class RyaccClientTest extends RyaccBaseTest{

	private static final Logger log = Logger.getLogger(RyaccClientTest.class);

	@Test
	public void ryaccClientTest(){	
		try {
			log.info("TEST STEP 2 --> EXECUTE SPARQL QUERY (NOT DISTINGUISHING GRAPH)");

			int expectedCnt = 4;
			JSONArray results = rs.sparqlQuery(Wrap.sparqlQuery(selectParams, whereArr));//all graphs
			assertEquals(String.format("Expected count %d",expectedCnt),expectedCnt,results.length());

			log.info("...VALIDATE RESULTS");
			log.info(results.toString(4));

		} catch(Exception e){
			e.printStackTrace();
			fail(e.getMessage());
		} 
	}
}

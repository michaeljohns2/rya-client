package com.research.automation.graph.ryacc.test.testee;

import static org.junit.Assert.assertEquals;

import org.apache.log4j.Logger;
import org.json.JSONArray;

import com.research.automation.graph.ryacc.AbstractRyaccTester;
import com.research.automation.graph.ryacc.RyaccStatementer;
import com.research.automation.graph.ryacc.Wrap;

public class RyaccClientTestee<T> implements Testee<T>{
	
	private static final Logger log = Logger.getLogger(RyaccClientTestee.class);
	
	@Override
	public void doTest(AbstractRyaccTester<T> tester) throws Exception{
		
		RyaccStatementer<T> rs = tester.getRs(); 
	    T ryaT = tester.getRyaT();
		
		log.info("TEST STEP 2 --> EXECUTE SPARQL QUERY (NOT DISTINGUISHING GRAPH)");

		int expectedCnt = 4;
		JSONArray results = rs.sparqlQueryT(ryaT,Wrap.sparqlQuery(selectParams, whereArr));//all graphs
		assertEquals(String.format("Expected count %d",expectedCnt),expectedCnt,results.length());

		log.info("...VALIDATE RESULTS");
		log.info(results.toString(4));
	}
}

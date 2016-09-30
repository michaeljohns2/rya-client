package com.research.automation.graph.ryacc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class MergeResultsTest extends RyaccBaseTest{

	private static final Logger log = Logger.getLogger(MergeResultsTest.class);

	@Test
	public void mergeResultsTest(){
		try{
			
			log.info("TEST STEP 2 --> SPARQL QUERY (MERGE RESULTS)");

			int expectedCnt = 5;
			JSONArray resultsRoot = JsonUtils.mergeUnique(
					rs.sparqlQuery(Wrap.sparqlQueryGraphsLinked(selectParams,whereArr)),
					rs.sparqlQuery(Wrap.sparqlQueryGraphsLinked(selectParams,whereArr2)));
			assertEquals(String.format("Expected count %d (merge results)",expectedCnt),expectedCnt,resultsRoot.length());

			log.info("...VALIDATE RESULTS");
			log.info(resultsRoot.toString(4));
			
			log.info("TEST STEP 3 --> SPARQL QUERY (MERGE ON KEY -- ADD_TO_MERGE_DIFF_KEY -- )");
			
			expectedCnt = 4;
			JSONArray results = JsonUtils.mergeUniqueWithKeyMatching(JsonUtils.copy(resultsRoot), MergeRule.ALL_SAME, MergeConflictRule.ADD_TO_MERGE_DIFF_KEY, "employee");
			assertEquals(String.format("Expected count %d (merge keys)",expectedCnt),expectedCnt,results.length());
			
			log.info("...VALIDATE RESULTS");
			log.info(results.toString(4));
			
			log.info("...BOB");			
			JSONObject bob = JsonUtils.findFirstEntryWith(results, "employee", Wrap.http("Bob"));
			log.info(bob.toString(4));
			assertTrue(String.format("Expected bob object to have key '%s'",MERGE_DIFF_KEY),bob.has(MERGE_DIFF_KEY));
			

			log.info("TEST STEP 4 --> PARAM COUNT FILTER (ONLY EMAILS)");

			expectedCnt = 1;
			results = JsonUtils.filterResultsBelowParamCount(JsonUtils.copy(results), 4);//note this builds on the previous result
			assertEquals(String.format("Expected count %d (merge) after post filter",
					expectedCnt),expectedCnt,results.length());
			
			log.info("...VALIDATE RESULTS");
			log.info(results.toString(4));
			
			log.info("TEST STEP 5 --> SPARQL QUERY (MERGE ON KEY -- OVERWRITE -- )");
			
			expectedCnt = 4;
			results = JsonUtils.mergeUniqueWithKeyMatching(JsonUtils.copy(resultsRoot), MergeRule.ALL_SAME, MergeConflictRule.OVERWRITE, "employee");
			assertEquals(String.format("Expected count %d (merge keys)",expectedCnt),expectedCnt,results.length());
			
			log.info("...VALIDATE RESULTS");
			log.info(results.toString(4));
			
			log.info("...BOB");			
			bob = JsonUtils.findFirstEntryWith(results, "employee", Wrap.http("Bob"));
			log.info(bob.toString(4));
			assertTrue(String.format("Expected bob object to have key '%s'","graph"),bob.has("graph"));
			assertEquals(String.format("Expected bob object with key '%s' to have value '%s'","graph",
					Wrap.http(graph0)),Wrap.http(graph0),bob.getString("graph"));
			
			log.info("TEST STEP 6 --> SPARQL QUERY (MERGE ON KEY -- SKIP_CONFLICT -- )");
			
			expectedCnt = 4;
			results = JsonUtils.mergeUniqueWithKeyMatching(JsonUtils.copy(resultsRoot), MergeRule.ALL_SAME, MergeConflictRule.SKIP_CONFLICT, "employee");
			assertEquals(String.format("Expected count %d (merge keys)",expectedCnt),expectedCnt,results.length());
			
			log.info("...VALIDATE RESULTS");
			log.info(results.toString(4));
			
			log.info("...BOB");			
			bob = JsonUtils.findFirstEntryWith(results, "employee", Wrap.http("Bob"));
			log.info(bob.toString(4));
			assertTrue(String.format("Expected bob object to have key '%s'","graph"),bob.has("graph"));
			assertEquals(String.format("Expected bob object with key '%s' to have value '%s'","graph",
					Wrap.http(graph1)),Wrap.http(graph1),bob.getString("graph"));
			
		} catch(Exception e){
			e.printStackTrace();
			fail(e.getMessage());
		} 
	}
}

package com.research.automation.graph.ryacc.test.mini;

import static org.junit.Assert.fail;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.openrdf.sail.Sail;

import com.research.automation.graph.ryacc.test.testee.RyaccMergeTestee;

public class RyaccMiniMergeResultsTest extends RyaccMiniBaseTest{

	private static final Logger log = Logger.getLogger(RyaccMiniMergeResultsTest.class);

	@Test
	public void mergeResultsTest(){
		try{
			RyaccMergeTestee<Sail> testee = new RyaccMergeTestee<>();
			testee.doTest(tester);
		} catch(Exception e){
			e.printStackTrace();
			fail(e.getMessage());
		} 
	}
}

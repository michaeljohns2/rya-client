package com.research.automation.graph.ryacc.test.remote;

import static org.junit.Assert.fail;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.openrdf.repository.Repository;

import com.research.automation.graph.ryacc.test.testee.RyaccMergeTestee;

public class RyaccRemoteMergeResultsTest extends RyaccRemoteBaseTest{

	private static final Logger log = Logger.getLogger(RyaccRemoteMergeResultsTest.class);

	@Test
	public void mergeResultsTest(){
		try{
			RyaccMergeTestee<Repository> testee = new RyaccMergeTestee<>();
			testee.doTest(tester);
		} catch(Exception e){
			e.printStackTrace();
			fail(e.getMessage());
		} 
	}
}

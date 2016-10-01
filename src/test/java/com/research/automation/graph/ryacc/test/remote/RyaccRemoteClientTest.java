package com.research.automation.graph.ryacc.test.remote;

import static org.junit.Assert.fail;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.openrdf.repository.Repository;

import com.research.automation.graph.ryacc.test.testee.RyaccClientTestee;

public class RyaccRemoteClientTest extends RyaccRemoteBaseTest{

	private static final Logger log = Logger.getLogger(RyaccRemoteClientTest.class);

	@Test
	public void ryaccClientTest(){	
		try {
			RyaccClientTestee<Repository> testee = new RyaccClientTestee<>();
			testee.doTest(tester);
		} catch(Exception e){
			e.printStackTrace();
			fail(e.getMessage());
		} 
	}
}

package com.research.automation.graph.ryacc.test.remote;

import static org.junit.Assert.fail;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.openrdf.repository.Repository;

import com.research.automation.graph.ryacc.test.testee.RyaccClientGraphTestee;

public class RyaccRemoteClientGraphTest extends RyaccRemoteBaseTest{

	private static final Logger log = Logger.getLogger(RyaccRemoteClientGraphTest.class);

	@Test
	public void ryaccClientGraphTest(){	
		try {
			RyaccClientGraphTestee<Repository> testee = new RyaccClientGraphTestee<>();
			testee.doTest(tester);
		} catch(Exception e){
			e.printStackTrace();
			fail(e.getMessage());
		} 
	}
}

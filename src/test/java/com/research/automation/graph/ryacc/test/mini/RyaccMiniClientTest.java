package com.research.automation.graph.ryacc.test.mini;

import static org.junit.Assert.fail;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.openrdf.sail.Sail;

import com.research.automation.graph.ryacc.test.testee.RyaccClientTestee;

public class RyaccMiniClientTest extends RyaccMiniBaseTest{

	private static final Logger log = Logger.getLogger(RyaccMiniClientTest.class);

	@Test
	public void ryaccClientTest(){	
		try {
			RyaccClientTestee<Sail> testee = new RyaccClientTestee<>();
			testee.doTest(tester);
		} catch(Exception e){
			e.printStackTrace();
			fail(e.getMessage());
		} 
	}
}

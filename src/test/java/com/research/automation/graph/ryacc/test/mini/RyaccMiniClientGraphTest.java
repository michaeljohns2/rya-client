package com.research.automation.graph.ryacc.test.mini;

import static org.junit.Assert.fail;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.openrdf.sail.Sail;

import com.research.automation.graph.ryacc.test.testee.RyaccClientGraphTestee;

public class RyaccMiniClientGraphTest extends RyaccMiniBaseTest{

	private static final Logger log = Logger.getLogger(RyaccMiniClientGraphTest.class);

	@Test
	public void ryaccClientGraphTest(){	
		try {
			RyaccClientGraphTestee<Sail> testee = new RyaccClientGraphTestee<>();
			testee.doTest(tester);
		} catch(Exception e){
			e.printStackTrace();
			fail(e.getMessage());
		} 
	}
}
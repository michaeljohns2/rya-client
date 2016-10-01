package com.research.automation.graph.ryacc.test.testee;

import com.research.automation.graph.ryacc.AbstractRyaccTester;
import com.research.automation.graph.ryacc.RyaccConstants;
import com.research.automation.graph.ryacc.RyaccTestConstants;

public interface Testee<T> extends RyaccConstants, RyaccTestConstants {

	void doTest(AbstractRyaccTester<T> tester) throws Exception;
}

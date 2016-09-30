package com.research.automation.graph.ryacc;

/**
 * Adapted from:
 * http://saltnlight5.blogspot.com/2012/09/enhancing-spring-test-framework-with.html
 * 
 * @author mjohns
 *
 */
public interface InstanceTestClassListener {
	
	void beforeClassSetup() throws Exception;
    void afterClassSetup();

}

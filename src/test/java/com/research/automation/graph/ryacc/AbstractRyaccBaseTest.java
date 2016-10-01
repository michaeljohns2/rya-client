package com.research.automation.graph.ryacc;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.zookeeper.ClientCnxn;
import org.junit.After;
import org.junit.runner.RunWith;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;

import io.fluo.api.mini.MiniFluo;

/**
 * Adapted from: 
 * https://github.com/apache/incubator-rya/blob/1937233962e9fabbdce036820c17731cb15de3e6/extras/indexingExample/src/main/java/RyaClientExample.java
 * 
 * Using test runner with support for before/after class instances (vice static) adapted from:
 * http://saltnlight5.blogspot.com/2012/09/enhancing-spring-test-framework-with.html
 */
@RunWith(InstanceTestClassRunner.class)
public abstract class AbstractRyaccBaseTest<T> implements InstanceTestClassListener, RyaccConstants, RyaccTestConstants {

	private static final Logger log = Logger.getLogger(AbstractRyaccBaseTest.class);

	/* quick toggle for debug console output (can override it per method) */
	protected boolean DEBUG_LOG = false;

	/* safety mainly for RyaccRemoteBaseTests as they are not blown away. */
	protected boolean skipAdd = true;

	protected ValueFactory vf = null;
	protected MiniFluo fluo = null;
	protected T ryaT = null;
	protected AbstractRyaccTester<T> tester = null;
	protected RyaccStatementer<T> rs = null;
	protected List<Statement> statements = null;

	//	protected File file = null;
	protected String baseURI = null;

	final protected boolean useFluo;
	

	/**
	 * Constructor.
	 * Specify whether to use fluo and whether to skipAdd (especially for remote tests).
	 * 
	 * @param useFluo boolean
	 * @param skipAdd
	 */
	public AbstractRyaccBaseTest(boolean useFluo, boolean skipAdd){
		this.useFluo = useFluo;
		this.skipAdd = skipAdd;
	}

	protected void setupLogging() {
		// Turn off all the loggers and customize how they write to the console.
		final Logger rootLogger = LogManager.getRootLogger();
		rootLogger.setLevel(Level.OFF);
		final ConsoleAppender ca = (ConsoleAppender) rootLogger.getAppender("stdout");

		System.out.println(String.format("ca null? %s",(ca == null? "true" : "false")));
		if (ca != null) ca.setLayout(new PatternLayout("%-5p - %m%n"));

		// Turn the logger used by the demo back on.
		log.setLevel(Level.INFO);
		Logger.getLogger(ClientCnxn.class).setLevel(Level.ERROR);
	}
	
	protected void setRyaT(T ryaT){
		this.ryaT = ryaT;
	}

	protected void setTesterAndGenerateStatements(AbstractRyaccTester<T> tester){
		this.tester = tester;
		this.rs = tester.getRs();
		this.statements = generateStatements();
	}

	/**
	 * Subclasses can override.
	 *
	 */
	protected List<Statement> generateStatements() {
		return Arrays.asList(
				rs.createUriStatement("Eve",		pTalks,	"Charlie",	graph1),
				rs.createUriStatement("Eve",		pTalks,	"Bob",		graph1),
				rs.createUriStatement("Charlie",	pWorks,	coffeeShop,	graph1),
				rs.createUriStatement("David",		pTalks,	"Alice",	graph2),
				rs.createUriStatement("Alice",		pWorks,	coffeeShop,	graph2),
				rs.createUriStatement("Bob",		pWorks,	coffeeShop,	graph3),
				rs.createUriStatement("George",	pTalks,	"Frank",	graph3),
				rs.createUriStatement("Frank",		pWorks,	coffeeShop,	graph3),
				rs.createStatement(
						vf.createURI(Wrap.http("Bob")),
						vf.createURI(Wrap.http(pEmail)),
						rs.createValue(Wrap.mail("bob@coffee.com")),
						vf.createURI(Wrap.http(graph0)))
				);
	}


	@After
	public void resetDebugLogToFalse() {
		//!!! this is called after each test!!!
		DEBUG_LOG = false;
	}

	final public void addStatementsForTests(){
		if (!skipAdd){
			this.addStatementsForTestsInternal();
		} else {
			log.info("######################################################");
			log.info("NOT ADDING ANYTHING TO REMOTE BECAUSE SKIPADD IS TRUE");
			log.info("######################################################");
			return;
		}
	}

	protected abstract void addStatementsForTestsInternal();
}

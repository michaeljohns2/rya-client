package com.research.automation.graph.ryacc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.accumulo.minicluster.MiniAccumuloCluster;
import org.apache.accumulo.minicluster.MiniAccumuloConfig;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.rya.indexing.pcj.fluo.app.export.rya.RyaExportParameters;
import org.apache.rya.indexing.pcj.fluo.app.observers.FilterObserver;
import org.apache.rya.indexing.pcj.fluo.app.observers.JoinObserver;
import org.apache.rya.indexing.pcj.fluo.app.observers.QueryResultObserver;
import org.apache.rya.indexing.pcj.fluo.app.observers.StatementPatternObserver;
import org.apache.rya.indexing.pcj.fluo.app.observers.TripleObserver;
import org.apache.zookeeper.ClientCnxn;
import org.junit.After;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.openrdf.model.ValueFactory;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;

import io.fluo.api.client.FluoAdmin;
import io.fluo.api.client.FluoAdmin.AlreadyInitializedException;
import io.fluo.api.client.FluoAdmin.TableExistsException;
import io.fluo.api.client.FluoFactory;
import io.fluo.api.config.FluoConfiguration;
import io.fluo.api.config.ObserverConfiguration;
import io.fluo.api.mini.MiniFluo;
import mvm.rya.accumulo.AccumuloRdfConfiguration;
import mvm.rya.api.client.Install.DuplicateInstanceNameException;
import mvm.rya.api.client.Install.InstallConfiguration;
import mvm.rya.api.client.RyaClient;
import mvm.rya.api.client.RyaClientException;
import mvm.rya.api.client.accumulo.AccumuloConnectionDetails;
import mvm.rya.api.client.accumulo.AccumuloRyaClientFactory;
import mvm.rya.api.persist.RyaDAOException;
import mvm.rya.indexing.accumulo.ConfigUtils;
import mvm.rya.indexing.external.PrecomputedJoinIndexerConfig;
import mvm.rya.rdftriplestore.inference.InferenceEngineException;
import mvm.rya.sail.config.RyaSailFactory;

/**
 * Demonstrates how a {@link RyaClient} may be used to interact with an instance of Accumulo to install and manage a Rya instance.
 * Adapted from: 
 * https://github.com/apache/incubator-rya/blob/1937233962e9fabbdce036820c17731cb15de3e6/extras/indexingExample/src/main/java/RyaClientExample.java
 * 
 * Using test runner with support for before/after class instances (vice static) adapted from:
 * http://saltnlight5.blogspot.com/2012/09/enhancing-spring-test-framework-with.html
 */
@RunWith(InstanceTestClassRunner.class)
public class RyaccBaseTest implements InstanceTestClassListener, RyaccConstants {
	private static final Logger log = Logger.getLogger(RyaccBaseTest.class);

	final protected String accumuloUsername = "root";
	final protected String accumuloPassword = "password";
	final protected String accumuloAuths = "U";	
	final protected String ryaInstanceName = "demoInstance_";	
	final protected String fluoAppName = "demoInstance_pcjUpdater";

	/* quick toggle for debug console output (can override it per method) */
	protected boolean DEBUG_LOG = false;

	protected MiniAccumuloConfig aConfig = null;
	protected MiniAccumuloCluster aCluster = null;	 
	protected Connector aConn = null;

	protected RyaClient ryaClient = null;
	protected MiniFluo fluo = null;
	protected Sail ryaSail = null;
	protected RyaccStatement rs = null;

	protected AccumuloConnectionDetails aConnDetails = null;
	protected InstallConfiguration ryaInstallConfig = null;
	protected AccumuloRdfConfiguration aRdfConf = null;
	protected ValueFactory vf = null;

	protected String aInstanceName = null;
	protected String zooKeepers = null;

	protected TemporaryFolder miniDataDir = null;

	final protected boolean useFluo;


	final protected String pTalks = "talksTo";
	final protected String pWorks = "worksAt";
	final protected String coffeeShop = "CoffeeShop";
	final protected String pEmail = "hasEmail";

	final protected String graph0 = "graph0";
	final protected String graph1 = "graph1";
	final protected String graph2 = "graph2";
	final protected String graph3 = "graph3";

	final protected String[] selectParams = {"graph","patron","employee","email"};

	final protected String[] whereArr = {
			"?patron <"+Wrap.http(pTalks)+"> ?employee .",
			"?employee <"+Wrap.http(pWorks)+"> <"+Wrap.http(coffeeShop)+"> . "
	};

	final protected String[] whereArr2 = {
			"?employee <"+Wrap.http(pEmail)+"> ?email . ",
			"?employee <"+Wrap.http(pWorks)+"> <"+Wrap.http(coffeeShop)+"> . ",
			"?patron <"+Wrap.http(pTalks)+"> ?employee ."
	};

	/**
	 * Default constructor.
	 * Assumes using fluo.
	 */
	public RyaccBaseTest(){
		this(true);
	}

	/**
	 * Constructor.
	 * Specify whether to use fluo.
	 * 
	 * @param useFluo boolean
	 */
	public RyaccBaseTest(boolean useFluo){
		this.useFluo = useFluo;
	}

	@Override
	public void beforeClassSetup() throws Exception { 

		log.info("1. Setting up the Logging used by this example.");
		//		setupLogging();

		log.info("2. Setting up the Mini Data Dir used by this example.");
		setupMiniDataDir();

		log.info("3. Setting up the Mini Accumulo instance used by this example.");
		setupMiniAccumulo();

		log.info("4. Setting up the Rya Client instance used by this example.");		
		setupRyaClient();

		log.info("5. Setting up the Mini Fluo instance used by this example (used to incrementally update the PCJ indicies).");		
		setupMiniFluo();

		log.info("6. Setting up the Rya Sail instance used by this example.");
		setupRyaSail();

		log.info("TEST STEP 1 --> ADD STATEMENTS");
		addStatements();
	}

	/**
	 * Subclasses can override.
	 * 
	 * @throws SailException 
	 */
	protected void addStatements() throws SailException{
		
		rs = new RyaccStatement(ryaSail,vf,fluo); 

		rs.addStatements(
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

	protected void setupMiniDataDir() throws IOException{
		miniDataDir = new TemporaryFolder();
		miniDataDir.create();
	}

	protected void setupMiniAccumulo() throws IOException, InterruptedException, AccumuloException, AccumuloSecurityException{
		aConfig = new MiniAccumuloConfig(miniDataDir.getRoot(), accumuloPassword);
		aCluster = new MiniAccumuloCluster(aConfig);
		aCluster.start();

		aInstanceName = aCluster.getInstanceName();
		zooKeepers = aCluster.getZooKeepers();

		// reference to connector
		aConn = aCluster.getConnector(accumuloUsername, accumuloPassword);

		// Give the root user authorizations.
		aConn.securityOperations().changeUserAuthorizations(accumuloUsername, new Authorizations(accumuloAuths));

		// This is used by Rya Client
		aConnDetails = new AccumuloConnectionDetails(
				accumuloUsername, 
				accumuloPassword.toCharArray(),
				aInstanceName,
				zooKeepers);
	}

	protected void setupRyaClient() throws DuplicateInstanceNameException, RyaClientException{

		ryaClient = AccumuloRyaClientFactory.build(aConnDetails, aConn);

		// Install an instance of Rya that has all of the secondary indexers turned on.		
		ryaInstallConfig = InstallConfiguration.builder()
				.setEnableTableHashPrefix(true)
				.setEnableEntityCentricIndex(true)
				.setEnableGeoIndex(true)
				.setEnableFreeTextIndex(true)
				.setEnableTemporalIndex(true)
				.setEnablePcjIndex(useFluo)
				.setFluoPcjAppName(fluoAppName)
				.build();

		ryaClient.getInstall().install(ryaInstanceName, ryaInstallConfig);
	}

	protected void setupMiniFluo() throws AlreadyInitializedException, TableExistsException{

		// Setup the observers that will be used by the Fluo PCJ Application.
		final List<ObserverConfiguration> observers = new ArrayList<>();
		observers.add(new ObserverConfiguration(TripleObserver.class.getName()));
		observers.add(new ObserverConfiguration(StatementPatternObserver.class.getName()));
		observers.add(new ObserverConfiguration(JoinObserver.class.getName()));
		observers.add(new ObserverConfiguration(FilterObserver.class.getName()));

		// Provide export parameters child test classes may provide to the
		// export observer.
		final ObserverConfiguration exportObserverConfig = new ObserverConfiguration(
				QueryResultObserver.class.getName());

		final HashMap<String, String> params = new HashMap<>();
		final RyaExportParameters ryaParams = new RyaExportParameters(params);
		ryaParams.setExportToRya(true);
		ryaParams.setAccumuloInstanceName(aInstanceName);
		ryaParams.setZookeeperServers(zooKeepers);
		ryaParams.setExporterUsername(accumuloUsername);
		ryaParams.setExporterPassword(accumuloPassword);
		ryaParams.setRyaInstanceName(fluoAppName);

		exportObserverConfig.setParameters(params);
		observers.add(exportObserverConfig);

		// Configure how the mini fluo will run.
		final FluoConfiguration config = new FluoConfiguration();
		config.setMiniStartAccumulo(false);
		config.setAccumuloInstance(aInstanceName);
		config.setAccumuloUser(accumuloUsername);
		config.setAccumuloPassword(accumuloPassword);
		config.setInstanceZookeepers(zooKeepers + "/fluo");
		config.setAccumuloZookeepers(zooKeepers);

		config.setApplicationName(fluoAppName);
		config.setAccumuloTable("fluo" + fluoAppName);

		config.addObservers(observers);

		FluoFactory.newAdmin(config).initialize(
				new FluoAdmin.InitOpts().setClearTable(true).setClearZookeeper(true) );
		fluo = FluoFactory.newMiniFluo(config);
	}

	protected void setupRyaSail() 
			throws SailException, AccumuloException, AccumuloSecurityException, RyaDAOException, InferenceEngineException{

		aRdfConf = new AccumuloRdfConfiguration();
		aRdfConf.setTablePrefix(ryaInstanceName);
		aRdfConf.set(ConfigUtils.CLOUDBASE_USER, accumuloUsername);
		aRdfConf.set(ConfigUtils.CLOUDBASE_PASSWORD, accumuloPassword);
		aRdfConf.set(ConfigUtils.CLOUDBASE_INSTANCE, aInstanceName);
		aRdfConf.set(ConfigUtils.CLOUDBASE_ZOOKEEPERS, zooKeepers);
		aRdfConf.set(ConfigUtils.CLOUDBASE_AUTHS, accumuloAuths);
		aRdfConf.set(ConfigUtils.USE_PCJ_FLUO_UPDATER, useFluo? "true" : "false");			
		aRdfConf.set(ConfigUtils.FLUO_APP_NAME, fluoAppName);
		aRdfConf.set(ConfigUtils.PCJ_STORAGE_TYPE, PrecomputedJoinIndexerConfig.PrecomputedJoinStorageType.ACCUMULO.toString());
		aRdfConf.set(ConfigUtils.PCJ_UPDATER_TYPE, PrecomputedJoinIndexerConfig.PrecomputedJoinUpdaterType.FLUO.toString());

		ryaSail = RyaSailFactory.getInstance(aRdfConf);

		vf = ryaSail.getValueFactory();
	}

	@Override
	public void afterClassSetup() {

		log.info("1. Shutting down the Rya Sail instance.");
		tearDownRyaSail();

		log.info("2. Shutting down the Mini Fluo instance.");
		tearDownFluo();

		log.info("3. Shutting down the Mini Accumulo instance.");
		tearDownMiniAccumulo();

		log.info("4. Deleting the Mini Accumulo folders.");
		tearDownMiniDataDir();
	}

	protected void tearDownRyaSail(){
		if(ryaSail != null) {
			try {
				ryaSail.shutDown();
			} catch (SailException e) {					
				log.error("Could not shut down the Rya Sail instance.", e);
			} finally {
				ryaSail = null;
			}
		}
	}

	protected void tearDownFluo(){
		if(fluo != null) {
			try {
				fluo.close();
			} catch (Exception e) {
				log.error("Could not shut down the Mini Fluo instance.", e);
			}  finally {
				fluo = null;
			}
		}
	}

	protected void tearDownMiniAccumulo(){
		if(aCluster != null) {			
			try{
				aCluster.stop();
			} catch (Exception e){
				log.error("Could not stop the Mini Accumulo instance.", e);		    	   
			} finally {
				aCluster = null;
			}
		}
	}

	protected void tearDownMiniDataDir(){
		if (miniDataDir != null){
			try{
				miniDataDir.delete();
			} catch (Exception e){
				log.error("Could not delete the Mini Accumulo folders.", e);
			} finally {
				miniDataDir = null;
			}
		}
	}

	@After
	public void resetDebugLogToFalse() {
		//!!! this is called after each test!!!
		DEBUG_LOG = false;
	}	
}
package com.research.automation.graph.ryacc.test.mini;

import static org.junit.Assert.fail;

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
import org.apache.log4j.Logger;
import org.apache.rya.indexing.pcj.fluo.app.export.rya.RyaExportParameters;
import org.apache.rya.indexing.pcj.fluo.app.observers.FilterObserver;
import org.apache.rya.indexing.pcj.fluo.app.observers.JoinObserver;
import org.apache.rya.indexing.pcj.fluo.app.observers.QueryResultObserver;
import org.apache.rya.indexing.pcj.fluo.app.observers.StatementPatternObserver;
import org.apache.rya.indexing.pcj.fluo.app.observers.TripleObserver;
import org.junit.rules.TemporaryFolder;
import org.openrdf.model.Statement;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;

import com.research.automation.graph.ryacc.AbstractRyaccBaseTest;

import io.fluo.api.client.FluoAdmin;
import io.fluo.api.client.FluoAdmin.AlreadyInitializedException;
import io.fluo.api.client.FluoAdmin.TableExistsException;
import io.fluo.api.client.FluoFactory;
import io.fluo.api.config.FluoConfiguration;
import io.fluo.api.config.ObserverConfiguration;
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
 */
public class RyaccMiniBaseTest extends AbstractRyaccBaseTest<Sail> {
	private static final Logger log = Logger.getLogger(RyaccMiniBaseTest.class);

	final protected String accumuloUsername = "root";
	final protected String accumuloPassword = "password";
	final protected String accumuloAuths = "U";	
	final protected String ryaInstanceName = "demoInstance_";	
	final protected String fluoAppName = "demoInstance_pcjUpdater";

	protected MiniAccumuloConfig aConfig = null;
	protected MiniAccumuloCluster aCluster = null;	 
	protected Connector aConn = null;

	protected RyaClient ryaClient = null;
	protected AccumuloConnectionDetails aConnDetails = null;
	protected InstallConfiguration ryaInstallConfig = null;
	protected AccumuloRdfConfiguration aRdfConf = null;

	protected String aInstanceName = null;
	protected String zooKeepers = null;
	protected TemporaryFolder miniDataDir = null;

	/**
	 * Default constructor.
	 * Assumes using fluo, not skipping adds.
	 */
	public RyaccMiniBaseTest(){
		super(true,false);
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

		log.info("7. Generate Statements.");
		this.setTesterAndGenerateStatements(new RyaccMiniTester(vf,fluo,ryaT));
		this.addStatementsForTests();//will handle `skipAdd`
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

		setRyaT(RyaSailFactory.getInstance(aRdfConf));

		vf = ryaT.getValueFactory();
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
		if(ryaT != null) {
			try {
				ryaT.shutDown();
			} catch (SailException e) {					
				log.error("Could not shut down the Rya Sail instance.", e);
			} finally {
				ryaT = null;
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

	@Override
	public void addStatementsForTestsInternal() {
		log.info("TEST STEP 1 --> ADD STATEMENTS");
		try {
			rs.addStatements(ryaT,statements.toArray(new Statement[statements.size()]));
		} catch (SailException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}	
}
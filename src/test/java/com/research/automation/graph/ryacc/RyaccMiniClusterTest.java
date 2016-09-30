package com.research.automation.graph.ryacc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Date;

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.Connector;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.base.Optional;

import mvm.rya.accumulo.MiniAccumuloClusterInstance;
import mvm.rya.accumulo.instance.AccumuloRyaInstanceDetailsRepository;
import mvm.rya.api.instance.RyaDetails;
import mvm.rya.api.instance.RyaDetails.EntityCentricIndexDetails;
import mvm.rya.api.instance.RyaDetails.FreeTextIndexDetails;
import mvm.rya.api.instance.RyaDetails.GeoIndexDetails;
import mvm.rya.api.instance.RyaDetails.JoinSelectivityDetails;
import mvm.rya.api.instance.RyaDetails.PCJIndexDetails;
import mvm.rya.api.instance.RyaDetails.PCJIndexDetails.FluoDetails;
import mvm.rya.api.instance.RyaDetails.PCJIndexDetails.PCJDetails;
import mvm.rya.api.instance.RyaDetails.PCJIndexDetails.PCJDetails.PCJUpdateStrategy;
import mvm.rya.api.instance.RyaDetails.ProspectorDetails;
import mvm.rya.api.instance.RyaDetails.TemporalIndexDetails;
import mvm.rya.api.instance.RyaDetailsRepository;

public class RyaccMiniClusterTest {

	private static final Logger log = Logger.getLogger(RyaccMiniClusterTest.class);

	protected static MiniAccumuloClusterInstance ryaccMini = null;


	@BeforeClass
	public static void setUp() 
			throws IOException, InterruptedException, AccumuloException, AccumuloSecurityException{

		ryaccMini = new MiniAccumuloClusterInstance();
		ryaccMini.startMiniAccumulo();

	}

	@AfterClass
	public static void tearDown()
			throws IOException, InterruptedException{

		ryaccMini.stopMiniAccumulo();
	}

	@Test
	public void ryaccMiniTest(){

		try{
			final String instanceName = "testInstance";

			// Create the metadata object the repository will be initialized with.
			final RyaDetails details = RyaDetails.builder()
					.setRyaInstanceName(instanceName)
					.setRyaVersion("1.2.3.4")
					.setEntityCentricIndexDetails( new EntityCentricIndexDetails(true) )
					.setGeoIndexDetails( new GeoIndexDetails(true) )
					.setTemporalIndexDetails( new TemporalIndexDetails(true) )
					.setFreeTextDetails( new FreeTextIndexDetails(true) )
					.setPCJIndexDetails(
							PCJIndexDetails.builder()
							.setEnabled(true)
							.setFluoDetails( new FluoDetails("test_instance_rya_pcj_updater") )
							.addPCJDetails(
									PCJDetails.builder()
									.setId("pcj 1")
									.setUpdateStrategy(PCJUpdateStrategy.BATCH)
									.setLastUpdateTime( new Date() ))
							.addPCJDetails(
									PCJDetails.builder()
									.setId("pcj 2")))
					.setProspectorDetails( new ProspectorDetails(Optional.of(new Date())) )
					.setJoinSelectivityDetails( new JoinSelectivityDetails(Optional.of(new Date())) )
					.build();

			// Setup the repository that will be tested using a mini instance of Accumulo.
			final Connector connector = ryaccMini.getConnector();
			final RyaDetailsRepository repo = new AccumuloRyaInstanceDetailsRepository(connector, instanceName);

			// Initialize the repository
			repo.initialize(details);

			// Fetch the stored details.
			final RyaDetails stored = repo.getRyaInstanceDetails();

			// Ensure the fetched object is equivalent to what was stored.
			assertEquals(details, stored);

		} catch(Exception e){
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}

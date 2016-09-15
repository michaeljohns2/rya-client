package com.research.automation.graph.ryacc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.json.JSONException;
import org.junit.Test;

public class RyaccConfigTest implements RyaccConstants{
	
	@Test
	public void testBuilder(){
		try {
			RyaccConfig config = RyaccConfig.init().addHost(defaultHost).addPort(defaultPort);
			assertEquals(String.format("host %s.",defaultHost),defaultHost,config.getHostOrNull());
			assertEquals(String.format("port %s.",defaultPort),defaultPort,config.getPortOrNull());
			
			config = RyaccConfig.init().addHost(null).addPort(null);
			assertNull("host (null) should be null",config.getHostOrNull());
			assertNull("port (null) should be null",config.getPortOrNull());
			
			config = RyaccConfig.init().addHost("").addPort("");
			assertNull("host (empty) should be null",config.getHostOrNull());
			assertNull("port (empty) should be null",config.getPortOrNull());
			
			config = RyaccConfig.init().addHost(" ").addPort(" ");
			assertNull("host (trim to empty) should be null",config.getHostOrNull());
			assertNull("port (trim to empty) should be null",config.getPortOrNull());
			
		} catch (JSONException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}

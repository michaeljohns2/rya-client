package com.research.automation.graph.ryacc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * RyaccConfig wraps a JSONObject to populate config. 
 * Supports builder pattern for construction.
 * 
 * @author mjohns
 *
 */
public class RyaccConfig implements RyaccConstants{

	public static RyaccConfig init() {
		return new RyaccConfig();
	}
	
	private JSONObject config;
	
	public RyaccConfig() {
		setConfig(new JSONObject());
	}
	
	public RyaccConfig put(String k, Object v) throws JSONException {
		config.put(k,v);
		return this;
	}
	
	public RyaccConfig addHost(String v) throws JSONException {
		config.put(HOST_KEY,v);
		return this;
	}
	
	public RyaccConfig addPort(String v) throws JSONException {
		config.put(PORT_KEY,v);
		return this;
	}
	
	public String getHostOrNull() {
		try {
			return RyaccUtils.manipulate(config.getString(HOST_KEY),ManOp.EMPTY_TO_NULL);
		} catch (JSONException e) {
			//ignore
		}		
		return null;
	}
	
	public String getPortOrNull() {
		try {
			return RyaccUtils.manipulate(config.getString(PORT_KEY),ManOp.EMPTY_TO_NULL);
		} catch (JSONException e) {
			//ignore
		}		
		return null;
	}
	
	/**
	 * Support persisting the config to file.
	 * @param filePath
	 * @throws IOException
	 */
	public void toFile(String filePath) throws IOException {		
		FileUtils.write(new File(filePath), config.toString());
	}
	
	/**
	 * Support reading and existing config from file.
	 * 
	 * @param filePath String 
	 * @return RyaccConfig
	 * 
	 * @throws IOException
	 * @throws JSONException
	 */
	public RyaccConfig fromFile(String filePath) throws IOException, JSONException {
	    InputStream is = FileUtils.openInputStream(new File(filePath));
	    String jsonTxt = IOUtils.toString(is);	    
	    return setConfig(new JSONObject(jsonTxt));
	}
	
	public JSONObject getConfig() {
		return config;
	}

	public RyaccConfig setConfig(JSONObject config) {
		this.config = config;
		return this;
	}
}

package com.research.automation.graph.ryacc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.InvalidParameterException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.openrdf.rio.RDFFormat;

/**
 * Add Data into Ryacc from a variety of formats.
 * Formats from {@link org.openrdf.rio.RDFFormat}.
 * 
 * @author mjohns
 *
 */
public class RyaccWrite implements RyaccConstants{
		
	/**
	 * Add data from a file.
	 * 
	 * @param config RyaccConfig
	 * @param file File
	 * @param rdfFormat RDFFormat
	 * @return String response from server
	 * 
	 * @throws IOException 
	 */
	public static String from(RyaccConfig config, File file, RDFFormat rdfFormat) throws IOException {
		return from(config, FileUtils.openInputStream(file),rdfFormat);
	}
	
	/**
	 * Add data from a URL.
	 * 
	 * @param config RyaccConfig
	 * @param url URL
	 * @param rdfFormat RDFFormat
	 * @return String response from server
	 * 
	 * @throws IOException 
	 */
	public static String from(RyaccConfig config, URL url, RDFFormat rdfFormat) throws IOException{
		return from(config, url.openStream(),rdfFormat);
	}
	
	/**
	 * Add data from an input stream. All [addData] 'from' methods funnel into this one.
	 * 
	 * @param config RyaccConfig
	 * @param in InputStream
	 * @param rdfFormat RDFFormat
	 * @return String response from server
	 * 
	 * @throws IOException 
	 */
	public static String from(RyaccConfig config, InputStream in, RDFFormat rdfFormat) throws IOException{
		URL url = buildLoadUrl(config, rdfFormat);
		
		/* NOTE: WANT TO USE SAME CONNECTION HERE */
		URLConnection uConn = url.openConnection(); 
		
		//write the data to the connection
		writeTo(uConn,in,false);
		
		//read back the response from the server
		InputStream rIn = uConn.getInputStream();
		try {
			return IOUtils.toString(rIn);
        } finally {
        	IOUtils.closeQuietly(in);
        	IOUtils.closeQuietly(rIn);
        }
	}
	
	/**
	 * Add data from a resource using the classloader.
	 * 
	 * @param resource String name of resource
	 * @param rdfFormat RDFFormat
	 * @return String response from server
	 * 
	 * @throws IOException 
	 */
	public static String fromResource(RyaccConfig config, String resource, RDFFormat rdfFormat) throws IOException{
		return from(config, Thread.currentThread().getContextClassLoader().getResourceAsStream(resource),rdfFormat);
	}
	
	/**
	 * Build the url for posting data.
	 * 
	 * @param config RyaccConfig 
	 * @param rdfFormat RDFFormat
	 * @return URL
	 * 
	 * @throws MalformedURLException
	 * @throws InvalidParameterException
	 */
	private static URL buildLoadUrl(RyaccConfig config, RDFFormat rdfFormat) throws MalformedURLException, InvalidParameterException{
		
		String host = RyaccUtils.paramMustNotStandardizeToNull(config.getHostOrNull(),HOST_KEY);
		String port = RyaccUtils.paramMustNotStandardizeToNull(config.getPortOrNull(),PORT_KEY);
		
		if (rdfFormat == null)
			throw new InvalidParameterException("RDFFormat cannot be null.");
		
		String url = URL_LOAD_TEMPLATE.replace(HOST_TOKEN, host).replace(PORT_TOKEN, port).replace(RDF_FORMAT_TOKEN, rdfFormat.getName());
		return new URL(url);
	}
	
	/**
	 * Write input stream to provided url connection.
	 * Assumes a text/plain Content-Type.
	 * 
	 * @param uConn URLConnection
	 * @param payload InputStream
	 * @param closeIn boolean	 
	 * 
	 * @throws IOException
	 */
	private static void writeTo(URLConnection uConn, InputStream payload, boolean closeIn) throws IOException{

		uConn.setRequestProperty("Content-Type", "text/plain");
		uConn.setDoOutput(true);

		try(OutputStream os = uConn.getOutputStream()){

			int read;
			while((read = payload.read()) >= 0) {
				os.write(read);
			}         
			os.flush();//just in case
		} finally {
			if (closeIn) IOUtils.closeQuietly(payload);
		}
	}
	
    public static void main(String[] args) throws IOException, JSONException {
    	String resource = "example_ntriple.txt";    
    	RyaccConfig config = RyaccConfig.init().addHost(defaultHost).addPort(defaultPort);    
    	
    	//verify resource can be found (optional)
//    	InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);  
//    	try{
//    	String data = IOUtils.toString(in);
//    	System.out.println(data);
//    	} finally {
//    		IOUtils.closeQuietly(in);
//    	}
    	
    	//load (comment out after)
    	String response = fromResource(config,resource, RDFFormat.NTRIPLES);
    	System.out.println(response);
    	
    	//verify it loaded
    	String query = "select * where {\n" +
        "<http://mynamespace/ProductType1> ?p ?o.\n" +
        "}";
    	
    		
        JSONObject jsonObj =  RyaccRead.query(config, query);
        System.out.println(jsonObj.toString(4));//effectively pretty print
    }
}

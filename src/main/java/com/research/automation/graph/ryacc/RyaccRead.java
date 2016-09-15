package com.research.automation.graph.ryacc;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Query client to Rya via Sparql
 * @author mjohns
 *
 */
public class RyaccRead implements RyaccConstants{

	/**
	 * Query using the provided config.
	 * 
	 * @param config RyaccConfig
	 * @param query String
	 * @return JSONObject
	 * 
	 * @throws JSONException
	 * @throws IOException
	 */
	public static JSONObject query(RyaccConfig config, String query) 
			throws JSONException, IOException {

		String host = config.getHostOrNull();
		String port = config.getPortOrNull();

		String baseUrl = buildBaseUrlStr(host,port);

		URL url = buildSparqlUrl(baseUrl,query,true);
		JSONObject jsonObj = getJsonFrom(url);
		return jsonObj; 
	}

	/**
	 * Build url string from URL_TEMPLATE
	 * 
	 * @param host String
	 * @param port String
	 * @return String
	 */
	public static String buildBaseUrlStr(String host, String port){
		return URL_QUERY_TEMPLATE.replace(HOST_TOKEN, host).replace(PORT_TOKEN, port);
	}

	/**
	 * Build sparql query string from SPARQL_TEMPLATE
	 * 
	 * @param url String
	 * @param query String
	 * @param encodeQuery boolean
	 * @return URL
	 * 
	 * @throws UnsupportedEncodingException
	 * @throws MalformedURLException 
	 */
	public static URL buildSparqlUrl(String baseUrl,String query, boolean encodeQuery) 
			throws MalformedURLException, UnsupportedEncodingException {

		return new URL(SPARQL_QUERY_TEMPLATE.replace(URL_TOKEN, baseUrl).replace(
				QUERY_TOKEN, (encodeQuery? URLEncoder.encode(query, "UTF-8") : query)));
	}

	/**
	 * Attempt to get JsonObject from provided url.
	 * 
	 * @param url URL
	 * @return JSONObject
	 * 
	 * @throws IOException
	 * @throws JSONException
	 */
	public static JSONObject getJsonFrom(URL url) throws IOException, JSONException{	
		URLConnection urlConnection = url.openConnection();
		urlConnection.addRequestProperty("Accept", "application/sparql-results+json");
		urlConnection.setDoOutput(true);

		InputStream in = urlConnection.getInputStream();
		try{
			return new JSONObject(IOUtils.toString(in));
		} finally {
			IOUtils.closeQuietly(in);
		}            
	}

	/**
	 * Inline-test harness, assumes data from vagrant example available.
	 * @param args
	 * @throws IOException
	 * @throws JSONException
	 */
	public static void main(String[] args) throws IOException, JSONException {

		/* Results in the following url:
    	"http://localhost:8080/web.rya/queryrdf?query.infer=&query.auth=&query.resultformat=json&padding=&query=PREFIX+money%3A%3Chttp%3A%2F%2Ftelegraphis.net%2Fontology%2Fmoney%2Fmoney%23%3E%0D%0A%0D%0Aselect+%3Fname+where+%7B%0D%0A+++%3Fx+a+money%3ACurrency+.%0D%0A+++%3Fx+money%3AshortName+%22dollar%22+.%0D%0A+++%3Fx+money%3Aname+%3Fname+.%0D%0A%7D";
		 */

		String query = "PREFIX money:<http://telegraphis.net/ontology/money/money#>\n"+
				"select ?name where {\n"+
				"?x a money:Currency .\n"+
				"?x money:shortName \"dollar\" .\n"+
				"?x money:name ?name .\n"+
				"}";


		RyaccConfig ryaccConfig = RyaccConfig.init().addHost(defaultHost).addPort(defaultPort);    	
		JSONObject jsonObj =  query(ryaccConfig, query);
		System.out.println(jsonObj.toString(4));//effectively pretty print 
	}
}
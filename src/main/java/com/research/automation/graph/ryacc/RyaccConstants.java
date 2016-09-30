package com.research.automation.graph.ryacc;

public interface RyaccConstants {
	
	public static enum MergeRule {
		ANY_SAME, ALL_SAME
	}

	public static enum MergeConflictRule {
		SKIP_CONFLICT, ADD_TO_MERGE_DIFF_KEY, OVERWRITE
	}
	
	/**
	 * Used to direct manipulation of Strings.
	 * 
	 * @author mjohns
	 *
	 */
	public static enum ManOp{
		EMPTY_TO_NULL,NULL_TO_EMPTY,TRIM,TO_LOWERCASE,TO_UPPERCASE
	}
		
	String defaultHost = "localhost";
	String defaultPort = "8080";    
	
	String HOST_KEY = "host";
	String PORT_KEY = "port";
	
	final public static String URL_TOKEN = "${URL}";
	final public static String QUERY_TOKEN = "${QUERY}";
	final public static String HOST_TOKEN = "${HOST}";
	final public static String PORT_TOKEN = "${PORT}";	
	
	final public static String SPARQL_QUERY_ENDPOINT_TEMPLATE = String.format("%s?query.infer=&query.auth=&query.resultformat=json&padding=&query=%s",URL_TOKEN,QUERY_TOKEN);
	final public static String URL_QUERY_TEMPLATE = String.format("http://%s:%s/web.rya/queryrdf",HOST_TOKEN,PORT_TOKEN);
	
	final public static String RDF_FORMAT_TOKEN = "${RDF_FORMAT}";//from open RDFFormat.getName()
	
	final public static String URL_LOAD_TEMPLATE = String.format("http://%s:%s/web.rya/loadrdf?format=%s",HOST_TOKEN,PORT_TOKEN,RDF_FORMAT_TOKEN);
	
	

	public static final String MERGE_DIFF_KEY = "merge_diff";

	public static final String SELECT_PARAMS_TOKEN = "${SELECT_PARAMS}";
	public static final String WHERE_CLAUSE_TOKEN = "${WHERE_CLAUSE}";
	public static final String SPARQL_QUERY_TEMPLATE = 	"SELECT "+SELECT_PARAMS_TOKEN+" \n"+
			"WHERE { \n"+
			WHERE_CLAUSE_TOKEN+"\n"+
			"}";

}

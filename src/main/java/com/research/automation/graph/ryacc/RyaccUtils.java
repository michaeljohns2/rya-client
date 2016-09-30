package com.research.automation.graph.ryacc;

import java.security.InvalidParameterException;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.openrdf.query.BindingSet;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.sparql.SPARQLParser;
import org.openrdf.queryrender.sparql.SPARQLQueryRenderer;

public class RyaccUtils implements RyaccConstants {
	
	private static final Logger log = Logger.getLogger(RyaccUtils.class);
	
	/**
	 * Convert an array to a String.
	 * 
	 * @param array String[]
	 * @param delim String
	 * @return String
	 */
	public static String arrayToStr(String[] array, String delim){
		if (array == null || array.length == 0) return "";

		boolean isFirst = true;
		String r = "";
		for (String s : array){
			r += isFirst? s : delim + s;
			isFirst = false;
		}
		return r;
	}
	
	/**
	 * Convert a BindingSet to a TreeMap. 
	 * Using TreeMap to leverage its always sorted properties.
	 * 
	 * @param bindingSet BindingSet
	 * @return TreeMap<String,String>
	 */
	public static TreeMap<String,String> bindingSetToTreeMap(BindingSet bindingSet){
		TreeMap<String,String> map = new TreeMap<>();
		for (String bname : bindingSet.getBindingNames())
			map.put(bname, bindingSet.getValue(bname).stringValue());
		return map;
	}

	/**
	 * Apply ManOp(s) to string to ensure conformance of manipulation. 
	 * 
	 * @param s String
	 * @param manops ManOp vararg 
	 * @return copy of s with ManOp(s) applied.
	 */
	public static String manipulate(String s, ManOp... manops){
		
		String c = s;
		
		if (manops == null) return c;
				
		for (ManOp m : manops){
			switch(m){
			case EMPTY_TO_NULL:
				if (c == null || c.trim().isEmpty()) c = null;
				break;
			case NULL_TO_EMPTY:
				if (c == null) c = "";
				break;
			case TO_LOWERCASE:
				if (c != null) c = c.toLowerCase();
				break;
			case TRIM:
				if (c != null) c = c.trim();
				break;
			case TO_UPPERCASE:
				if (c != null) c = c.toUpperCase();
				break;
			default:
				break;
			}
		}
		
		return c;
	}
	
	/**
	 * Convenience for throwing exception on null values.
	 * 
	 * @param s String 
	 * @param msg String
	 * @return String
	 * 
	 * @throws InvalidParameterException
	 */
	public static String mustNotStandardizeToNull(String s, String msg)
			throws InvalidParameterException {
		String c = standardize(s);

		if (c == null)
			throw new InvalidParameterException(msg);

		return c;
	}
	
	/**
	 * Convenience for throwing exception on param standardization.
	 * 
	 * @param param String
	 * @param paramName vararg of Strings; paramName[0] used to further decorate details if provided. 
	 * @return String
	 * 
	 * @throws InvalidParameterException
	 */
	public static String paramMustNotStandardizeToNull(String param,String... paramName)
			throws InvalidParameterException {

		String msg = String.format("param%s cannot be empty.", (paramName.length > 0? "'"+paramName[0]+"' ": ""));
		return mustNotStandardizeToNull(param,String.format(msg, param));
	}
	
	/**
	 * Pretty format sparql query using {@link SPARQLParser#parseQuery(String, String)} and
	 * {@link SPARQLQueryRenderer#render(ParsedQuery)}.
	 * 
	 * @param sparql
	 * @return String[]
	 * 
	 * @throws Exception
	 */
	public static String[] prettyFormatSparql(final String sparql) throws Exception {
		final SPARQLParser parser = new SPARQLParser();
		final SPARQLQueryRenderer renderer = new SPARQLQueryRenderer();
		final ParsedQuery pq = parser.parseQuery(sparql, null);
		final String prettySparql = renderer.render(pq);
		return StringUtils.split(prettySparql, '\n');
	}
	
	/**
	 * Pretty format sparql query for logging.
	 * Internally calls {@link #prettyFormatSparql(String)}.
	 * 
	 * @param sparql String
	 */
	public static void prettyLogSparql(final String sparql) {
		try {
			// Pretty print.
			final String[] lines = prettyFormatSparql(sparql);
			for(final String line : lines) {
				log.info(line);
			}
		} catch (final Exception e) {
			// Pretty print failed, so ugly print instead.
			log.info(sparql);
		}
	}

	/**
	 * Standardize a string to NULL.
	 * 
	 * Uses ManOp EMPTY_TO_NULL and TRIM
	 * 
	 * @param s
	 * @return String | null
	 */
	public static String standardize(String s){
		return manipulate(s, ManOp.EMPTY_TO_NULL,ManOp.TRIM);
	}

	/**
	 * Exposing method for clarity that treeMap is required for standardized String.
	 * This is used to populate JSONObjects, often from BindingSets after queries.
	 * 
	 * @param treeMap TreeMap<String,String>
	 * @return String
	 */
	public static String toStandardizedStr(TreeMap<String,String> treeMap){
		return treeMap.toString();
	}
}

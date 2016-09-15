package com.research.automation.graph.ryacc;

import java.security.InvalidParameterException;

import com.research.automation.graph.ryacc.RyaccConstants.ManOp;

public class RyaccUtils {
	
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
			case TRIM:
				if (c != null) c = c.trim();
			}
		}
		
		return c;
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
	 * Convenience for throwing exception on null values.
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
}

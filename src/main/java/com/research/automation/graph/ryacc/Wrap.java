package com.research.automation.graph.ryacc;

public class Wrap implements RyaccConstants{
	
	/**
	 * For each in whereLogic, append all the provided clauses (will repeat n times).
	 *  
	 * @param whereLogic String[]
	 * @param appendClauses String[]
	 * @return String[]
	 */
	public static String[] forEachAppendClauses(String[] whereLogic, String[] appendClauses){
		String[] arr = new String[whereLogic.length];
		StringBuilder sb = null;
		for (int i=0; i<whereLogic.length; i++){
			sb = new StringBuilder(); 
			sb.append(whereLogic[i]);
			for (String clause : appendClauses)
				sb.append("\n").append(clause);

			arr[i] = sb.toString();
		}
		return arr;
	}

	/**
	 * Only if needed, wrap value in 'http://'.
	 * @param v String
	 * @return String
	 */
	public static String http(String v){
		if (v.startsWith("http://")) return v;
		else return "http://"+v;
	}
	
	/**
	 * Test whether a value starts with either 'http:' or 'mailto:'
	 * @param v String
	 * @return boolean
	 */
	public static boolean isSparqlUri(String v){
		return v.startsWith("http:") || v.startsWith("mailto");
	}

	/**
	 * Only if needed, wrap value in 'mailto:'.
	 * @param v String
	 * @return String
	 */
	public static String mail(String v){
		if (v.startsWith("mailto:")) return v;
		else return "mailto:"+v;
	}
	
	/**
	 * Wrap logic with 'GRAPH %graph {...}'.
	 *  
	 * @param graph String
	 * @param whereLogic String
	 * @return String
	 */
	public static String sparqlGraph(String graph, String whereLogic){
		StringBuilder sb = new StringBuilder();
		sb.append("GRAPH ").append(graph).append(" {").append("\n");
		sb.append(whereLogic).append("\n");
		sb.append("}");
		return sb.toString();
	}
	
	/**
	 * Wrap logic with 'OPTIONAL {...}'.
	 * 
	 * @param whereLogic String
	 * @return String
	 */
	public static String sparqlOptional(String whereLogic){
		StringBuilder sb = new StringBuilder();
		sb.append("OPTIONAL{ ");
		sb.append(whereLogic);
		sb.append(" }");
		return sb.toString();
	}
	
	/**
	 * Convenience method for no graphs specified, which will default to all graphs.
	 * Internally redirects to {@link #buildSparqlQueryGraphsLinked(String[], String[], String...)}
	 * @param selectParams
	 * @param whereLogic
	 * @return
	 */
	public static String sparqlQuery(String[] selectParams, String[] whereLogic){
		return sparqlQueryGraphsLinked(selectParams, whereLogic);
	}
	
	/**
	 * Compose a SPARQL query for the following graphs (or all named graphs if omitted).
	 * This will use filtering to select only matches from provided graphs.
	 * 
		SELECT ?graph ?patron ?employee 
		WHERE { 
		GRAPH ?graph { 
		OPTIONAL { ?patron <http://talksTo> ?employee. }. 
		OPTIONAL { ?employee <http://worksAt> <http://CoffeeShop>. }.
		} FILTER regex (str(?graph),"graph1|graph3"). 
		}
	 * 
	 * @param selectParams String[]
	 * @param whereLogic String[]
	 * @param graphs String vararg
	 * @return String
	 */
	public static String sparqlQueryGraphsLinked(String[] selectParams, String[] whereLogic, String... graphs){

		StringBuilder sb = new StringBuilder();	

		String[] clauses = new String[whereLogic.length];
		for (int i=0; i< whereLogic.length; i++){
			clauses[i] = Wrap.sparqlOptional(whereLogic[i])+".";
		}

		sb.append(Wrap.sparqlGraph("?graph", RyaccUtils.arrayToStr(clauses,"\n")));
		sb.append(" ").append("FILTER regex (str(?graph),").append("\"");
		boolean isFirst = true;
		for (String graph : graphs){
			if (!isFirst) sb.append("|");
			sb.append(graph);
			isFirst = false;
		}
		sb.append("\").").append("\n");

		return SPARQL_QUERY_TEMPLATE.replace(SELECT_PARAMS_TOKEN, Wrap.sparqlSelect(selectParams)).replace(WHERE_CLAUSE_TOKEN, sb.toString());
	}
	
	/**
	 * Generate a param string to be included in a sparql query.
	 * If no params provided, '*' will be used. Also, '?' are
	 * added to param names.
	 * 
	 * @param selectParams String[]
	 * @return String
	 */
	public static String sparqlSelect(String[] selectParams){
		String sp = "";
		if (selectParams == null || selectParams.length == 0){
			sp += "*";
		} else {
			boolean isFirst = true;
			for (String p : selectParams){
				sp += isFirst? "?"+p : " ?"+p;
				isFirst = false;
			}
		}
		return sp;
	}

	/**
	 * Wrap union for clauses from two graphs.
	 * 
	 * @param graph1 String
	 * @param graph2 String 
	 * @param whereLogic1 String 
	 * @param whereLogic2 String 
	 * @param open boolean 
	 * @param close boolean 
	 * @return String
	 */
	public static String sparqlUnion(String graph1, String graph2, String whereLogic1, String whereLogic2, boolean open, boolean close){

		StringBuilder sb = new StringBuilder();
		if (open) sb.append("{").append("\n");
		sb.append(sparqlGraph(graph1, whereLogic1)).append("\n");
		sb.append("} UNION {").append("\n");
		sb.append(sparqlGraph(graph2, whereLogic2)).append("\n");
		if (close) sb.append("}").append("\n");

		return sb.toString();
	}
	
	/**
	 * Wrap value in '<...>' for sparql uri query. If
	 * value is not {@link #isSparqlUri(String)}, then prepend 'http://'.
	 *   
	 * @param str String
	 * @return String
	 */
	public static String sparqlUriQuery(String str){
		StringBuilder sb = new StringBuilder();
		sb.append("<");
		if (!isSparqlUri(str))
			sb.append("http://");
		sb.append(str);
		sb.append(">");
		return sb.toString();
	}

}

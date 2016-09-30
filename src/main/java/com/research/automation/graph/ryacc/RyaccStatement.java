package com.research.automation.graph.ryacc;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.sparql.SPARQLParser;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

import info.aduna.iteration.CloseableIteration;
import io.fluo.api.mini.MiniFluo;

public class RyaccStatement  implements RyaccConstants {
	
	private static final Logger log = Logger.getLogger(RyaccStatement.class);
	
	final protected MiniFluo fluo;
	final protected ValueFactory vf;
	final protected Sail ryaSail;
	
	/**
	 * Public constructor, results in fluo being null.
	 * 
	 * @param ryaSail Sail
	 * @param vf ValueFactory
	 */
	public RyaccStatement(Sail ryaSail, ValueFactory vf){
		this(ryaSail,vf,null);
	}
	
	/**
	 * Full public contructor.
	 * 
	 * @param ryaSail Sail
	 * @param vf ValueFactory
	 * @param fluo MiniFluo | null
	 */
	public RyaccStatement(Sail ryaSail, ValueFactory vf, MiniFluo fluo){
		this.ryaSail = ryaSail;
		this.vf = vf;
		this.fluo = fluo;
	}

	
	/**
	 * Add Statements to Rya.
	 * 
	 * @param statements Statement vararg
	 * 
	 * @throws SailException
	 */
	public void addStatements(Statement... statements) throws SailException{
		SailConnection ryaConn = ryaSail.getConnection();
		try{
			log.info("");
			log.info("Loading the following statements:");
			ryaConn.begin();
			for(final Statement statement : statements) {
				log.info("    " + statement.toString());

				if (statement.getContext() != null){
					ryaConn.addStatement(statement.getSubject(), statement.getPredicate(), statement.getObject(), statement.getContext());
				}
				else 
					ryaConn.addStatement(statement.getSubject(), statement.getPredicate(), statement.getObject());
			}
			log.info("");

			if (fluo != null)
				fluo.waitForObservers();		

		} finally {
			ryaConn.commit();//MLJ: commit should be called
			ryaConn.close();
		}
	}

	/**
	 * Create a Statement.
	 * 
	 * @param s Resource
	 * @param p URI
	 * @param o Value
	 * @return Statement
	 */
	public Statement createStatement(Resource s, URI p, Value o){
		return vf.createStatement(s,p,o);
	}

	/**
	 * Create a Statement, with graph context.
	 * 
	 * @param s Resource
	 * @param p URI
	 * @param o Value
	 * @param g Resource
	 * @return Statement
	 */
	public Statement createStatement(Resource s, URI p, Value o, Resource g){
		return vf.createStatement(s,p,o,g);
	}
	
	/**
	 * Create a Statement.
	 * All *Uri params will be converted to {@link org.openrdf.model.URI} by 
	 * {@link ValueFactory}. 
	 *  
	 * @param sUri String
	 * @param pUri String
	 * @param oVal Value
	 * @return Statement
	 */
	public Statement createStatement(String sUri, String pUri, Value oVal){
		return createStatement(				
				vf.createURI(sUri), 
				vf.createURI(pUri), 
				oVal);
	}

	/**
	 * Create a Statement, with graph context.
	 * All *Uri params will be converted to {@link org.openrdf.model.URI} by 
	 * {@link ValueFactory}. 
	 *  
	 * @param sUri String
	 * @param pUri String
	 * @param oVal Value
	 * @param gUri String
	 * @return Statement
	 */
	public Statement createStatement(String sUri, String pUri, Value oVal, String gUri){
		return createStatement(				
				vf.createURI(sUri),
				vf.createURI(pUri), 
				oVal, 
				vf.createURI(gUri));
	}
    
	/**
	 * Create a Statement.
	 * All params will be converted to {@link org.openrdf.model.URI} by 
	 * {@link ValueFactory} after {@link Wrap#http(String)}.
	 * 
	 * @param s String  
	 * @param p String
	 * @param o String 
	 * @return Statement
	 */
	public Statement createUriStatement(String s, String p, String o){
		return createStatement(
				vf.createURI(Wrap.http(s)),
				vf.createURI(Wrap.http(p)),
				vf.createURI(Wrap.http(o)));
	}

	/**
	 * Create a Statement, with graph context.
	 * All params will be converted to {@link org.openrdf.model.URI} by 
	 * {@link ValueFactory} after {@link Wrap#http(String)}.
	 * 
	 * @param s String  
	 * @param p String
	 * @param o String 
	 * @param g String
	 * @return Statement
	 */
	public Statement createUriStatement(String s, String p, String o, String g){
		return createStatement(
				vf.createURI(Wrap.http(s)),
				vf.createURI(Wrap.http(p)),
				vf.createURI(Wrap.http(o)),
				vf.createURI(Wrap.http(g)));
	}

	/**
	 * Create Value.
	 * If {@link Wrap#isSparqlUri(String)} then use 
	 * {@link ValueFactory} to create a {@link org.openrdf.model.URI};
	 * otherwise, use it to create a String {@link org.openrdf.model.Literal}.
	 * 
	 * Note: String Literals are restricted in Ryacc to geospatial, e.g. WKF.
	 * 
	 * @param val String
	 * @return Value
	 */
	public Value createValue(String val){
		return Wrap.isSparqlUri(val) ? vf.createURI(val) : vf.createLiteral(val,
				vf.createURI("http://www.w3.org/2001/XMLSchema#string"));
	}
	
	/**
	 * Create Value, specifying data type URI.
	 * If {@link Wrap#isSparqlUri(String)} then use 
	 * {@link ValueFactory} to create a {@link org.openrdf.model.URI};
	 * otherwise, use it to create a {@link org.openrdf.model.Literal} with
	 * the provided data type URI.
	 * 
	 * Note: String Literals are restricted in Ryacc to geospatial, e.g. WKF.
	 * 
	 * @param val String
	 * @param dataTypeUri URI
	 * @return Value
	 */
	public Value createValue(String val, URI dataTypeUri){
		return Wrap.isSparqlUri(val) ? vf.createURI(val) : vf.createLiteral(val,dataTypeUri);
	}
	
	/**
	 * Execute Sparql Query and return the results.
	 *
	 * @param sparql String to use in query
	 * @return JSONArray
	 * 
	 * @throws SailException
	 * @throws MalformedQueryException
	 * @throws QueryEvaluationException
	 */
	public JSONArray sparqlQuery(String sparql) throws SailException, MalformedQueryException, QueryEvaluationException{

		JSONArray jRoot = new JSONArray();
		SailConnection ryaConn = ryaSail.getConnection();

		try{
			log.debug("Executing the following query: ");
			RyaccUtils.prettyLogSparql(sparql);
			log.debug("");

			final ParsedQuery parsedQuery = new SPARQLParser().parseQuery(sparql, null);

			/* 
			  <<< SailConnection #evaluate(...) >>>
			  Evaluates the supplied TupleExpr on the data contained in this Sail object, 
			  using the (optional) dataset and supplied bindings as input parameters.

			  Parameters:
				tupleExpr - The tuple expression to evaluate.
				dataset - The dataset to use for evaluating the query, null to use the Sail's default dataset.
				bindings - A set of input parameters for the query evaluation. The keys reference variable
				           names that should be bound to the value they map to.
				includeInferred - Indicates whether inferred triples are to be considered in the query result.
				                  If false, no inferred statements are returned; if true, inferred statements
				                  are returned if available.

			  Returns:
				The TupleQueryResult.
			 */

			final CloseableIteration<? extends BindingSet, QueryEvaluationException> result = ryaConn.evaluate(
					parsedQuery.getTupleExpr(), null, null, false);

			log.debug("Results:");
			while(result.hasNext()) {
				BindingSet bs = result.next();
				log.debug("    " + bs);
				jRoot.put(RyaccUtils.bindingSetToTreeMap(bs));
			}
			log.debug("");
			return jRoot;
		} finally{
			ryaConn.close();
		}
	}
	
	//##############################################################################################################
	// GETTERS
	//##############################################################################################################
	
	public MiniFluo getFluo() {
		return fluo;
	}

	public Sail getRyaSail() {
		return ryaSail;
	}

	public ValueFactory getVf() {
		return vf;
	}

}

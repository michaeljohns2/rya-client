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
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.sparql.SPARQLParser;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

import info.aduna.iteration.CloseableIteration;
import io.fluo.api.mini.MiniFluo;

public class RyaccStatement  implements RyaccConstants {

	private static final Logger log = Logger.getLogger(RyaccStatement.class);

	final protected MiniFluo fluo;//May not be used!
	final protected ValueFactory vf;	

	/**
	 * Public constructor, results in fluo being null.
	 *
	 * @param vf ValueFactory
	 */
	public RyaccStatement(ValueFactory vf){
		this(vf,null);
	}

	/**
	 * Full public contructor.
	 *
	 * @param vf ValueFactory
	 * @param fluo MiniFluo | null
	 */
	public RyaccStatement(ValueFactory vf, MiniFluo fluo){
		this.vf = vf;
		this.fluo = fluo;
	}

	/**
	 * Add Statements to Rya, using a {@link Repository} instance.
	 * 
	 * @param ryaRepo Repository
	 * @param statements Statement vararg
	 *
	 * @throws RepositoryException
	 */
	public void addStatements(Repository ryaRepo, Statement... statements) throws RepositoryException {
		RepositoryConnection ryaConn = ryaRepo.getConnection();
		try{
			log.info("");
			log.info("Loading the following statements:");
			ryaConn.begin();
			int count = 0;
			for(final Statement statement : statements) {
				count ++;
				log.info("    " + statement.toString());

				if (statement.getContext() != null){
					ryaConn.add(statement.getSubject(), statement.getPredicate(), statement.getObject(), statement.getContext());
				}
				else 
					ryaConn.add(statement.getSubject(), statement.getPredicate(), statement.getObject());

				if (count % COMMIT_EVERY_N == 0){
					log.info(String.format("...committing next insert batch at n == '%d'",count));
					ryaConn.commit();
				}
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
	 * Add Statements to Rya using a {@link Sail} instance.
	 * 
	 * @param ryaSail
	 * @param statements Statement vararg
	 * 
	 * @throws SailException
	 */
	public void addStatements(Sail ryaSail, Statement... statements) throws SailException{
		SailConnection ryaConn = ryaSail.getConnection();
		try{
			log.info("");
			log.info("Loading the following statements:");
			ryaConn.begin();
			int count = 0;
			for(final Statement statement : statements) {
				count ++;
				log.info("    " + statement.toString());

				if (statement.getContext() != null){
					ryaConn.addStatement(statement.getSubject(), statement.getPredicate(), statement.getObject(), statement.getContext());
				}
				else 
					ryaConn.addStatement(statement.getSubject(), statement.getPredicate(), statement.getObject());

				if (count % COMMIT_EVERY_N == 0){
					log.info(String.format("...committing next insert batch at n == '%d'",count));
					ryaConn.commit();
				}
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
	 * Execute Sparql Query into a {@link Repository} instance and return the results.
	 *
	 * @param sparql String to use in query
	 * @param ryaRepo Repository
	 * @return JSONArray
	 * 
	 * @throws QueryEvaluationException 
	 * @throws RepositoryException 
	 * @throws MalformedQueryException 
	 */
	public JSONArray sparqlQuery(Repository ryaRepo, String sparql) throws QueryEvaluationException, RepositoryException, MalformedQueryException {

		JSONArray jRoot = new JSONArray();

		RepositoryConnection ryaConn = ryaRepo.getConnection();
		CloseableIteration<? extends BindingSet, QueryEvaluationException> result = null;

		try{
			log.info("Executing the following query: ");
			RyaccUtils.prettyLogSparql(sparql);
			log.info("");

			TupleQuery tupleQuery = ryaConn.prepareTupleQuery(QueryLanguage.SPARQL, sparql);
			result = tupleQuery.evaluate();

			log.info("Results:");
			while(result.hasNext()) {
				BindingSet bs = result.next();
				if (bs != null){
					log.info("    " + bs);
					jRoot.put(RyaccUtils.bindingSetToTreeMap(bs));
				} else log.warn("... bindingset was null (may be norma?)");
			}
			log.info("");
			return jRoot;
		} finally{
			if (result != null) result.close();
			ryaConn.close();
		}
	}

	/**
	 * Execute Sparql Query into a {@link Sail} instance and return the results.
	 *
	 * @param sparql String to use in query
	 * @param ryaSail Sail
	 * @return JSONArray
	 * 
	 * @throws SailException
	 * @throws MalformedQueryException
	 * @throws QueryEvaluationException
	 */
	public JSONArray sparqlQuery(Sail ryaSail, String sparql) throws SailException, MalformedQueryException, QueryEvaluationException{

		JSONArray jRoot = new JSONArray();

		SailConnection ryaConn = ryaSail.getConnection();
		CloseableIteration<? extends BindingSet, QueryEvaluationException> result = null;

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

			result = ryaConn.evaluate(parsedQuery.getTupleExpr(), null, null, false);

			log.debug("Results:");
			while(result.hasNext()) {
				BindingSet bs = result.next();				
				if (bs != null){
					log.info("    " + bs);
					jRoot.put(RyaccUtils.bindingSetToTreeMap(bs));
				} else log.warn("... bindingset was null (may be norma?)");
			}
			log.debug("");
			return jRoot;

		} finally{
			if (result != null) result.close();
			ryaConn.close();
		} 
	}

	//##############################################################################################################
	// GETTERS
	//##############################################################################################################

	public MiniFluo getFluo() {
		return fluo;
	}

	public ValueFactory getVf() {
		return vf;
	}

}

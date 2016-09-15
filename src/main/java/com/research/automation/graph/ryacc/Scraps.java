package com.research.automation.graph.ryacc;
import java.util.List;

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.accumulo.core.client.security.tokens.PasswordToken;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import mvm.rya.accumulo.AccumuloRdfConfiguration;
import mvm.rya.accumulo.AccumuloRyaDAO;
import mvm.rya.prospector.service.ProspectorServiceEvalStatsDAO;
import mvm.rya.rdftriplestore.RdfCloudTripleStore;
import mvm.rya.rdftriplestore.RyaSailRepository;
import mvm.rya.rdftriplestore.inference.InferenceEngine;

public class Scraps {

	public static void main(String[] args) 
			throws RepositoryException, MalformedQueryException, TupleQueryResultHandlerException, QueryEvaluationException, AccumuloException, AccumuloSecurityException{
		
		//for docker, host:localhost will work, port:2181, accumulo instance:dev, user:root, pwd:root 
//		Connector connector = new ZooKeeperInstance("dev", "localhost").getConnector("root", new PasswordToken("root"));
//		System.out.println("...have connector");
//		
//		final RdfCloudTripleStore store = new RdfCloudTripleStore();
//		AccumuloRyaDAO crdfdao = new AccumuloRyaDAO();
//		crdfdao.setConnector(connector);
//
//		AccumuloRdfConfiguration conf = new AccumuloRdfConfiguration();
//		conf.setTablePrefix("rts_");
//		conf.setDisplayQueryPlan(true);
//		crdfdao.setConf(conf);
//		store.setRyaDAO(crdfdao);//MLJ: was rdfDao
//
//		ProspectorServiceEvalStatsDAO evalDao = new ProspectorServiceEvalStatsDAO(connector, conf);
//		evalDao.init();
//		store.setRdfEvalStatsDAO(evalDao);
//
//		InferenceEngine inferenceEngine = new InferenceEngine();
//		inferenceEngine.setRyaDAO(crdfdao);//MLJ: was rdfDao
//		inferenceEngine.setConf(conf);
//		store.setInferenceEngine(inferenceEngine);
//
//		Repository myRepository = new RyaSailRepository(store);
//		myRepository.initialize();
//
//		String query = "select * where {\n" +
//				"<http://mynamespace/ProductType1> ?p ?o.\n" +
//				"}";
//		
//		RepositoryConnection conn = myRepository.getConnection();
//		System.out.println(query);
//		
//		TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
//		ValueFactory vf = ValueFactoryImpl.getInstance();
//
//		TupleQueryResultHandler writer = new SPARQLResultsXMLWriter(System.out);
//		tupleQuery.evaluate(new TupleQueryResultHandler() {
//
//			int count = 0;
//
//			@Override
//			public void startQueryResult(List<String> strings) throws TupleQueryResultHandlerException {
//				//TODO: ANYTHING?
//				System.out.println(String.format("...starting query result for %s",strings.toString()));
//			}
//
//			@Override
//			public void endQueryResult() throws TupleQueryResultHandlerException {
//				//TODO: ANYTHING?
//				System.out.println("...ending query result");
//			}
//
//			@Override
//			public void handleSolution(BindingSet bindingSet) throws TupleQueryResultHandlerException {
//				//TODO: ANYTHING?
//				System.out.println(String.format("...handle solution (BindingSet) %s",bindingSet.toString()));
//			}
//
//			@Override
//			public void handleBoolean(boolean bool) throws QueryResultHandlerException {
//				//TODO: ANYTHING?
//				//MLJ:ADDED METHOD
//				System.out.println(String.format("...handle boolean %s",new Boolean(bool).toString()));
//				
//			}
//
//			@Override
//			public void handleLinks(List<String> links) throws QueryResultHandlerException {
//				//TODO: ANYTHING?
//				//MLJ:ADDED METHOD
//				System.out.println(String.format("...handle links (List<String>) %s",links.toString()));
//				
//			}
//		});
//
//		conn.close();
//		myRepository.shutDown();
	}

}

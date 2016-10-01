package com.research.automation.graph.ryacc;

import org.json.JSONArray;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.sail.Sail;

import io.fluo.api.mini.MiniFluo;

public class RyaccStatementer<T> extends RyaccStatement{
	
	public RyaccStatementer(ValueFactory vf) {
		super(vf);
	}
	
	public RyaccStatementer(ValueFactory vf, MiniFluo fluo){
		super(vf,fluo);
	}

	public void addStatementsT(T ryaT, Statement...statements) throws Exception {
		if (isRyaTRepositoryObj(ryaT)) this.addStatements((Repository)ryaT, statements);
		else if (isRyaTSailObj(ryaT)) this.addStatements((Sail)ryaT, statements);
		else throw new RuntimeException("... ryaT must be or extends a Repository or Sail instance.");
	}
	
	public JSONArray sparqlQueryT(T ryaT, String sparql) throws Exception {
		if (isRyaTRepositoryObj(ryaT)) return this.sparqlQuery((Repository)ryaT, sparql);
		else if (isRyaTSailObj(ryaT)) return this.sparqlQuery((Sail)ryaT, sparql);
		else throw new RuntimeException("... ryaT must be or extends a Repository or Sail instance.");
	}
	
	protected boolean isRyaTSailObj(T ryaT){
		return ryaT != null && Sail.class.isAssignableFrom(ryaT.getClass());
	}

	protected boolean isRyaTRepositoryObj(T ryaT){
		return ryaT != null && Repository.class.isAssignableFrom(ryaT.getClass());
	}
}

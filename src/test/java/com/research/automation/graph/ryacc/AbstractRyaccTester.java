package com.research.automation.graph.ryacc;

import org.openrdf.model.ValueFactory;

import io.fluo.api.mini.MiniFluo;

public class AbstractRyaccTester<T> {
	
	final protected ValueFactory vf;
	final protected MiniFluo fluo;
	final protected RyaccStatementer<T> rs;
	final protected T ryaT;
	
	
	public AbstractRyaccTester(ValueFactory vf, MiniFluo fluo, T ryaT){
		this.vf = vf;
		this.fluo = fluo;
		this.rs = new RyaccStatementer<T>(vf, fluo);
		this.ryaT = ryaT;
	}

	public RyaccStatementer<T> getRs() {
		return rs;
	}

	public T getRyaT() {
		return ryaT;
	}

	public ValueFactory getVf() {
		return vf;
	}

	public MiniFluo getFluo() {
		return fluo;
	}
}

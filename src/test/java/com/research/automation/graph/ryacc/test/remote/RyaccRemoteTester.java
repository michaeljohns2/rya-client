package com.research.automation.graph.ryacc.test.remote;

import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;

import com.research.automation.graph.ryacc.AbstractRyaccTester;

import io.fluo.api.mini.MiniFluo;

public class RyaccRemoteTester extends AbstractRyaccTester<Repository>{

	public RyaccRemoteTester(ValueFactory vf, MiniFluo fluo, Repository ryaT) {
		super(vf, fluo, ryaT);
	}

}

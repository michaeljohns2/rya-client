package com.research.automation.graph.ryacc.test.mini;

import org.openrdf.model.ValueFactory;
import org.openrdf.sail.Sail;

import com.research.automation.graph.ryacc.AbstractRyaccTester;
import com.research.automation.graph.ryacc.RyaccStatement;

import io.fluo.api.mini.MiniFluo;

public class RyaccMiniTester extends AbstractRyaccTester<Sail>{

	public RyaccMiniTester(ValueFactory vf, MiniFluo fluo, Sail ryaT) {
		super(vf, fluo, ryaT);
	}
}

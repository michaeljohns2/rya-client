package com.research.automation.graph.ryacc;

public interface RyaccTestConstants {
	
	final public static String pTalks = "talksTo";
	final public static String pWorks = "worksAt";
	final public static String coffeeShop = "CoffeeShop";
	final public static String pEmail = "hasEmail";
          
	final public static String graph0 = "graph0";
	final public static String graph1 = "graph1";
	final public static String graph2 = "graph2";
	final public static String graph3 = "graph3";

	final public static String[] selectParams = {"graph","patron","employee","email"};

	final public static String[] whereArr = {
			"?patron <"+Wrap.http(pTalks)+"> ?employee .",
			"?employee <"+Wrap.http(pWorks)+"> <"+Wrap.http(coffeeShop)+"> . "
	};

	final public static String[] whereArr2 = {
			"?employee <"+Wrap.http(pEmail)+"> ?email . ",
			"?employee <"+Wrap.http(pWorks)+"> <"+Wrap.http(coffeeShop)+"> . ",
			"?patron <"+Wrap.http(pTalks)+"> ?employee ."
	};

}

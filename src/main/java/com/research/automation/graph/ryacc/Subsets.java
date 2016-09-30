package com.research.automation.graph.ryacc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * This is a java program to determine unique subsets for items in an array
 * adapted from:
 * http://www.programcreek.com/2013/01/leetcode-subsets-ii-java/
 *  
 * @author mjohns
 *
 */
public class Subsets {
	
	private static boolean DEBUG = false;
	private static boolean TRACE = false;
	
	/**
	 * Map positive and negative subsets.
	 * @param strs String[]
	 * @param skipDups boolean
	 * @param skipKeyValueMirrors boolean
	 * @return Map<Set<String>,Set<String>>
	 */
	public static Map<List<String>,List<String>> mapPosNegSubsets(String[] strs, boolean skipDups, boolean skipKeyValueMirrors){
		Map<List<String>,List<String>> map = new HashMap<>();
		
		Set<String> pmaster = new HashSet<>();
		Set<String> nmaster = new HashSet<>();
		
		//1. get subsets
		ArrayList<ArrayList<String>> subsets = subsetsAsc(strs, skipDups);
		for (ArrayList<String> psubset : subsets){
			//2. get negated subset 
			List<String> nsubset = negateSubset(strs, psubset, skipDups);
			//3. test both masters for the positive subset to determine if it is a mirror
			if (	!skipKeyValueMirrors || (
					!pmaster.contains(psubset.toString()) &&
					!nmaster.contains(psubset.toString())
					)
				){
				map.put(psubset,nsubset);
				pmaster.add(psubset.toString());
				nmaster.add(nsubset.toString());
			}
		}
		
		printDebug("--- positive:negative subsets ---");
		printDebug(map.toString());
		
		return map;
	}
	
	/**
	 * For a given subset from provided strs, provide the negated subset.
	 * @param strs String[]
	 * @param subset List<String>
	 * @param boolean skipDups
	 * @return List<String> negation of subset
	 */
	public static List<String> negateSubset(String[] strs, List<String> subset, boolean skipDups){
		
		List<String> nlist = new ArrayList<String>();
		if (skipDups){
			for (String s : strs){
				if (!subset.contains(s) && !nlist.contains(s))
					nlist.add(s);
			}
			printTrace("...negated subset (skipDups)");
			printTrace(nlist.toString());
			return nlist;
		}
		
		//otherwise handle dups via a linked list
		LinkedList<String> sset = new LinkedList<>();
		for (String s : strs)
			sset.add(s);
			
		for (int i=0; i<subset.size(); i++){
			String s = subset.get(i);
			sset.remove(s);
		}
		Collections.sort(sset);
		printTrace("...negated subset (dups)");
		printTrace(nlist.toString());
		return sset;
	}
	
	/**
	 * Generate Subsets from the provided strs.
	 * 
	 * E.g. String[] strs = {"A","B","C"} (or {"A","B","B","C"} with skipDups) would result in the following:
	 * 
	 * [A]
	 * [A, B]
	 * [A, B, C]
	 * [A, C]
	 * [B]
	 * [B, C]
	 * [C]
	 * 
	 * @param strs String[]
	 * @param skipDups boolean if true, treat like sets; otherwise, allow duplicates
	 * @return ArrayList<ArrayList<String>>
	 */
	public static ArrayList<ArrayList<String>> subsetsAsc(String[] strs, boolean skipDups) {
		ArrayList<ArrayList<String>> sresults = new ArrayList<>();
		Set<String> master = new HashSet<>();
		
        int[] idxs = IntStream.range(0, strs.length).toArray();
		ArrayList<ArrayList<Integer>> aresults = subsetsDesc(idxs);
		printDebug("--- subset ascending (String[]) ---");
		
		//reverse loop
        for (int i = aresults.size() - 1; i>=0 ; i--){
        	ArrayList<Integer> alist = aresults.get(i);
        	ArrayList<String> slist = new ArrayList<>();
        	for (Integer a : alist){
        		String v = strs[a];
        		if (!skipDups || !slist.contains(v))
        			slist.add(v);
        	}
        	
        	//make sure slist is sorted
        	Collections.sort(slist);
        	
        	if (!skipDups || !master.contains(slist.toString())){
        		sresults.add(slist);
        		master.add(slist.toString());
        		printDebug(slist.toString());
        	}
        }
		return sresults;
	}

    /**
     * 
     * @param num
     * @return
     */
	public static ArrayList<ArrayList<Integer>> subsetsDesc(int[] num) {
		if (num == null)
			return null;

		Arrays.sort(num);

		ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Integer>> prev = new ArrayList<ArrayList<Integer>>();

		for (int i = num.length-1; i >= 0; i--) {

			//get existing sets
			if (i == num.length - 1 || num[i] != num[i + 1] || prev.size() == 0) {
				prev = new ArrayList<ArrayList<Integer>>();
				for (int j = 0; j < result.size(); j++) {
					prev.add(new ArrayList<Integer>(result.get(j)));
				}
			}

			//add current number to each element of the set
			for (ArrayList<Integer> temp : prev) {
				temp.add(0, num[i]);
			}

			//add each single number as a set, only if current element is different with previous
			if (i == num.length - 1 || num[i] != num[i + 1]) {
				ArrayList<Integer> temp = new ArrayList<Integer>();
				temp.add(num[i]);
				prev.add(temp);
			}

			//add all set created in this iteration
			for (ArrayList<Integer> temp : prev) {
				result.add(new ArrayList<Integer>(temp));
			}
		}

		//add empty set
		result.add(new ArrayList<Integer>());
		
		
		printDebug("--- subset descending (int[]) ---");
		for (ArrayList<Integer> set : result)
			printDebug(set.toString());
		
		return result;
	}
	
	private static void printDebug(String msg){
		if (DEBUG) System.out.println(msg);
	}
	
	private static void printTrace(String msg){
		if (TRACE) System.out.println(msg);
	}

	public static void main(String[] args) {
		
//		DEBUG = true;
//		TRACE = true;
		
// (1.) TEST SUBSET FROM INT[]
		int[] num = {0,1,2,3};
		ArrayList<ArrayList<Integer>> sets = subsetsDesc(num);
		
		
// (2.) TEST SUBSET FROM STRING[]
		String[] strs = {"A","B","B","C"};
		subsetsAsc(strs,false);
		
// (3.) TEST NEGATE SUBSET
	List<String> subset = new ArrayList<>();
	subset.add("A");
	subset.add("B");
	negateSubset(strs, subset, false);

// (4.) TEST MAP
mapPosNegSubsets(strs, true, true);
		

	}
}

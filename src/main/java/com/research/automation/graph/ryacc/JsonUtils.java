package com.research.automation.graph.ryacc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtils implements RyaccConstants {
	
	/**
	 * Copy a JSONArray filled with map-like JSONObjects.
	 * 
	 * @param arr JSONArray
	 * @return JSONArray
	 * 
	 * @throws JSONException 
	 */
	public static JSONArray copy(JSONArray arr) throws JSONException{
		JSONArray c = new JSONArray();
		
		for (int i=0; i < arr.length(); i++)
			c.put(copy(arr.getJSONObject(i)));
		return c;
	}
	
	/**
	 * Copy a map-like JSONObject.
	 * 
	 * @param obj JSONObject
	 * @return JSONObject
	 * 
	 * @throws JSONException
	 */
	public static JSONObject copy(JSONObject obj) throws JSONException{
		JSONObject c = new JSONObject();
		merge(obj,c,MergeConflictRule.OVERWRITE);
		return c;
	}
	
	/**
	 * Filter out any JSONObjects within results having less than the threshold.
	 * This assumes map-like JSONObjects within results.
	 * 
	 * @param results JSONArray
	 * @param requiredParamCount int
	 * @return JSONArray
	 * 
	 * @throws JSONException 
	 */
	public static JSONArray filterResultsBelowParamCount(JSONArray results, int requiredParamCount) throws JSONException {
		JSONArray jarr = new JSONArray();
		for (int i = 0; i< results.length(); i++){
			JSONObject o = results.getJSONObject(i);
			if (o.length() >= requiredParamCount){
				jarr.put(o);
			}
		}

		return jarr;
	}
	
	/**
	 * Find the first entry matching the provided key / value.
	 * This assumes map-like JSONObjects within results.
	 * 
	 * @param results JSONArray
	 * @param key String
	 * @param value String
	 * @return JSONObject
	 */
	public static JSONObject findFirstEntryWith(JSONArray results, String key,String value){
		for (int i=0; i< results.length(); i++){
			try {
				JSONObject o = results.getJSONObject(i);
				if (o.has(key)){
					if (o.getString(key).equals(value))
						return o;
				}
			} catch (JSONException ignore) {
				//ignore
			}
		}
		return null;
	}
	
	/**
	 * Merge provided key value into JSONObject, following MergeConflictRule.
	 * This assumes map-like JSONObject.
	 * 
	 * @param key String
	 * @param value String
	 * @param o JSONObject
	 * @param conflictRule MergeConflictRule (e.g. SKIP_CONFLICT or OVERWRITE or ADD_TO_MERGE_DIFF_KEY) 
	 * 
	 * @throws JSONException
	 */
	public static void keyValueMerge(String key, String value, JSONObject o, MergeConflictRule conflictRule) 
			throws JSONException{

		if (o.has(key)){
			String tVal = o.getString(key);
			switch(conflictRule){
			case SKIP_CONFLICT:
				break;
			case OVERWRITE:
				//If the values don't match, overwrite with given.
				if (!tVal.equals(value))
					o.put(key, value);
				break;
			case ADD_TO_MERGE_DIFF_KEY:
				//if the values don't match, add to MERGE_DIFF_KEY 
				if (!tVal.equals(value)){
					Map<String,String> map = new HashMap<>();
					map.put(key, value);
					JSONObject jMap = new JSONObject(map);
					if (!o.has(MERGE_DIFF_KEY)){
						o.put(MERGE_DIFF_KEY, new JSONArray());
					} 
					o.getJSONArray(MERGE_DIFF_KEY).put(jMap);
				}
				break;
			}
		} else {
			o.put(key, value);//no conflict, just add
		}
	}
	
	/**
	 * Merge source into target (target will be altered).
	 * This assumes map-like JSONObjects.
	 * 
	 * @param source JSONObject
	 * @param target JSONObject (will be altered)
	 * 
	 * @throws JSONException 
	 */
	public static void merge(JSONObject source, JSONObject target, MergeConflictRule conflictRule) throws JSONException{
		@SuppressWarnings("unchecked")
		Iterator<String> it = source.keys();
		while (it.hasNext()){
			String k = it.next();
			keyValueMerge(k,source.getString(k),target,conflictRule);
		}
	}

	/**
	 * Consolidate results into a JSONArray of unique entries based on key / value comparisons.
	 * This assumes map-like JSONObjects within results.
	 * 
	 * @param results 1+ JSONArray vararg
	 * @return JSONArray 
	 * 
	 * @throws JSONException
	 */
	public static JSONArray mergeUnique(JSONArray... results) throws JSONException {
		JSONArray arr = new JSONArray();
		if (results == null) return arr;

		Set<String> master = new HashSet<String>();

		for (JSONArray result : results){			
			for (int i=0; i<result.length(); i++){
				JSONObject jMap = result.getJSONObject(i);
				String stand = toStandarizedStr(jMap);
				if (master.contains(stand)) continue;

				master.add(stand);
				arr.put(jMap);
			}
		}
		return arr;
	}

	/**
	 * Merge into unique entries with keys matching based on MergeRule provided.
	 * This assumes map-like JSONObjects within results.
	 * 
	 * @param results JSONArray
	 * @param mergeRule MergeRule (e.g. ANY or ALL)
	 * @param mergeConflictRule MergeConflictRule (e.g. SKIP_CONFLICT or OVERWRITE or ADD_TO_MERGE_DIFF_KEY) 
	 * @param keys String vararg (1+ expected)
	 * @return JSONArray
	 * 
	 * @throws JSONException
	 */
	public static JSONArray mergeUniqueWithKeyMatching(
			JSONArray results, MergeRule mergeRule, MergeConflictRule mergeConflictRule, String... keys) throws JSONException {
		JSONArray arr = new JSONArray();		
		if (results == null || mergeRule == null) return arr;

		Set<String> master = new HashSet<String>();
		Map<String,Integer> arrLookup = new HashMap<>();
		boolean allSameRule = mergeRule.equals(MergeRule.ALL_SAME); 

		for (int i=0; i<results.length(); i++){
			boolean merge = false;
			JSONObject jMap = results.getJSONObject(i);
			String stand = toStandarizedStr(jMap);

			/* test1: uniqueness */
			if (master.contains(stand)) continue;
			master.add(stand);

			/* test2: key matches (favors first mention) */
			if (keys != null){
				JSONObject jl = null;// if set, also indicates a key
				boolean allKeys = true;//all keys?

				for (String key : keys){
					try {
						// (a) get the value if present (expect exception if key not present).
						String v = jMap.getString(key);

						// (b) find in lookup, if present (if not add).
						String lk = key+v;
						Integer lv = arrLookup.get(lk);//figure out first mention of a given key

						if (lv == null){
							arrLookup.put(lk,i);//give this entry first mention credit for the key 
						} 
						// (c) set jl if it hasn't been set (officially have a key now) 
						else if (jl == null)
							jl = results.getJSONObject(lv);//sticking with the first

					} catch (Exception ignore){
						allKeys = false;
						if (allSameRule)
							break;//no reason to keep going
					}
				}//for keys

				/* handle merge */
				// (1) make sure conditions are met for allSameRule (if applicable)
				// (2) make sure conditions are met for !allSameRule (if applicable)
				if (
						(jl != null && allKeys && allSameRule) ||
						(jl != null && !allSameRule) && testKeys(jMap, jl, mergeRule, keys)){

					merge = true;//this is a merge
					merge(jMap,jl,mergeConflictRule);//manages the merge

				}  else merge = false;//just to be clear
			}//if keys

			// add to array if not merged into another entry
			if (!merge) arr.put(jMap);

		}//for all

		return arr;	
	}

	/**
	 * Test key / value pair(s) found in o1 for matches in o2.
	 * 
	 * @param o1 JSONObject
	 * @param o2 JSONObject
	 * @param mergeRule MergeRule (e.g. ANY_SAME or ALL_SAME)
	 * @param keys String vararg to test
	 * @return boolean
	 */
	public static boolean testKeys(JSONObject o1, JSONObject o2, MergeRule mergeRule, String... keys){
		if (keys == null || keys.length == 0) return true;//no keys
		if (o1 == null || o2 == null) return false;//need both objects
		if (mergeRule == null) return false;//need a mergeRule

		boolean allSameRule = mergeRule.equals(MergeRule.ALL_SAME);

		for (String key : keys){

			try {
				String v1 = o1.getString(key);
				String v2 = o2.getString(key);
				boolean same = v1.equals(v2);

				if (same && !allSameRule) return true;
				else if (!same && allSameRule) return false;

			} catch (JSONException ignore) {
				if (allSameRule) return false;
			}
		}
		if (allSameRule) return true;//means all same was successful
		else return false; //means any same never hit
	}
	
	/**
	 * Convert a map-like JSONObject into a TreeMap (which is always sorted).
	 * 
	 * @param jMap JSONObject
	 * @return TreeMap<String,String>
	 * 
	 * @throws JSONException
	 */
	public static TreeMap<String,String> toSortedMap(JSONObject jMap) throws JSONException {
		TreeMap<String,String> map = new TreeMap<>();
		@SuppressWarnings("unchecked")
		Iterator<String> it = jMap.sortedKeys();
		while (it.hasNext()){
			String key = it.next();
			map.put(key, jMap.getString(key));
		}
		return map;
	}
	
	/**
	 * Convert a map-like JSONObject into a standardized string for comparison.
	 * 
	 * @param jMap JSONObject
	 * @return String
	 * 
	 * @throws JSONException
	 */
	public static String toStandarizedStr(JSONObject jMap) throws JSONException	{
		return RyaccUtils.toStandardizedStr(toSortedMap(jMap));		
	}

}

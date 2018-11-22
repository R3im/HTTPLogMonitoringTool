package main.java.com.httplogmonitoringtool.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class HTTPStats {

	private final HashMap<HTTPStatsType, Integer> stastValues = new HashMap<HTTPStatsType, Integer>();
	private final HashMap<String, Integer> hitSections = new HashMap<String, Integer>();
	private final static int MOST_HIT_SECTION_DISPLAYED = 3;

	public HTTPStats() {
		clear();
	}

	public void increase(HTTPStatsType type) {
		this.increase(type, 1);
	}

	public void increase(HTTPStatsType type, int value) {
		stastValues.put(type, stastValues.get(type).intValue() + value);
	}

	public void sectionHitted(String section) {
		hitSections.put(section, hitSections.containsKey(section) ? hitSections.get(section).intValue() + 1 : 1);
	}

	public void clear() {
		for (HTTPStatsType type : HTTPStatsType.values()) {
			stastValues.put(type, 0);
		}
		hitSections.clear();
	}
	
	@Override
	public String toString() {
		StringBuilder statsSb = new StringBuilder();
		statsSb.append("////////////////////\\\\\\\\\\\\\\\\\\\\");
		//sort section map by most hit
		HashMap<String, Integer> sortedMap = 
				hitSections.entrySet().stream()
			    .sorted(Entry.comparingByValue())
			    .collect(Collectors.toMap(Entry::getKey, Entry::getValue,
			                              (e1, e2) -> e1, LinkedHashMap::new));
		int maxCountSectionDeisplay = sortedMap.size()>MOST_HIT_SECTION_DISPLAYED?MOST_HIT_SECTION_DISPLAYED:sortedMap.size();
		for(int i = 0; i<maxCountSectionDeisplay; i++) {
			String[] values = (String[]) sortedMap.keySet().toArray();
			statsSb.append(values[i]);
		}
		statsSb.append("\\\\\\\\\\\\\\\\\\\\////////////////////");
		return statsSb.toString();
	}
}

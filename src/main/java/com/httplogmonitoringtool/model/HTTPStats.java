package com.httplogmonitoringtool.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class HTTPStats {

	private final HashMap<HTTPStatsType, Long> stastValues = new HashMap<HTTPStatsType, Long>();
	private final HashMap<String, Integer> hitSections = new HashMap<String, Integer>();
	private final HashMap<String, Integer> userCount = new HashMap<String, Integer>();
	private final HashMap<String, Integer> remoteHostsCount = new HashMap<String, Integer>();
	public final static int MOST_HIT_SECTION_DISPLAYED = 3;

	public HTTPStats() {
		clear();
	}

	public void increase(HTTPStatsType type) {
		this.increase(type, 1);
	}

	public void increase(HTTPStatsType type, int value) {
		stastValues.put(type, (long) (stastValues.get(type).intValue() + value));
	}

	public void addSection(String section) {
		hitSections.put(section, hitSections.containsKey(section) ? hitSections.get(section).intValue() + 1 : 1);
	}

	public void addUser(String section) {
		userCount.put(section, userCount.containsKey(section) ? userCount.get(section).intValue() + 1 : 1);
	}
	public void addRemoteHost(String section) {
		remoteHostsCount.put(section, remoteHostsCount.containsKey(section) ? remoteHostsCount.get(section).intValue() + 1 : 1);
	}

	public void clear() {
		for (HTTPStatsType type : HTTPStatsType.values()) {
			stastValues.put(type, 0l);
		}
		hitSections.clear();
	}
	
	public void clearSections() {
		hitSections.clear();
	}
	
	public void clearUsers() {
		userCount.clear();
	}
	
	public void clearRemoteHosts() {
		remoteHostsCount.clear();
	}

	public HashMap<String, Integer> getHitSection() {
		return hitSections;
	}

	public HashMap<String, Integer> getMostHitSection() {
		int maxCountSectionToDisplay = hitSections.size() > MOST_HIT_SECTION_DISPLAYED ? MOST_HIT_SECTION_DISPLAYED
				: hitSections.size();
		// sort section map by most hit
		HashMap<String, Integer> sortedMap = hitSections.entrySet().stream()
				.sorted(Collections.reverseOrder(Entry.comparingByValue())).limit(maxCountSectionToDisplay)
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

		return sortedMap;
	}

	public String getTopUser() {
		if(userCount.isEmpty()) {
			return "";
		}
		// sort user map by most counted
		HashMap<String, Integer> sortedMap = userCount.entrySet().stream()
				.sorted(Collections.reverseOrder(Entry.comparingByValue())).limit(1)
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

		return sortedMap.keySet().iterator().next();
	}
	
	public String getTopRemoteHost() {
		if(remoteHostsCount.isEmpty()) {
			return "";
		}
		// sort remoteHost map by most counted
		HashMap<String, Integer> sortedMap = remoteHostsCount.entrySet().stream()
				.sorted(Collections.reverseOrder(Entry.comparingByValue())).limit(1)
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

		return sortedMap.keySet().iterator().next();
	}

	public HashMap<HTTPStatsType, Long> getStatsValues() {
		return stastValues;
	}

	@Override
	public String toString() {
		StringBuilder statsSb = new StringBuilder();
		// sort section map by most hit
		HashMap<String, Integer> sortedMap = hitSections.entrySet().stream()
				.sorted(Collections.reverseOrder(Entry.comparingByValue()))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		int maxCountSectionDisplay = sortedMap.size() > MOST_HIT_SECTION_DISPLAYED ? MOST_HIT_SECTION_DISPLAYED
				: sortedMap.size();

		statsSb.append("Most hit section (");
		statsSb.append(maxCountSectionDisplay);
		statsSb.append("/");
		statsSb.append(hitSections.size());
		statsSb.append("):\n");
		for (int i = 0; i < MOST_HIT_SECTION_DISPLAYED; i++) {
			if (i < maxCountSectionDisplay) {
				Object[] values = sortedMap.entrySet().toArray();
				Entry<String, Integer> entry = (Entry<String, Integer>) values[i];
				statsSb.append("\"");
				statsSb.append(entry.getKey());
				statsSb.append("\":");
				statsSb.append(entry.getValue());
			}
			statsSb.append("\n");
		}
		// stats value
		for (Entry<HTTPStatsType, Long> entry : stastValues.entrySet()) {
			statsSb.append(entry.getKey().toString());
			statsSb.append(": ");
			statsSb.append(entry.getValue());
			statsSb.append("\n");
		}
		return statsSb.toString();
	}
}

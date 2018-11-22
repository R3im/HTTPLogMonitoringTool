package com.httplogmonitoringtool.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class HTTPStats {

	private final HashMap<HTTPStatsType, Integer> stastValues = new HashMap<HTTPStatsType, Integer>();
	private final HashMap<String, Integer> hitSections = new HashMap<String, Integer>();
	public final static int MOST_HIT_SECTION_DISPLAYED = 3;

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

	public HashMap<String, Integer> getHitSection() {
		return hitSections;
	}

	public HashMap<String, Integer> getMostHitSection() {

		int maxCountSectionDisplay = hitSections.size() > MOST_HIT_SECTION_DISPLAYED ? MOST_HIT_SECTION_DISPLAYED
				: hitSections.size();
		// sort section map by most hit
		HashMap<String, Integer> sortedMap = hitSections.entrySet().stream()
				.sorted(Collections.reverseOrder(Entry.comparingByValue())).limit(maxCountSectionDisplay)
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

		return sortedMap;
	}

	public HashMap<HTTPStatsType, Integer> getStatsValues() {
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
		for (Entry<HTTPStatsType, Integer> entry : stastValues.entrySet()) {
			statsSb.append(entry.getKey().toString());
			statsSb.append(": ");
			statsSb.append(entry.getValue());
			statsSb.append("\n");
		}
		return statsSb.toString();
	}
}

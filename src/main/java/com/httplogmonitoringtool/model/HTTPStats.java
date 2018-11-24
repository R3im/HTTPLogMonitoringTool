package com.httplogmonitoringtool.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class HTTPStats {

	private final HashMap<HTTPStatsType, Long> statsValues = new HashMap<HTTPStatsType, Long>();
	private final HashMap<HTTPStatsStatus, Long> statsStatus = new HashMap<HTTPStatsStatus, Long>();
	private final HashMap<String, Integer> hitSections = new HashMap<String, Integer>();
	private final HashMap<String, Integer> userCount = new HashMap<String, Integer>();
	private final HashMap<String, Integer> remoteHostsCount = new HashMap<String, Integer>();
	private int alertAverage = 0;
	public final static int MOST_HIT_SECTION_DISPLAYED = 3;

	public HTTPStats() {
		clear();
	}

	public void increase(HTTPStatsType type) {
		this.increase(type, 1);
	}

	public void increase(HTTPStatsType type, int value) {
		statsValues.put(type, (long) (statsValues.get(type).intValue() + value));
	}

	public void increase(HTTPStatsStatus status) {
		statsStatus.put(status, (long) (statsStatus.get(status).intValue() + 1));
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
			statsValues.put(type, 0l);
		}
		for (HTTPStatsStatus type : HTTPStatsStatus.values()) {
			statsStatus.put(type, 0l);
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
		return statsValues;
	}

	public HashMap<HTTPStatsStatus, Long> getStatsStatus() {
		return statsStatus;
	}

	public int getAlertAverage() {
		return alertAverage;
	}

	public void setAlertAverage(int alertAverage) {
		this.alertAverage = alertAverage;
	}

}

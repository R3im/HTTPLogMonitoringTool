package com.httplogmonitoringtool.models;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * HTTP statistics
 * 
 * @author Remi c
 *
 */
public class HTTPStats {

	/**
	 * Common HTTP statistics counters
	 */
	private final HashMap<HTTPStatsType, Long> statsValues = new HashMap<HTTPStatsType, Long>();
	
	/**
	 * All common HTTP Status code counters
	 */
	private final HashMap<HTTPStatsStatus, Long> statsStatus = new HashMap<HTTPStatsStatus, Long>();
	
	/**
	 * Counts section hits
	 */
	private final HashMap<String, Integer> hitSections = new HashMap<String, Integer>();
	
	/**
	 * Counts user requests
	 */
	private final HashMap<String, Integer> userCount = new HashMap<String, Integer>();
	
	/**
	 * Counts remote hosts requests
	 */
	private final HashMap<String, Integer> remoteHostsCount = new HashMap<String, Integer>();
	
	/**
	 * alert average value
	 */
	private int alertAverage = 0;
	
	/**
	 * Most hit display limitation
	 */
	public final static int MOST_HIT_SECTION_DISPLAYED = 3;

	/**
	 * init HTTPStats
	 */
	public HTTPStats() {
		clear();
	}

	/**
	 * increase common HTTP statistics counter for type
	 * 
	 * @param type
	 */
	public void increase(HTTPStatsType type) {
		this.increase(type, 1);
	}

	/**
	 * increase common HTTP statistics counter for type by value
	 * 
	 * @param type
	 */
	public void increase(HTTPStatsType type, int value) {
		statsValues.put(type, (long) (statsValues.get(type).intValue() + value));
	}

	/**
	 * increase common HTTP Status code counter for status
	 * 
	 * @param type
	 */
	public void increase(HTTPStatsStatus status) {
		statsStatus.put(status, (long) (statsStatus.get(status).intValue() + 1));
	}

	/**
	 * add section to section hit counter
	 * 
	 * @param section
	 */
	public void addSection(String section) {
		hitSections.put(section, hitSections.containsKey(section) ? hitSections.get(section).intValue() + 1 : 1);
	}

	/**
	 * add section to user request counter
	 * 
	 * @param section
	 */
	public void addUser(String section) {
		userCount.put(section, userCount.containsKey(section) ? userCount.get(section).intValue() + 1 : 1);
	}

	/**
	 * add section to remote host request counter
	 * 
	 * @param section
	 */
	public void addRemoteHost(String section) {
		remoteHostsCount.put(section,
				remoteHostsCount.containsKey(section) ? remoteHostsCount.get(section).intValue() + 1 : 1);
	}

	/**
	 * clear all stats
	 */
	public void clear() {
		for (HTTPStatsType type : HTTPStatsType.values()) {
			statsValues.put(type, 0l);
		}
		for (HTTPStatsStatus type : HTTPStatsStatus.values()) {
			statsStatus.put(type, 0l);
		}
		hitSections.clear();
		userCount.clear();
		remoteHostsCount.clear();
		alertAverage = 0;
	}

	/**
	 * clear sections hit count
	 */
	public void clearSections() {
		hitSections.clear();
	}


	/**
	 * clear users request count
	 */
	public void clearUsers() {
		userCount.clear();
	}

	/**
	 * clear remote hosts request count
	 */
	public void clearRemoteHosts() {
		remoteHostsCount.clear();
	}

	/**
	 * clear specific total content stats value
	 */
	public void clearTotalContent() {
		statsValues.put(HTTPStatsType.TOTAL_CONTENT, 0l);
	}

	/**
	 * {@link #hitSections}
	 * 
	 * @return hitSections
	 */
	public HashMap<String, Integer> getHitSection() {
		return hitSections;
	}

	/**
	 * get most hit sections limited to {@link #MOST_HIT_SECTION_DISPLAYED}
	 * 
	 * @return mostHitSection
	 */
	public HashMap<String, Integer> getMostHitSection() {
		int maxCountSectionToDisplay = hitSections.size() > MOST_HIT_SECTION_DISPLAYED ? MOST_HIT_SECTION_DISPLAYED
				: hitSections.size();
		// sort section map by most hit
		HashMap<String, Integer> sortedMap = hitSections.entrySet().stream()
				.sorted(Collections.reverseOrder(Entry.comparingByValue())).limit(maxCountSectionToDisplay)
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

		return sortedMap;
	}

	/**
	 * get the most present user
	 * 
	 * @return
	 */
	public String getTopUser() {
		if (userCount.isEmpty()) {
			return "";
		}
		// sort user map by most counted
		HashMap<String, Integer> sortedMap = userCount.entrySet().stream()
				.sorted(Collections.reverseOrder(Entry.comparingByValue())).limit(1)
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

		return sortedMap.keySet().iterator().next();
	}

	/**
	 * get the most present remote host
	 * 
	 * @return
	 */
	public String getTopRemoteHost() {
		if (remoteHostsCount.isEmpty()) {
			return "";
		}
		// sort remoteHost map by most counted
		HashMap<String, Integer> sortedMap = remoteHostsCount.entrySet().stream()
				.sorted(Collections.reverseOrder(Entry.comparingByValue())).limit(1)
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

		return sortedMap.keySet().iterator().next();
	}

	/**
	 * {@link #statsValues}
	 * 
	 * @return statsValues
	 */
	public HashMap<HTTPStatsType, Long> getStatsValues() {
		return statsValues;
	}

	/**
	 * {@link #statsStatus}
	 * 
	 * @return statsStatus
	 */
	public HashMap<HTTPStatsStatus, Long> getStatsStatus() {
		return statsStatus;
	}

	/**
	 * {@link #alertAverage}
	 * 
	 * @return alertAverage
	 */
	public int getAlertAverage() {
		return alertAverage;
	}

	/**
	 * {@link #alertAverage}
	 * 
	 * @param alertAverage
	 */
	public void setAlertAverage(int alertAverage) {
		this.alertAverage = alertAverage;
	}

}

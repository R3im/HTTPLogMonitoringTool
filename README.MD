HTTP Log Monitoring Tool
========================

HTTP Log Monitoring Tool is a simple program that monitors HTTP traffic.
It consume HTTP traffic logs based on the [W3C log format](https://www.w3.org/Daemon/User/Config/Logging.html#common-logfile-format)


Contents
--------

* [Installation](#installation)
* [Fake log generator](#fake-log-generator)
* [Build instructions](#build-instructions)
* [Docker](#docker)
* [Tests](#tests)
* [Improvements proposal](#improvements-proposal)

Installation
------------

1. Download jar:
   [Download](https://github.com/R3im/HTTPLogMonitoringTool/raw/master/build/libs/HTTPLogMonitoringTool-1.0.jar).
2. Install JRE 1.8+:<br>
   [Download and install JRE 1.8 (or newer)](https://www.java.com/fr/download/).
3. Execute:
   Open a command prompt a terminal, `cd` to the jar location,
   and execute `java -jar HTTPLogMonitoringTool-1.0.jar`.
   Jar options:
```
   USAGE: HTTPLogMonitoringTool [option...] [--help]
	-?, -h, --help               		Shows this help message.
	-log, -l                     		Set HTTP log file fullpath (default: "/var/log/access.log").
	-alert_threshold, -at               	Set alert threshold (>0) (default: 2 minutes).`
```

Fake log generator
------------------

If you don't have access to a HTTP traffic logger, you can use the [related project](https://github.com/R3im/HTTPTrafficFakeLog).

Build instructions
------------------

1. Install JDK 1.8+:<br>
   [Download and install JDK 1.8 (or newer)](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).
2. Install Eclipse:<br>
   [Download and install Eclipse](https://www.eclipse.org/downloads/).
3. Install Gradle<br>
   Download and install Gradle 4.10.2 (or newer).
   Follow the instructions: [Gradle install](https://gradle.org/install/)
4. Install Git:<br>
   Download [Git](http://git-scm.com/downloads) and install it.
   This step is optional.
5. Clone the HTTPLogMonitoringTool repository:<br>
   If you installed Git, execute the following command: `git clone https://github.com/R3im/HTTPLogMonitoringTool.git`<br>
   Otherwise, download the repository as a [master zip](https://github.com/R3im/HTTPLogMonitoringTool/archive/master.zip) and extract it.
6. Download dependencies:<br>
   Open a command prompt a terminal, `cd` to the project root directory,
   and execute `./gradle eclipse`.
7. Generate Jar:
   Open a command prompt a terminal, `cd` to the project root directory,
   and execute `./gradle fatjar`.

Docker
------

A Docker image for HTTP Monitor Log Tool on anapsix/alpine-java is available at [Docker Hub](https://hub.docker.com/r/r3im/http-monitoring-log/).

1. Download: `docker pull r3im/http-monitoring-log` 
2. Execute: `docker run -it r3im/http-monitoring-log:latest`

- By default it executes the [Fake log generator](https://github.com/R3im/HTTPTrafficFakeLog).
- Jar are stored to `/usr/src/HTTPTrafficFakeLog.jar` and `/usr/src/HTTPLogMonitoringTool.jar`
- Log is stored at `/var/log/access.log`

Tests
-----

Junit tests are available in the folder [src/test/java](https://github.com/R3im/HTTPLogMonitoringTool/tree/master/src/test/java/com/httplogmonitoringtool)

When [Build instructions](#build-instructions) are followed.
on Eclipse right click on `src/test/java` > `Run As` > 2 `JUnit Test`
	
Improvements proposal
---------------------

Improvement of application design and code implementation:
- Check for optimization.
- Simplify parser by using Scanner, StringTokenizer or RegularExpressions.
- Generate log file for possible bugs.
- Aim 100% Coverage JUnit tests (Current 68%).
- Use of Checkstyle, FindBugs or PMD Eclipse plugins to improve code quality.
- Generate Javadocs.
- Make console clear command working.
- Use of database.
- Simple user interface for a nicer display
	
	

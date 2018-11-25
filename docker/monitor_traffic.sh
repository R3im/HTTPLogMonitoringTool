#!/bin/bash
java -jar /usr/src/HTTPTrafficFakeLog.jar &
echo ....Starts the monitoring \(be patient\)
java -jar /usr/src/HTTPLogMonitoringTool.jar 

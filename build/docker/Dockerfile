FROM java:8
RUN touch /tmp/access.log
WORKDIR /usr/src
COPY HTTPTrafficFakeLog-1.0.jar /usr/src/HTTPTrafficFakeLog.jar
COPY HTTPLogMonitoringTool-1.0.jar /usr/src/HTTPLogMonitoringTool.jar
COPY monitor_traffic.sh  /usr/src/monitor_traffic.sh 
ENTRYPOINT ["sh","/usr/src/monitor_traffic.sh"]
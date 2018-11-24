FROM anapsix/alpine-java:8
RUN touch /var/log/access.log
WORKDIR /usr/src
COPY HTTPTrafficFakeLog-1.0.jar /usr/src/HTTPTrafficFakeLog.jar
COPY HTTPLogMonitoringTool-1.0.jar /usr/src/HTTPLogMonitoringTool.jar
COPY monitor_traffic.sh  /usr/src/monitor_traffic.sh 
ENTRYPOINT bash /usr/src/monitor_traffic.sh 
FROM tomcat:10-jdk17-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY target/ROOT.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
ENTRYPOINT ["catalina.sh","run"]

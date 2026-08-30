FROM tomcat:10-jdk21

LABEL org.opencontainers.image.title="VProfile application" \
      org.opencontainers.image.description="Java web application deployed with Apache Tomcat"

WORKDIR /usr/local/tomcat

# Remove Tomcat's sample applications so this archive is served at the root URL.
RUN rm -rf webapps/*

COPY target/vprofile-v2.war webapps/ROOT.war

EXPOSE 8080

CMD ["catalina.sh", "run"]

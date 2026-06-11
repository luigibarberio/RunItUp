# 1. Base: Usa Tomcat 9 con Java 11 (Standard per Servlet/JSP)
FROM tomcat:9.0-jdk11

# 2. Pulizia: Rimuovi le pagine di default che Tomcat installa (es. ROOT, docs)
RUN rm -rf /usr/local/tomcat/webapps/*

# 3. Deploy: Copia il file .war generato da Maven nella cartella webapps
# Nota: "target/RunItUp.war" è il percorso predefinito di Maven
COPY target/*.war /usr/local/tomcat/webapps/RunItUp.war

# 4. Networking: Espone la porta 8080 interna
EXPOSE 8080
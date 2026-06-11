# ==========================================
# STAGE 1: Compilazione (Builder)
# ==========================================
# Usiamo una base Java 11 pulita (Eclipse Temurin è lo standard moderno). 
# Il wrapper scaricherà la versione corretta di Maven
FROM eclipse-temurin:11-jdk AS builder

WORKDIR /app

# Copiamo i file essenziali per far funzionare il wrapper e il pom.xml
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Assicuriamoci che lo script mvnw abbia i permessi di esecuzione in Linux!
RUN chmod +x mvnw

# Scarichiamo le dipendenze offline per ottimizzare la cache di Docker
RUN ./mvnw dependency:go-offline

# Copiamo il codice sorgente
COPY src ./src

# Compiliamo usando il NOSTRO wrapper
RUN ./mvnw clean package -DskipTests

# ==========================================
# STAGE 2: Esecuzione (Tomcat)
# ==========================================
# 1. Base: Usa Tomcat 9 con Java 11 (Standard per Servlet/JSP)
FROM tomcat:9.0-jdk11

# 2. Pulizia: Rimuovi le pagine di default che Tomcat installa
RUN rm -rf /usr/local/tomcat/webapps/*

# 3. Deploy: Copia il file .war generato dallo STAGE 1
COPY --from=builder /app/target/*.war /usr/local/tomcat/webapps/ROOT.war

# 4. Networking: Espone la porta 8080
EXPOSE 8080
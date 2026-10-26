<!-- Logo di RunItUp -->
<p align="center">
  <img src="src/main/webapp/images/common/icons/logo-runitup.svg" 
       style="filter: invert(40%) sepia(4%) saturate(20000%) hue-rotate(190deg) brightness(90%) contrast(80%);" 
       width="200" alt="Logo RunItUp">
</p>

# RunItUp, L'E-Commerce per il Running

<p align="center">
  <img src="https://img.shields.io/badge/Java-11-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 11">
  <img src="https://img.shields.io/badge/Jakarta%20EE-Servlet%204.0-2C5E8A?style=for-the-badge&logo=eclipse&logoColor=white" alt="Servlet 4.0">
  <img src="https://img.shields.io/badge/PostgreSQL-15-316192?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL">
  <img src="https://img.shields.io/badge/Apache%20Tomcat-9.0-F8DC75?style=for-the-badge&logo=apache-tomcat&logoColor=black" alt="Apache Tomcat 9">
  <img src="https://img.shields.io/badge/Docker-Enabled-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker">
</p>

---

## 🇬🇧 Executive Summary (For International Visitors & Recruiters)

**RunItUp** is a full-stack B2C e-commerce platform specialized in running gear, built from scratch using pure Java (Servlet 4.0, JSP, JDBC) without higher-level frameworks such as Spring Boot or Hibernate.

**Key Engineering Highlights:**
- **Layered Architecture:** Strict MVC separation, DAO/DTO patterns, dedicated service layer (`CarrelloManager`), and security filters (`AuthFilter`, `AdminFilter`, `OrderOwnerFilter`).
- **ACID Transactions:** Thread-safe order placement and inventory reduction under concurrent load.
- **Faceted Search & Dynamic Catalog:** Dual-context querying algorithm providing real-time multi-attribute filtering (Brand, Size, Color, Technical Specs) without empty states.
- **Containerized Infrastructure:** Ready-to-run environment with Docker Compose, automated PostgreSQL schema initialization, and database seeding.
- **Asynchronous UX:** Vanilla ES6+ JavaScript, Fetch API, live search suggestions, and responsive design.

---

> **Corso:** Tecnologie Software per il Web  
> **Università:** Università degli Studi di Salerno  
> **Anno Accademico:** 2025/2026  
> **Corso di Laurea:** Informatica  

## Il Team (Gruppo RunTime)
* **[Luigi Barberio](https://github.com/luigibarberio/)**
* **[Antonio Tolino](https://github.com/Antonio-T-5)**

---

## Panoramica del Progetto
RunItUp è una piattaforma e-commerce B2C specializzata nella vendita di articoli per il running. Nasce dall'analisi dei leader di settore per offrire un'esperienza d'acquisto immersiva, intuitiva e tecnicamente avanzata. Il sito si rivolge sia al runner amatoriale che all'atleta agonista, mettendo a disposizione strumenti di ricerca potenziati (come l'algoritmo FindMyShoes) e un layout Light Mode dinamico e completamente responsivo.

## Stack Tecnologico
Il progetto è stato sviluppato attenendosi rigorosamente allo stack richiesto dalla traccia d'esame, sviluppando l'intera logica di business e di persistenza from scratch, senza l'ausilio di framework esterni (es. Spring o Hibernate). Non sono stati, inoltre, effettuati deploy su piattaforme cloud, ma l'applicativo è stato testato in locale tramite container Docker.

* Backend: Java 11, Servlet 4.0, JSP (JavaServer Pages)
* Frontend: HTML5, CSS3 (CSS Grid/Flexbox), Vanilla JavaScript (ES6+), Fetch API
* Database: PostgreSQL 
* Build & Dependency Management: Apache Maven
* Application Server: Apache Tomcat 9
* Data Format: JSON (tramite libreria Google Gson)
* Containerization & Deploy: Docker, Docker Compose

---

## Architettura e Design Pattern
Il codice sorgente è stato ingegnerizzato applicando rigorosi principi di Ingegneria del Software (Separation of Concerns, Domain-Driven Design):

* Pattern MVC (Model-View-Controller): Netta separazione logica. Le Servlet agiscono da puri dispatcher smistando le View JSP, senza mai stampare codice HTML o contenere logica di business complessa.
* Organizzazione per Domini: Il layer di Controllo e di View è stato suddiviso in sub-package semantici (admin, auth, catalog, checkout, user, order) per massimizzare la coesione e la manutenibilità.
* Pattern DAO & DTO: L'accesso ai dati è centralizzato nelle classi DAO. Per evitare il trasferimento di query ridondanti alle JSP, sono stati impiegati i Data Transfer Object (ProdottoDTO, VarianteDTO) che incapsulano e pre-calcolano i dati per l'interfaccia.
* Service Layer: Logiche di business trasversali (es. gestione e calcolo dei totali del carrello) sono state isolate in classi Manager dedicate (es. CarrelloManager).
* Servlet Filter (Filtri di Sicurezza): Implementazione di controlli a monte sulle richieste HTTP (AuthFilter, AdminFilter, OrderOwnerFilter) per garantire l'autenticazione, prevenire la Privilege Escalation e proteggere le rotte sensibili.
* Connection Pool: Gestione altamente ottimizzata e concorrenziale delle connessioni al database tramite l'implementazione di un DataSource all'interno della classe ConnectionPool.

---

## Funzionalità Chiave e Soluzioni Tecniche (Checklist d'Esame)

Tutti i requisiti obbligatori richiesti dalla traccia sono stati implementati e superati tramite l'aggiunta di diverse funzionalità Premium:

- Transazioni ACID (Checkout Sicuro): L'inserimento dell'ordine e il decremento delle scorte in magazzino avvengono all'interno di un'unica transazione isolata, prevenendo l'over-selling grazie a controlli concorrenziali a livello di database.
- Faceted Search (Catalogo Avanzato): Sistema di filtraggio a doppia query. Il backend calcola dinamicamente le opzioni stabili della sidebar (macro-contesto) e i risultati della griglia (micro-contesto), garantendo che l'utente possa incrociare Brand, Taglie, Colori e Specifiche Tecniche (es. Appoggio, Terreno) senza mai svuotare le opzioni di scelta.
- Comunicazioni Asincrone (AJAX/Fetch API):
  - Ricerca Intelligente: Suggerimenti in tempo reale nel catalogo, ottimizzati tramite query con doppio CONCAT per intercettare termini misti (es. Nike Pegasus o Pegasus Nike).
  - Validazione Email: Verifica asincrona della disponibilità dell'indirizzo in fase di registrazione.
  - Carrello Seamless: Aggiunta al carrello senza ricaricamento della pagina e aggiornamento dinamico del badge.
- Sicurezza Attiva: Password cifrate nel database, prevenzione delle vulnerabilità SQL Injection tramite l'utilizzo esclusivo di PreparedStatement.
- Gestione Errori Personalizzata: Intercettazione nativa tramite web.xml per mostrare interfacce User-Friendly in caso di errori 404, 403, 400 e 500.
- Scarcity Marketing: Logica JavaScript client-side che avvisa l'utente in tempo reale se la combinazione Colore/Taglia scelta è in esaurimento.
- Shoe Finder: Innovativo quiz multi-step con layout a strati sovrapposti per guidare il runner alla scarpa perfetta.
- UX Avanzata: Toggle Mostra/Nascondi Password, feedback visivi inline, focus dinamico sui form.
- Fattura PDF: Layout di dettaglio ordine formattato e ottimizzato nativamente per la stampa tramite Media Query.
- Area Amministrativa: Dashboard protetta per l'amministrazione del catalogo, l'eliminazione dei prodotti e la gestione delle scorte per singola variante.

---

## Guida all'Installazione e Setup

Il progetto è predisposto per un avvio immediato e isolato tramite Docker, semplificando drasticamente il processo di test per la valutazione.

### 1. Configurazione Iniziale
1. Clonare la repository in locale.
2. Rinominare il file .env.example in .env nella root del progetto.
3. Il file .env è già precompilato con i valori di default (Porta 8080, DB postgres, ecc.) pronti per l'uso in locale.
4. Opzionalmente, inserire un Token Cloudflare nel file .env per esporre l'applicativo al mondo esterno tramite Cloudflare Tunnel.

### 2. Metodo Consigliato: Avvio tramite Docker Compose
Questa soluzione orchestra automaticamente il database PostgreSQL e il server Tomcat, senza richiedere installazioni manuali.

1. Assicurarsi di avere Docker e Docker Compose installati sul proprio PC.
2. Aprire un terminale nella cartella root del progetto.
3. Eseguire il comando:
   ```bash
   docker-compose up --build -d
   ```
4. Attendere pochi secondi: l'infrastruttura caricherà il database, eseguirà in automatico gli script SQL presenti in /db_scripts (generazione schema e seeding dati) ed esporrà l'applicativo.
5. Il sito sarà accessibile all'indirizzo: `http://localhost:8080/RunItUp` (o alla porta definita nel file .env), oppure all'indirizzo fornito dal Cloudflare Tunnel.

### 3. Metodo per lo Sviluppo: Avvio Locale (Eclipse IDE / Tomcat)
Qualora si preferisse analizzare il codice e avviare il server direttamente dall'IDE:

1. Importare il progetto in Eclipse come Maven Project (File > Import > Existing Maven Projects).
2. Per il Database, è possibile avviare solo l'istanza PostgreSQL usando Docker (`docker-compose up -d db`) oppure utilizzare una propria installazione locale. In quest'ultimo caso, eseguire manualmente gli script in `/db_scripts` (`01-schema.sql` e `02-seed.sql`).
3. Aggiungere il progetto al server integrato Apache Tomcat (versione 9.0 o superiore).
4. Avviare il server dall'IDE.

---

## Licenza e Diritti
Copyright © 2026 Luigi Barberio, Antonio Tolino. Tutti i diritti riservati.  
Questo repository è reso pubblico a scopo di consultazione, portfolio e valutazione tecnica delle competenze degli autori. Consulta il file [LICENSE.md](LICENSE.md) per i dettagli completi sui termini d'uso.

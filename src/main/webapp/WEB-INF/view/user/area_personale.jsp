<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="it.runitup.model.bean.Utente" %>

<%
    // Recupero dell'utente sincronizzato passato dalla AreaPersonaleServlet
    Utente utente = (Utente) session.getAttribute("utenteLoggato");
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Area Personale - RunItUp</title>
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/images/common/favicon.png">
    
    <%-- Fogli di stile centralizzati e specifici --%>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/header.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/footer.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/toast.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/area_personale.css">

</head>
<body>

    <%-- Inclusione del fragment Header --%>
    <jsp:include page="/WEB-INF/view/fragments/header.jsp" />

    <main class="profile-main">
        <div class="profile-container">
            
            <%-- Benvenuto --%>
            <header class="profile-welcome-section">
                <h1>Ciao, <%= utente.getNome() %>!</h1>
                <p>Benvenuto nella tua area riservata. Qui puoi gestire le tue informazioni e monitorare i tuoi acquisti.</p>
            </header>

            <%-- Griglia delle scatole (Dashboard a macro-blocchi) --%>
            <div class="dashboard-grid">
                
                <%-- SCATOLA 1: Dati Anagrafici --%>
                <section class="dashboard-card user-info-card">
                    <div class="card-icon">👤</div>
                    <h2>I Miei Dati</h2>
                    <p class="card-description">Visualizza e modifica i dettagli del tuo account.</p>
                    
                    <div class="info-summary-list">
                        <div class="info-summary-item">
                            <span class="info-label">Nome e Cognome</span>
                            <span class="info-value"><%= utente.getNome() %> <%= utente.getCognome() %></span>
                        </div>
                        <div class="info-summary-item">
                            <span class="info-label">Indirizzo Email</span>
                            <span class="info-value"><%= utente.getEmail() %></span>
                        </div>
                        <div class="info-summary-item">
                            <span class="info-label">Telefono</span>
                            <span class="info-value"><%= (utente.getTelefono() != null && !utente.getTelefono().isEmpty()) ? utente.getTelefono() : "Non inserito" %></span>
                        </div>
                    </div>
                    
                    <div class="card-actions">
                        <a href="${pageContext.request.contextPath}/area_personale/modifica" class="btn-secondary w-100">
                            Modifica Profilo
                        </a>
                    </div>
                </section>

                <%-- SCATOLA 2: Ordini --%>
                <article class="dashboard-card link-card">
                    <div class="card-icon">📦</div>
                    <h2>I Miei Ordini</h2>
                    <p class="card-description">Consulta lo storico degli acquisti effettuati, scarica le ricevute e traccia lo stato delle spedizioni.</p>
                    <div class="card-actions mt-auto">
                        <a href="${pageContext.request.contextPath}/ordini" class="btn-primary w-100">
                            Mostra Cronologia Ordini
                        </a>
                    </div>
                </article>

                <%-- SCATOLA 3: Sicurezza e Logout --%>
                <article class="dashboard-card link-card security-card">
                    <div class="card-icon">🔒</div>
                    <h2>Sicurezza ed Account</h2>
                    <p class="card-description">Proteggi il tuo profilo modificando la password o effettua la disconnessione in totale sicurezza.</p>
                    
                    <div class="card-actions security-actions">
                        <a href="${pageContext.request.contextPath}/area_personale/modifica" class="btn-secondary w-100 mb-2">
                            Cambia Password
                        </a>
                        <%-- Bottone di logout asincrono agganciato al file JS --%>
                        <button type="button" id="btnLogout" class="btn-logout w-100">
                            Disconnetti Account
                        </button>
                    </div>
                </article>

            </div>
        </div>
    </main>

    <%-- Inclusione del fragment Footer --%>
    <jsp:include page="/WEB-INF/view/fragments/footer.jsp" />

    <%-- OVERLAY DI SUCCESS GLOBALE: Riutilizzato per la disconnessione fluida --%>
    <div id="successOverlay" class="success-overlay hidden">
        <div class="success-content">
            <div class="check-icon">✔</div>
            <h3 id="overlayTitle">Sessione Chiusa</h3>
            <p id="overlayText">Disconnessione effettuata con successo. A presto!</p>
        </div>
    </div>

    <%-- Placeholder centralizzato per i Toast --%>
    <jsp:include page="/WEB-INF/view/fragments/toast.jsp" />
    
    <%-- File JavaScript dedicati --%>
    <script src="${pageContext.request.contextPath}/js/toast.js"></script>
    <script src="${pageContext.request.contextPath}/js/area_personale.js"></script>

</body>
</html>
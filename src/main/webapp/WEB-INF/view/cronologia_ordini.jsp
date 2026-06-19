<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="it.runitup.model.bean.Ordine" %>
<%@ page import="it.runitup.model.bean.StatoOrdine" %>

<%
    // Recupero della lista degli ordini passata dalla OrdiniServlet
    @SuppressWarnings("unchecked")
    List<Ordine> cronologiaOrdini = (List<Ordine>) request.getAttribute("cronologiaOrdini");

    // Formattatore per le date (Es: 16 Giugno 2026)
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
    
    boolean haOrdini = (cronologiaOrdini != null && !cronologiaOrdini.isEmpty());
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>I Miei Ordini - RunItUp</title>
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/images/common/favicon.png">
    
    <%-- Fogli di stile condivisi e specifici --%>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/header.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/footer.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/toast.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/cronologia_ordini.css">
</head>
<body>

    <%-- Inclusione dell'header dinamico --%>
    <jsp:include page="/WEB-INF/view/fragments/header.jsp" />

    <main class="history-main">
        <div class="history-container">
            
            <%-- Titolo ed introduzione della sezione --%>
            <header class="history-header">
                <h1>I Miei Ordini</h1>
                <p>Consulta lo storico degli acquisti effettuati e monitora lo stato delle tue spedizioni.</p>
            </header>

            <%-- CASISTICA 1: L'utente NON ha ancora effettuato ordini (Empty State) --%>
            <% if (!haOrdini) { %>
                <div class="empty-history-box">
                    <div class="empty-icon">📦</div>
                    <h2>Non hai ancora effettuato ordini</h2>
                    <p>Il tuo storico è vuoto. Dai uno sguardo al nostro catalogo e trova la scarpa da running perfetta per te!</p>
                    <a href="${pageContext.request.contextPath}/catalogo" class="btn-primary">
                        Esplora il Catalogo
                    </a>
                </div>
            <% } else { %>
            
                <%-- CASISTICA 2: Elenco degli ordini presenti --%>
                <div class="orders-list-wrapper">
                    <% 
                        for (Ordine o : cronologiaOrdini) { 
                            // Assegnazione della classe del badge speculare a dettaglio_ordine
                            String badgeClass = "badge-default";
                            if (o.getStato() == StatoOrdine.IN_LAVORAZIONE) badgeClass = "badge-warning";
                            else if (o.getStato() == StatoOrdine.SPEDITO) badgeClass = "badge-info";
                            else if (o.getStato() == StatoOrdine.CONSEGNATO) badgeClass = "badge-success";
                            else if (o.getStato() == StatoOrdine.ANNULLATO) badgeClass = "badge-danger";
                    %>
                        <div class="order-history-card">
                            
                            <%-- Metadati dell'ordine (ID e Data) --%>
                            <div class="order-meta-info">
                                <span class="order-number">Ordine #<%= o.getIdOrdine() %></span>
                                <span class="order-date"><%= o.getDataOrdine().format(formatter) %></span>
                            </div>
                            
                            <%-- Corpo centrale della card con dettagli commerciali --%>
                            <div class="order-card-body">
                                <div class="body-info-group">
                                    <span class="info-label">Destinatario</span>
                                    <span class="info-value"><%= o.getNome_e_cognome_destinatario() %></span>
                                </div>
                                <div class="body-info-group">
                                    <span class="info-label">Metodo Pagamento</span>
                                    <span class="info-value"><%= o.getMetodoPagamento() %></span>
                                </div>
                                <div class="body-info-group">
                                    <span class="info-label">Importo Totale</span>
                                    <span class="info-value price-value">€ <%= String.format(java.util.Locale.ITALY, "%.2f", o.getTotalePagato()) %></span>
                                </div>
                            </div>
                            
                            <%-- Barra delle azioni (Stato ed Instradamento al dettaglio) --%>
                            <div class="order-card-footer">
                                <span class="order-status-badge <%= badgeClass %>">
                                    <%= o.getStato().name().replace("_", " ") %>
                                </span>
                                <a href="${pageContext.request.contextPath}/ordini?id=<%= o.getIdOrdine() %>" class="btn-detail-link">
                                    Visualizza Dettagli &rarr;
                                </a>
                            </div>
                            
                        </div>
                    <% } %>
                </div>
                
            <% } %>

            <%-- Link di ritorno rapido alla futura Area Personale --%>
            <div class="back-to-profile">
                <a href="${pageContext.request.contextPath}/area_personale" class="profile-link">
                    &larr; Torna all'Area Personale
                </a>
            </div>

        </div>
    </main>

    <%-- Inclusione frammenti strutturali --%>
    <jsp:include page="/WEB-INF/view/fragments/footer.jsp" />
    <jsp:include page="/WEB-INF/view/fragments/toast.jsp" />

    <script src="${pageContext.request.contextPath}/js/toast.js"></script>

</body>
</html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>
<%@ page import="it.runitup.model.bean.Ordine" %>
<%@ page import="it.runitup.model.bean.Utente" %>

<%
    // Recupero delle liste dalla Servlet
    @SuppressWarnings("unchecked")
    List<Ordine> ordiniAdmin = (List<Ordine>) request.getAttribute("ordiniAdmin");
    @SuppressWarnings("unchecked")
    List<Utente> listaClienti = (List<Utente>) request.getAttribute("listaClienti");

    // Recupero dei parametri di filtro (per mantenere i campi compilati)
    String filtroDal = (String) request.getAttribute("filtroDal");
    String filtroAl = (String) request.getAttribute("filtroAl");
    String filtroEmail = (String) request.getAttribute("filtroEmail");
    
    // Gestione Errori Backend (es. email non trovata)
    String erroreFiltro = (String) request.getAttribute("erroreFiltro");

    // Formattatori
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    NumberFormat valutaFormat = NumberFormat.getCurrencyInstance(Locale.ITALY);
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gestione Ordini - Admin RunItUp</title>
    
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/images/common/favicon.png">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/toast.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin_layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin_ordini.css">
</head>
<body>

    <div class="admin-layout-wrapper">
        
        <%-- Inclusione della Sidebar Comune --%>
        <jsp:include page="/WEB-INF/view/fragments/admin_sidebar.jsp" />

        <main class="admin-main-content">
            
            <div class="admin-page-header">
                <h1 class="admin-page-title">Gestione Ordini</h1>
                <p class="admin-page-subtitle">Visualizza, filtra e gestisci tutti gli ordini del negozio.</p>
            </div>

            <%-- COMUNICAZIONE DEGLI ERRORI AL JS --%>
            <% if (erroreFiltro != null) { %>
                <input type="hidden" id="backendErrorMsg" value="<%= erroreFiltro.replace("\"", "&quot;") %>">
            <% } %>

            <%-- BARRA DEI FILTRI --%>
            <div class="admin-filter-bar-card">
                <form action="${pageContext.request.contextPath}/admin/ordini" method="GET" class="admin-filter-form">
                    
                    <div class="filter-group">
                        <label for="dal">Da Data</label>
                        <input type="date" id="dal" name="dal" value="<%= filtroDal %>" class="filter-input">
                    </div>
                    
                    <div class="filter-group">
                        <label for="al">A Data</label>
                        <input type="date" id="al" name="al" value="<%= filtroAl %>" class="filter-input">
                    </div>

                    <div class="filter-group filter-group-large">
                        <label for="emailCliente">Cerca Cliente</label>
                        <input type="text" id="emailCliente" name="emailCliente" list="clientiList" 
                               value="<%= filtroEmail %>" class="filter-input" 
                               placeholder="Inizia a digitare il nome o l'email..." autocomplete="off">
                        
                        <datalist id="clientiList">
                            <% if(listaClienti != null) {
                                for(Utente u : listaClienti) { %>
                                    <option value="<%= u.getEmail() %>"><%= u.getNome() %> <%= u.getCognome() %></option>
                            <%  }
                               } %>
                        </datalist>
                    </div>

                    <div class="filter-actions">
                        <a href="${pageContext.request.contextPath}/admin/ordini" class="btn-reset" title="Resetta Filtri">Rimuovi Filtri</a>
                        <button type="submit" class="btn-primary btn-filter">Applica Filtri</button>
                    </div>
                </form>
            </div>

            <%-- GRIGLIA DEGLI ORDINI (Riutilizzo logica Card) --%>
            <% if (ordiniAdmin != null && !ordiniAdmin.isEmpty()) { %>
                <div class="admin-orders-grid">
                    <% for (Ordine o : ordiniAdmin) { 
                        // Colore Badge dinamico in base allo stato
                        String badgeClass = "badge-" + o.getStato().name().toLowerCase();
                    %>
                        <div class="admin-order-card">
                            
                            <div class="aoc-header">
                                <div class="aoc-id-date">
                                    <span class="aoc-id">Ordine #<%= o.getIdOrdine() %></span>
                                    <span class="aoc-date"><%= o.getDataOrdine().format(dtf) %></span>
                                </div>
                                <span class="aoc-badge <%= badgeClass %>">
                                    <%= o.getStato().name().replace("_", " ") %>
                                </span>
                            </div>

                            <div class="aoc-body">
                                <div class="aoc-client-info">
                                    <strong>👤 Cliente:</strong> 
                                    <span><%= o.getUtente().getNome() %> <%= o.getUtente().getCognome() %></span><br>
                                    <span class="aoc-email"><%= o.getUtente().getEmail() %></span>
                                </div>
                                
                                <div class="aoc-financials">
                                    <div class="aoc-row">
                                        <span>Pagamento:</span>
                                        <strong><%= o.getMetodoPagamento() %></strong>
                                    </div>
                                    <div class="aoc-row">
                                        <span>Totale Ordine:</span>
                                        <strong class="aoc-total"><%= valutaFormat.format(o.getTotalePagato()) %></strong>
                                    </div>
                                </div>
                            </div>

                            <div class="aoc-footer">
                                <%-- Questo link sfrutta la Servelt pubblica "OrdiniServlet", che grazie al nostro Filter accetterà gli Admin! --%>
                                <a href="${pageContext.request.contextPath}/ordini?id=<%= o.getIdOrdine() %>" class="btn-view-details">
                                    Dettagli Completi &rarr;
                                </a>
                                <%-- Generazione diretta PDF della fattura --%>
                                <a href="${pageContext.request.contextPath}/fattura_pdf?id=<%= o.getIdOrdine() %>" class="btn-download-pdf" target="_blank" title="Scarica PDF">
                                    📄
                                </a>
                            </div>

                        </div>
                    <% } %>
                </div>
            <% } else { %>
                <div class="empty-state-box">
                    <p>Nessun ordine trovato per i filtri selezionati.</p>
                    <a href="${pageContext.request.contextPath}/admin/ordini" class="btn-primary">Mostra tutti gli ordini</a>
                </div>
            <% } %>

        </main>
    </div>

    <jsp:include page="/WEB-INF/view/fragments/toast.jsp" />
    <script src="${pageContext.request.contextPath}/js/toast.js"></script>
    <script src="${pageContext.request.contextPath}/js/admin_ordini.js"></script>

</body>
</html>
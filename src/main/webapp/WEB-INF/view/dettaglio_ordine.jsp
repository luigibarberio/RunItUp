<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="it.runitup.model.bean.Ordine" %>
<%@ page import="it.runitup.model.bean.DettaglioOrdine" %>
<%@ page import="it.runitup.model.bean.StatoOrdine" %>

<%
    // Recupero attributi passati dalla Servlet
    Ordine ordine = (Ordine) request.getAttribute("ordine");
    @SuppressWarnings("unchecked")
    List<DettaglioOrdine> dettagliOrdini = (List<DettaglioOrdine>) request.getAttribute("dettagliOrdini");

    // Formattatore date (Es: 16 Giu 2026, 14:30)
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
    
    // Funzione helper locale per il colore del badge di stato
    String badgeClass = "badge-default";
    if (ordine.getStato() == StatoOrdine.IN_LAVORAZIONE) badgeClass = "badge-warning";
    else if (ordine.getStato() == StatoOrdine.SPEDITO) badgeClass = "badge-info";
    else if (ordine.getStato() == StatoOrdine.CONSEGNATO) badgeClass = "badge-success";
    else if (ordine.getStato() == StatoOrdine.ANNULLATO) badgeClass = "badge-danger";
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dettaglio Ordine #<%= ordine.getIdOrdine() %> - RunItUp</title>
    
    <%-- Fogli di stile --%>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/header.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/footer.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/toast.css">
    <%-- Il CSS specifico che andremo a creare --%>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/dettaglio_ordine.css">
</head>
<body>

    <jsp:include page="/WEB-INF/view/fragments/header.jsp" />

    <main class="order-detail-main">
        <div class="order-detail-container">
            
            <%-- Intestazione Ordine (Titolo + Badge + Tasto PDF) --%>
            <header class="order-header-section">
                <div class="order-title-wrapper">
                    <h1>Ordine #<%= ordine.getIdOrdine() %></h1>
                    <span class="order-status-badge <%= badgeClass %>">
                        <%= ordine.getStato().name().replace("_", " ") %>
                    </span>
                </div>
                <div class="order-actions">
                    <%-- Tasto Stampa: Porta alla JSP finta-A4 che esegue window.print() in automatico --%>
                    <a href="${pageContext.request.contextPath}/stampa_fattura?id=<%= ordine.getIdOrdine() %>" class="btn-secondary" target="_blank">
                        Stampa
                    </a>
                    
                    <%-- Tasto PDF: In futuro chiamerà la Servlet che genera il binario iText --%>
                    <a href="${pageContext.request.contextPath}/fattura_pdf?id=<%= ordine.getIdOrdine() %>" class="btn-primary" target="_blank">
                        Scarica Ricevuta PDF
                    </a>
                </div>
            </header>
            
            <p class="order-date-text">Effettuato il: <strong><%= ordine.getDataOrdine().format(formatter) %></strong></p>

            <%-- Layout Dashboard: 2 Colonne su Desktop --%>
            <div class="dashboard-layout">
                
                <%-- COLONNA SINISTRA: Lista Prodotti Acquistati --%>
                <section class="products-column">
                    <h2 class="section-title">Articoli Acquistati</h2>
                    
                    <div class="purchased-items-list">
                        <% for(DettaglioOrdine dett : dettagliOrdini) { %>
                            <div class="purchased-item-card">
                                <div class="item-info">
                                    <%-- Il link c'è solo se il prodotto non è stato fisicamente eliminato dal DB --%>
                                    <% if (dett.getVariante() != null && dett.getVariante().getProdotto() != null) { %>
                                        <a href="${pageContext.request.contextPath}/dettaglio?id=<%= dett.getVariante().getProdotto().getIdProdotto() %>" class="item-name-link">
                                            <%= dett.getNomeProdottoStorico() %>
                                        </a>
                                    <% } else { %>
                                        <span class="item-name-text"><%= dett.getNomeProdottoStorico() %> <small>(Fuori Catalogo)</small></span>
                                    <% } %>
                                    
                                    <div class="item-attributes">
                                        <span>Colore: <%= dett.getColoreStorico() %></span> | 
                                        <span>Taglia: <%= dett.getTagliaStorica() %></span>
                                    </div>
                                    <div class="item-qty">
                                        Qtà: <strong><%= dett.getQuantitaAcquistata() %></strong>
                                    </div>
                                </div>
                                <div class="item-price">
                                    <%-- Prezzo Unitario * Quantità (Mostriamo il prezzo congelato comprensivo di IVA) --%>
                                    <% java.math.BigDecimal subtotaleRiga = dett.getPrezzoUnitarioConIva().multiply(new java.math.BigDecimal(dett.getQuantitaAcquistata())); %>
                                    € <%= String.format(java.util.Locale.ITALY, "%.2f", subtotaleRiga) %>
                                </div>
                            </div>
                        <% } %>
                    </div>
                </section>

                <%-- COLONNA DESTRA: Riepilogo Economico e Dati Spedizione --%>
                <aside class="summary-column">
                    
                    <%-- Pannello Riepilogo Costi --%>
                    <div class="summary-panel mb-4">
                        <h3 class="panel-title">Riepilogo Costi</h3>
                        
                        <div class="cost-row">
                            <span>Subtotale Prodotti</span>
                            <%-- Ricalcolo al volo per mostrare il subtotale pre-sconto --%>
                            <% 
                                java.math.BigDecimal totProdotti = ordine.getTotalePagato().subtract(ordine.getCostoSpedizione());
                                if (ordine.getCoupon() != null) {
                                    totProdotti = totProdotti.add(ordine.getCoupon().getValoreSconto());
                                }
                            %>
                            <span>€ <%= String.format(java.util.Locale.ITALY, "%.2f", totProdotti) %></span>
                        </div>
                        
                        <% if (ordine.getCoupon() != null) { %>
                            <div class="cost-row coupon-applied">
                                <span>Sconto Coupon (<%= ordine.getCoupon().getCodice() %>)</span>
                                <span>-€ <%= String.format(java.util.Locale.ITALY, "%.2f", ordine.getCoupon().getValoreSconto()) %></span>
                            </div>
                        <% } %>

                        <div class="cost-row">
                            <span>Spedizione</span>
                            <span>
                                <% if (ordine.getCostoSpedizione().compareTo(java.math.BigDecimal.ZERO) == 0) { %>
                                    <strong style="color: var(--conferme);">Gratis</strong>
                                <% } else { %>
                                    € <%= String.format(java.util.Locale.ITALY, "%.2f", ordine.getCostoSpedizione()) %>
                                <% } %>
                            </span>
                        </div>

                        <div class="divider"></div>

                        <div class="cost-row total-row">
                            <span>Totale Pagato</span>
                            <span>€ <%= String.format(java.util.Locale.ITALY, "%.2f", ordine.getTotalePagato()) %></span>
                        </div>
                        <div class="tax-info">
                            di cui IVA (22%): € <%= String.format(java.util.Locale.ITALY, "%.2f", ordine.getTotaleIva()) %>
                        </div>
                    </div>

                    <%-- Pannello Info Spedizione --%>
                    <div class="summary-panel">
                        <h3 class="panel-title">Dettagli Consegna</h3>
                        
                        <div class="info-block">
                            <strong>Destinatario:</strong><br>
                            <%= ordine.getNome_e_cognome_destinatario() %>
                        </div>
                        
                        <div class="info-block">
                            <strong>Indirizzo di spedizione:</strong><br>
                            <%= ordine.getIndirizzoSpedizione() %><br>
                            <%= ordine.getCap() %> - <%= ordine.getCitta() %>
                        </div>
                        
                        <div class="info-block mb-0">
                            <strong>Metodo di Pagamento:</strong><br>
                            <%= ordine.getMetodoPagamento() %>
                        </div>
                    </div>
                </aside>

            </div>
            
            <%-- Link per tornare indietro --%>
            <div class="back-navigation">
                <a href="${pageContext.request.contextPath}/ordini" class="back-link">
                    &larr; Torna a I Miei Ordini
                </a>
            </div>

        </div>
    </main>

    <jsp:include page="/WEB-INF/view/fragments/footer.jsp" />
    <jsp:include page="/WEB-INF/view/fragments/toast.jsp" />

    <script src="${pageContext.request.contextPath}/js/toast.js"></script>

</body>
</html>
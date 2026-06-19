<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%-- Importiamo le classi Java necessarie --%>
<%@ page import="java.util.List" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="it.runitup.model.bean.VoceCarrello" %>
<%@ page import="it.runitup.model.bean.VarianteProdotto" %>
<%@ page import="it.runitup.model.bean.Prodotto" %>
<%@ page import="it.runitup.model.dto.VarianteDTO" %>
<%@ page import="it.runitup.model.dto.VoceCarrelloDTO" %>
<%@ page import="it.runitup.util.SpedizioneUtil" %>

<%
    // Ora recuperiamo una lista di DTO, non più di Bean puri
    @SuppressWarnings("unchecked")
    List<VoceCarrelloDTO> vociCarrello = (List<VoceCarrelloDTO>) request.getAttribute("vociCarrello");
    BigDecimal totaleCarrello = (BigDecimal) request.getAttribute("totaleCarrello");
    
    boolean isCarrelloVuoto = (vociCarrello == null || vociCarrello.isEmpty());
    boolean hasStockError = false;

    BigDecimal costoSpedizione = SpedizioneUtil.calcolaCostoSpedizione(isCarrelloVuoto ? null : totaleCarrello);
    BigDecimal totaleFinale = totaleCarrello.add(costoSpedizione);

%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Il tuo Carrello - RunItUp</title>
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/images/common/favicon.png">
    
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/header.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/footer.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/toast.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/carrello.css">
</head>
<body>

    <jsp:include page="/WEB-INF/view/fragments/header.jsp" />

    <main class="cart-main">
        <div class="cart-container">
            
            <div class="cart-items-panel">
                <div class="cart-title">Il tuo Carrello</div>
                
                <% if (isCarrelloVuoto) { %>
                    <div class="empty-cart-message">
                        <p>Il tuo carrello è vuoto. Torna ad esplorare i nostri prodotti!</p>
                        <a href="${pageContext.request.contextPath}/catalogo" class="link-catalogo btn-primary">Vai al Catalogo</a>
                    </div>
                <% } else { %>
                    <div id="cart-items-list">
                        <% 
                            for (VoceCarrelloDTO voceDTO : vociCarrello) { 
                                VoceCarrello voce = voceDTO.getVoceCarrello();
                                VarianteDTO varianteDTO = voceDTO.getVarianteDTO();
                                VarianteProdotto variante = varianteDTO.getVariante();
                                Prodotto prodotto = variante.getProdotto();
                                
                                int qtaMagazzino = variante.getQuantitaMagazzino();
                                boolean esaurito = (qtaMagazzino == 0);
                                if (esaurito) { hasStockError = true; }
                                
                                // Calcoli economici della singola riga
                                BigDecimal prezzoEffettivoUnitario = varianteDTO.getPrezzoEffettivo();
                                BigDecimal prezzoOriginaleUnitario = variante.getPrezzo();
                                
                                BigDecimal subtotaleEffettivo = prezzoEffettivoUnitario.multiply(new BigDecimal(voce.getQuantita()));
                                BigDecimal subtotaleOriginale = prezzoOriginaleUnitario.multiply(new BigDecimal(voce.getQuantita()));
                        %>
                            <div class="cart-item" id="item-<%= variante.getIdVariante() %>" 
                                 data-prezzo-effettivo="<%= prezzoEffettivoUnitario %>"
                                 data-prezzo-originale="<%= prezzoOriginaleUnitario %>"
                                 data-in-promo="<%= varianteDTO.isInPromozione() %>">
                                
                                <a href="<%= request.getContextPath() %>/dettaglio?id=<%= prodotto.getIdProdotto() %>" class="product-img-wrapper">
                                    <img src="${pageContext.request.contextPath}/images/prodotti?id=<%= prodotto.getIdProdotto() %>" 
                                         alt="<%= prodotto.getNome() %>" 
                                         class="product-img" 
                                         loading="lazy"
                                         onerror="this.src='${pageContext.request.contextPath}/images/placeholder.svg'">
                                </a>
                                
                                <div class="product-details">
                                    <h3>
                                        <a href="<%= request.getContextPath() %>/dettaglio?id=<%= prodotto.getIdProdotto() %>" class="cart-product-link">
                                            <%= prodotto.getNome() %>
                                        </a>
                                    </h3>
                                    <p>Brand: <strong><%= prodotto.getBrand().getNomeBrand() %></strong></p>
                                    <p>Colore: <%= variante.getColore() %> | Taglia: <%= variante.getTaglia() %></p>
                                    <button type="button" class="btn-remove" onclick="rimuoviArticolo(<%= variante.getIdVariante() %>)">Rimuovi</button>
                                </div>
                                
                                <div class="quantity-controls">
                                    <button class="btn-qty" onclick="modificaQuantita(<%= variante.getIdVariante() %>, -1)" id="btn-minus-<%= variante.getIdVariante() %>" <%= voce.getQuantita() <= 1 ? "disabled" : "" %>>-</button>
                                    <span class="qty-val" id="qty-<%= variante.getIdVariante() %>"><%= voce.getQuantita() %></span>
                                    <button class="btn-qty" onclick="modificaQuantita(<%= variante.getIdVariante() %>, 1)" id="btn-plus-<%= variante.getIdVariante() %>" <%= voce.getQuantita() >= qtaMagazzino ? "disabled" : "" %>>+</button>
                                </div>
                                
                                <div class="item-price-box">
                                    <% if (varianteDTO.isInPromozione()) { %>
                                        <div class="old-price-wrapper">
                                            € <span id="old-subtot-<%= variante.getIdVariante() %>"><%= String.format(java.util.Locale.ITALY, "%.2f", subtotaleOriginale) %></span>
                                        </div>
                                        <div class="promo-price-wrapper">
                                            € <span id="subtot-<%= variante.getIdVariante() %>"><%= String.format(java.util.Locale.ITALY, "%.2f", subtotaleEffettivo) %></span>
                                        </div>
                                    <% } else { %>
                                        <div class="standard-price-wrapper">
                                            € <span id="subtot-<%= variante.getIdVariante() %>"><%= String.format(java.util.Locale.ITALY, "%.2f", subtotaleEffettivo) %></span>
                                        </div>
                                    <% } %>
                                </div>

                                <% if (esaurito) { %>
                                    <div class="stock-alert">Attenzione: questo articolo è esaurito nel nostro magazzino. Rimuovilo per continuare.</div>
                                <% } %>
                            </div>
                        <% } %>
                    </div>
                <% } %>
            </div>

            <div class="cart-summary-panel">
                <div class="cart-title">Riepilogo Ordine</div>
                <div class="summary-row">
                    <span>Prodotti:</span>
                    <span>€ <span id="summary-subtotal"><%= String.format(java.util.Locale.ITALY, "%.2f", totaleCarrello) %></span></span>
                </div>
                <div class="summary-row">
                    <span>Spedizione:</span>
                    <span id="shipping-container">
                        <% if (isCarrelloVuoto) { %>
                            <span>€ 0,00</span>
                        <% } else if (costoSpedizione.compareTo(BigDecimal.ZERO) == 0) { %>
                            <span class="free-shipping">Gratis</span>
                        <% } else { %>
                            <span>€ <%= String.format(java.util.Locale.ITALY, "%.2f", costoSpedizione) %></span>
                        <% } %>
                    </span>
                </div>
                <div class="summary-row total-row">
                    <span>Totale:</span>
                    <span>€ <span id="cart-total-val"><%= String.format(java.util.Locale.ITALY, "%.2f", totaleFinale) %></span></span>
                </div>
                
                <button type="button" class="btn-primary btn-checkout" id="btnCheckout" 
                        onclick="window.location.href='${pageContext.request.contextPath}/checkout'"
                        <%= (isCarrelloVuoto || hasStockError) ? "disabled" : "" %>>
                    Procedi al Checkout
                </button>
            </div>
        </div>
    </main>

    <jsp:include page="/WEB-INF/view/fragments/footer.jsp" />
    <jsp:include page="/WEB-INF/view/fragments/toast.jsp" />

    <script src="${pageContext.request.contextPath}/js/toast.js"></script>
    <script src="${pageContext.request.contextPath}/js/carrello.js"></script>
</body>
</html>
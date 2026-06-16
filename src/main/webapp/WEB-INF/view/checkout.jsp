<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%-- Importiamo le classi necessarie --%>
<%@ page import="java.util.List" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="it.runitup.model.bean.Utente" %>
<%@ page import="it.runitup.model.bean.VarianteProdotto" %>
<%@ page import="it.runitup.model.bean.Prodotto" %>
<%@ page import="it.runitup.model.dto.VoceCarrelloDTO" %>

<%
    // Recuperiamo l'utente dalla sessione (sicuri al 100% che ci sia grazie all'AuthFilter)
    Utente utente = (Utente) session.getAttribute("utenteLoggato");

    // Recuperiamo i dati economici e gli articoli passati dalla CheckoutServlet
    @SuppressWarnings("unchecked")
    List<VoceCarrelloDTO> vociCheckout = (List<VoceCarrelloDTO>) request.getAttribute("vociCheckout");
    BigDecimal totaleProdotti = (BigDecimal) request.getAttribute("totaleProdotti");
    BigDecimal costoSpedizione = (BigDecimal) request.getAttribute("costoSpedizione");
    BigDecimal totaleFinale = (BigDecimal) request.getAttribute("totaleFinale");
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Checkout - RunItUp</title>
    
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/header.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/footer.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/toast.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/checkout.css">
</head>
<body>

    <%-- Inclusione Header --%>
    <jsp:include page="/WEB-INF/view/fragments/header.jsp" />

    <main class="checkout-main">
        <div class="checkout-container">
            
            <div class="checkout-form-panel">
                <h2 class="section-title">Informazioni di Consegna</h2>
                
                <%-- Form muto per AJAX (novalidate blocca i popup nativi del browser per usare i nostri errori inline) --%>
                <form id="checkoutForm" novalidate>
                    
                    <div class="form-group-row">
                        <div class="form-group">
                            <label for="nome">Nome Destinatario</label>
                            <input type="text" id="nome" name="nome" value="<%= utente.getNome() %>" placeholder="Es. Mario" required maxlength="100">
                            <span class="error-msg" id="nomeError"></span>
                        </div>
                        <div class="form-group">
                            <label for="cognome">Cognome Destinatario</label>
                            <input type="text" id="cognome" name="cognome" value="<%= utente.getCognome() %>" placeholder="Es. Rossi" required maxlength="100">
                            <span class="error-msg" id="cognomeError"></span>
                        </div>
                    </div>
                    
                    <div class="form-group">
                        <label for="email">Email di contatto</label>
                        <input type="email" id="email" value="<%= utente.getEmail() %>" readonly class="input-disabled">
                    </div>

                    <div class="form-group">
                        <label for="indirizzo">Indirizzo di Spedizione (Via, Piazza, Civico)</label>
                        <input type="text" id="indirizzo" name="indirizzo" placeholder="Es. Via Roma, 12" required maxlength="255">
                        <span class="error-msg" id="indirizzoError"></span>
                    </div>

                    <div class="form-group-row">
                        <div class="form-group city-box">
                            <label for="citta">Città</label>
                            <input type="text" id="citta" name="citta" placeholder="Es. Salerno" required maxlength="100">
                            <span class="error-msg" id="cittaError"></span>
                        </div>
                        <div class="form-group cap-box">
                            <label for="cap">CAP</label>
                            <input type="text" id="cap" name="cap" placeholder="Es. 84100" required maxlength="5">
                            <span class="error-msg" id="capError"></span>
                        </div>
                    </div>

                    <h2 class="section-title" style="margin-top: 40px;">Metodo di Pagamento</h2>
                    
                    <div class="payment-options">
                        <label class="payment-radio-card">
                            <input type="radio" name="metodoPagamento" value="CARTA" checked>
                            <span class="radio-label">Carta di Credito / Debito</span>
                        </label>
                        <label class="payment-radio-card">
                            <input type="radio" name="metodoPagamento" value="PAYPAL">
                            <span class="radio-label">PayPal</span>
                        </label>
                        <label class="payment-radio-card">
                            <input type="radio" name="metodoPagamento" value="CONTRASSEGNO">
                            <span class="radio-label">Contrassegno (Alla consegna)</span>
                        </label>
                    </div>

                    <div id="creditCardFields" class="credit-card-subform">
                        <div class="form-group">
                            <label for="numeroCarta">Numero della Carta</label>
                            <input type="text" id="numeroCarta" placeholder="0000 0000 0000 0000" maxlength="16">
                            <span class="error-msg" id="numeroCartaError"></span>
                        </div>
                        <div class="form-group-row">
                            <div class="form-group">
                                <label for="scadenzaCarta">Scadenza (MM/AA)</label>
                                <input type="text" id="scadenzaCarta" placeholder="MM/AA" maxlength="5">
                                <span class="error-msg" id="scadenzaCartaError"></span>
                            </div>
                            <div class="form-group">
                                <label for="cvvCarta">CVV</label>
                                <input type="password" id="cvvCarta" placeholder="123" maxlength="4">
                                <span class="error-msg" id="cvvCartaError"></span>
                            </div>
                        </div>
                    </div>

                    <div class="coupon-section">
                        <label for="codiceCoupon">Hai un codice promozionale?</label>
                        <div class="coupon-input-wrapper">
                            <input type="text" id="codiceCoupon" name="codiceCoupon" placeholder="Inserisci codice">
                            <button type="button" id="btnApplicaCoupon" class="btn-secondary">Applica</button>
                        </div>
                        <span class="coupon-message" id="couponMessage"></span>
                    </div>

                    <button type="submit" class="btn-primary btn-submit-order" id="btnInviaOrdine">
                        Conferma e Paga (€ <%= String.format(java.util.Locale.ITALY, "%.2f", totaleFinale) %>)
                    </button>
                </form>
            </div>

            <div class="checkout-summary-panel">
                <h3 class="summary-title">Riepilogo Articoli</h3>
                <div class="checkout-items-scroll">
                    <% 
                        for (VoceCarrelloDTO voceDTO : vociCheckout) {
                            Prodotto p = voceDTO.getVarianteDTO().getVariante().getProdotto();
                            BigDecimal prezzoUnitario = voceDTO.getVarianteDTO().getPrezzoEffettivo();
                            int qta = voceDTO.getVoceCarrello().getQuantita();
                    %>
                        <div class="checkout-item-row">
                            <div class="mini-img-wrapper">
                                <% if (p.getImmagineBase64() != null) { %>
                                    <img src="data:image/jpeg;base64,<%= p.getImmagineBase64() %>" alt="<%= p.getNome() %>">
                                <% } else { %>
                                    <img src="${pageContext.request.contextPath}/images/placeholder.png" alt="Placeholder">
                                <% } %>
                            </div>
                            <div class="mini-item-details">
                                <h4><%= p.getNome() %></h4>
                                <p>Qtà: <%= qta %> | Tg: <%= voceDTO.getVarianteDTO().getVariante().getTaglia() %></p>
                            </div>
                            <div class="mini-item-price">
                                € <%= String.format(java.util.Locale.ITALY, "%.2f", prezzoUnitario.multiply(new BigDecimal(qta))) %>
                            </div>
                        </div>
                    <% } %>
                </div>

                <div class="checkout-cost-breakdown">
                    <div class="cost-row">
                        <span>Totale articoli:</span>
                        <span>€ <%= String.format(java.util.Locale.ITALY, "%.2f", totaleProdotti) %></span>
                    </div>
                    <div class="cost-row">
                        <span>Spedizione:</span>
                        <% 
                            boolean isFreeShipping = costoSpedizione.compareTo(BigDecimal.ZERO) == 0;
                            String cssClass = isFreeShipping ? "free-shipping" : "";
                        %>
                        <span id="shipping-cost-display" class="<%= cssClass %>">
                            <% if (isFreeShipping) { %>
                                Gratis
                            <% } else { %>
                                € <%= String.format(java.util.Locale.ITALY, "%.2f", costoSpedizione) %>
                            <% } %>
                        </span>
                    </div>
                    <div class="cost-row coupon-row hidden" id="summary-coupon-row">
                        <span>Sconto Coupon (<span id="summary-coupon-code"></span>):</span>
                        <span class="coupon-discount-val" id="summary-coupon-val">-€ 0,00</span>
                    </div>

                    <div class="cost-row total-final-row">
                        <span>Totale dell'Ordine:</span>
                        <span id="final-total-display">€ <%= String.format(java.util.Locale.ITALY, "%.2f", totaleFinale) %></span>
                    </div>
                </div>
            </div>
        </div>

        <div id="successOverlay" class="success-overlay hidden">
            <div class="success-content">
                <div class="check-icon">✔</div>
                <h3>Ordine Ricevuto!</h3>
                <p>Ti stiamo reindirizzando alla ricevuta dell'ordine...</p>
            </div>
        </div>
    </main>

    <%-- Inclusione Footer e Toast --%>
    <jsp:include page="/WEB-INF/view/fragments/footer.jsp" />
    <jsp:include page="/WEB-INF/view/fragments/toast.jsp" />

    <script src="${pageContext.request.contextPath}/js/toast.js"></script>
    <script src="${pageContext.request.contextPath}/js/formUtils.js"></script>
    <script src="${pageContext.request.contextPath}/js/checkout.js"></script>
</body>
</html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="it.runitup.model.dto.ProdottoDTO" %>
<%@ page import="it.runitup.model.bean.Utente" %>
<%@ page import="it.runitup.model.bean.Coupon" %>
<%@ page import="it.runitup.model.bean.TipoScontoCoupon" %>
<%@ page import="java.time.format.DateTimeFormatter" %>

<%
    // Recupero delle liste dalla HomeServlet
    @SuppressWarnings("unchecked")
    List<ProdottoDTO> nuoviArrivi = (List<ProdottoDTO>) request.getAttribute("prodottiNuoviArrivi");
    @SuppressWarnings("unchecked")
    List<ProdottoDTO> inEvidenza = (List<ProdottoDTO>) request.getAttribute("prodottiInEvidenza");
    @SuppressWarnings("unchecked")
    List<ProdottoDTO> inOfferta = (List<ProdottoDTO>) request.getAttribute("prodottiInOfferta");
    @SuppressWarnings("unchecked")
    List<Coupon> couponHome = (List<Coupon>) request.getAttribute("couponHome");
    
    // Recupero dell'utente loggato
    Utente utenteLoggato = (Utente) session.getAttribute("utenteLoggato");
    boolean isLoggato = (utenteLoggato != null);

    // Formattatore per l'eventuale data di scadenza dei coupon
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>RunItUp - Supera i tuoi limiti</title>
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/images/common/favicon.png">
    
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/header.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/footer.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/prodotto_card.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/home.css">
    
    <!-- Preload dell'immagine hero per migliorare l'esperienza utente -->
    <link rel="preload" href="${pageContext.request.contextPath}/images/home/hero-bg.jpg" as="image">
</head>
<body>

    <%-- 1. HEADER --%>
    <jsp:include page="/WEB-INF/view/fragments/header.jsp" />

    <main class="home-main">
        
        <%-- 2. HERO SECTION --%>
        <section class="hero-section">
            <div class="hero-overlay"></div>
            <div class="hero-content">
                <div class="hero-title-container">
                    <%-- Se l'utente è loggato e ha un nome, mostra un messaggio di benvenuto --%>
                    <% if (isLoggato && utenteLoggato.getNome() != null && !utenteLoggato.getNome().isEmpty()) { %>
                        <h1 class="hero-title">Supera i tuoi limiti, ${utenteLoggato.getNome()}!</h1>
                    <% } else { %>
                        <h1 class="hero-title">Supera i tuoi limiti.</h1>
                    <% } %>
                    <p class="hero-subtitle">Scopri la nuova collezione running e trova il ritmo perfetto per la tua corsa.</p>
                </div>
                <div class="hero-btn-container">
                    <a href="${pageContext.request.contextPath}/catalogo" class="btn-primary hero-btn bounce-element">Esplora la Collezione</a>
                </div>
            </div>
        </section>

        <%-- 3. SHOP BY CATEGORY (Uomo, Donna, Unisex) --%>
        <section class="category-section container">
            <div class="category-grid">
                <a href="${pageContext.request.contextPath}/catalogo?genere=UOMO" class="category-card">
                    <img src="${pageContext.request.contextPath}/images/home/home-uomo.jpg" alt="Collezione Uomo" loading="lazy">
                    <span class="category-label">Uomo</span>
                </a>
                <a href="${pageContext.request.contextPath}/catalogo?genere=DONNA" class="category-card">
                    <img src="${pageContext.request.contextPath}/images/home/home-donna.jpg" alt="Collezione Donna" loading="lazy">
                    <span class="category-label">Donna</span>
                </a>
                <a href="${pageContext.request.contextPath}/catalogo?genere=UNISEX" class="category-card">
                    <img src="${pageContext.request.contextPath}/images/home/home-unisex.jpg" alt="Collezione Unisex" loading="lazy">
                    <span class="category-label">Per Tutti</span>
                </a>
            </div>
        </section>

        <%-- 4. CAROSELLO NUOVI ARRIVI --%>
        <section class="carousel-section container">
            <div class="section-header">
                <h2 class="section-title">Nuovi Arrivi</h2>
                <a href="${pageContext.request.contextPath}/catalogo?nuoviArrivi=true" class="link-vedi-tutti">Vedi tutti &rarr;</a>
            </div>
            
            <% if (nuoviArrivi != null && !nuoviArrivi.isEmpty()) { %>
                <div class="carousel-container snap-x">
                    <% for (ProdottoDTO dto : nuoviArrivi) { 
                        request.setAttribute("prodottoDto", dto);
                    %>
                        <div class="carousel-item">
                            <jsp:include page="/WEB-INF/view/fragments/prodotto_card.jsp" />
                        </div>
                    <% } %>
                </div>
            <% } else { %>
                <p class="empty-section-msg">I nuovi modelli stanno per atterrare. Torna a trovarci a breve!</p>
            <% } %>
        </section>

        <%-- 5. SEZIONE COUPON (Asimmetrica e Gestita Dinamicamente) --%>
        <% if (couponHome != null && !couponHome.isEmpty()) { %>
            <section class="coupon-section container">
                <%-- Se ci sono 2 coupon, usiamo la griglia asimmetrica (2/3 e 1/3) --%>
                <div class="coupon-grid <%= (couponHome.size() == 2) ? "coupon-grid-asymmetric" : "coupon-grid-single" %>">
                    <% 
                        for (int i = 0; i < couponHome.size(); i++) {
                            Coupon c = couponHome.get(i);
                            // Formattazione sconto (es. "10%" o "15€")
                            String formatSconto = (c.getTipoSconto() == TipoScontoCoupon.PERCENTUALE) 
                                                  ? c.getValoreSconto().intValue() + "%" 
                                                  : "€" + c.getValoreSconto().intValue();
                            // Se asimmetrica, il primo è grande e il secondo è piccolo
                            String cardSizeClass = (couponHome.size() == 2 && i == 0) ? "coupon-large" : "coupon-small";
                    %>
                        <div class="coupon-card <%= cardSizeClass %>">
                            <div class="coupon-decorations"></div>
                            <div class="coupon-content">
                                <span class="coupon-badge">Codice Sconto</span>
                                <h3 class="coupon-value">-<%= formatSconto %></h3>
                                <p class="coupon-code-box">Usa il codice: <strong><%= c.getCodice() %></strong></p>
                                <% if (c.getDataScadenza() != null) { %>
                                    <p class="coupon-expiry">Valido fino al: <%= c.getDataScadenza().format(dtf) %></p>
                                <% } else { %>
                                    <p class="coupon-expiry">Senza scadenza</p>
                                <% } %>
                            </div>
                        </div>
                    <% } %>
                </div>
            </section>
        <% } %>

        <%-- 6. BENTO BANNER SHOEFINDER --%>
        <section class="bento-shoefinder-section container">
            <div class="bento-wrapper">
                
                <div class="bento-cell bento-image-cell">
                    <img src="${pageContext.request.contextPath}/images/home/shoefinder-banner.jpg" alt="Trova la scarpa perfetta" loading="lazy">
                </div>

                <div class="bento-cell bento-text-cell">
                    <h2>Non sai quale scegliere?</h2>
                    <p>
                        Rispondi a 4 semplici domande e lascia che il nostro algoritmo trovi la compagna di corsa perfetta per te.
                    </p>
                </div>

                <a href="${pageContext.request.contextPath}/findmyshoes" class="bento-cell bento-btn-cell">
                    <span class="bento-btn-text">Avvia FindMyShoes</span>
                    <span class="bento-btn-icon">&rarr;</span>
                </a>

            </div>
        </section>

        <%-- 7. CAROSELLO IN EVIDENZA --%>
        <section class="carousel-section container">
            <div class="section-header">
                <h2 class="section-title">Scelti dai Professionisti</h2>
                <a href="${pageContext.request.contextPath}/catalogo?inEvidenza=true" class="link-vedi-tutti">Vedi tutti &rarr;</a>
            </div>
            
            <% if (inEvidenza != null && !inEvidenza.isEmpty()) { %>
                <div class="carousel-container snap-x">
                    <% for (ProdottoDTO dto : inEvidenza) { 
                        request.setAttribute("prodottoDto", dto);
                    %>
                        <div class="carousel-item">
                            <jsp:include page="/WEB-INF/view/fragments/prodotto_card.jsp" />
                        </div>
                    <% } %>
                </div>
            <% } else { %>
                <p class="empty-section-msg">Aggiornamento catalogo in corso.</p>
            <% } %>
        </section>

        <%-- 8. GRIGLIA PROMOZIONI (Fissa 2x2 o in riga se < 4) --%>
        <section class="promo-grid-section container">
            <div class="section-header">
                <h2 class="section-title text-danger">Offerte a Tempo</h2>
                <a href="${pageContext.request.contextPath}/catalogo?inOfferta=true" class="link-vedi-tutti">Vedi tutte le promo &rarr;</a>
            </div>
            
            <% if (inOfferta != null && !inOfferta.isEmpty()) { %>
                <div class="promo-grid">
                    <% for (ProdottoDTO dto : inOfferta) { 
                        request.setAttribute("prodottoDto", dto);
                    %>
                        <div class="promo-grid-item">
                            <jsp:include page="/WEB-INF/view/fragments/prodotto_card.jsp" />
                        </div>
                    <% } %>
                </div>
            <% } else { %>
                <p class="empty-section-msg">Al momento non ci sono promozioni attive. Iscriviti per non perdere i prossimi sconti!</p>
            <% } %>
        </section>

    </main>

    <%-- 9. FOOTER --%>
    <jsp:include page="/WEB-INF/view/fragments/footer.jsp" />
    
</body>
</html>
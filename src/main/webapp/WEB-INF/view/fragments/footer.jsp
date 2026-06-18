<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<footer class="main-footer">
    <%-- INNER CONTAINER PER APPLICARE IL MAX-WIDTH E CENTRARE IL CONTENUTO --%>
    <div class="footer-container">
        
        <%-- COLONNA 1: BRAND, LOGO E SOCIAL --%>
        <div class="footer-column brand-column">
            <a href="${pageContext.request.contextPath}/" class="footer-logo">
                <img src="${pageContext.request.contextPath}/images/common/logo-runitup.svg" alt="RunItUp Logo" class="footer-logo-img">
            </a>
            <p class="footer-brand-description">
                La tua corsa inizia da qui. Le migliori scarpe da running selezionate dai professionisti per superare ogni tuo limite.
            </p>
            <div class="footer-social-wrapper">
                <a href="https://instagram.com" target="_blank" rel="noopener" aria-label="Instagram">
                    <img src="${pageContext.request.contextPath}/images/common/social/instagram.png" alt="Instagram" class="social-icon">
                </a>
                <a href="https://facebook.com" target="_blank" rel="noopener" aria-label="Facebook">
                    <img src="${pageContext.request.contextPath}/images/common/social/facebook.png" alt="Facebook" class="social-icon">
                </a>
                <a href="https://x.com" target="_blank" rel="noopener" aria-label="X (Twitter)">
                    <img src="${pageContext.request.contextPath}/images/common/social/x.png" alt="X" class="social-icon">
                </a>
                <a href="https://youtube.com" target="_blank" rel="noopener" aria-label="YouTube">
                    <img src="${pageContext.request.contextPath}/images/common/social/youtube.png" alt="YouTube" class="social-icon">
                </a>
            </div>
        </div>

        <%-- COLONNA 2: ACQUISTA (CATEGORIE E HIGHLIGHTS) --%>
        <div class="footer-column">
            <h3>Acquista</h3>
            <ul>
                <li><a href="${pageContext.request.contextPath}/catalogo?genere=UOMO">Collezione Uomo</a></li>
                <li><a href="${pageContext.request.contextPath}/catalogo?genere=DONNA">Collezione Donna</a></li>
                <li><a href="${pageContext.request.contextPath}/catalogo?genere=UNISEX">Scarpe Unisex</a></li>
                <li><a href="${pageContext.request.contextPath}/shoefinder" class="footer-highlight-link">Shoe Finder</a></li>
                <li><a href="${pageContext.request.contextPath}/catalogo?nuoviArrivi=true">Nuovi Arrivi</a></li>
                <li><a href="${pageContext.request.contextPath}/catalogo?inOfferta=true" class="footer-promo-link">Promozioni</a></li>
            </ul>
        </div>

        <%-- COLONNA 3: ASSISTENZA CLIENTE --%>
        <div class="footer-column">
            <h3>Assistenza</h3>
            <ul>
                <li><a href="${pageContext.request.contextPath}/assistenza">Contattaci</a></li>
                <li><a href="${pageContext.request.contextPath}/spedizioni">Spedizioni e Consegna</a></li>
                <li><a href="${pageContext.request.contextPath}/resi">Resi e Rimborsi</a></li>
                <li><a href="${pageContext.request.contextPath}/faq">Domande Frequenti (FAQ)</a></li>
            </ul>
        </div>

        <%-- COLONNA 4: INFO LEGALI --%>
        <div class="footer-column">
            <h3>Info Legali</h3>
            <ul>
                <li><a href="${pageContext.request.contextPath}/termini">Termini e Condizioni</a></li>
                <li><a href="${pageContext.request.contextPath}/privacy">Privacy Policy</a></li>
                <li><a href="${pageContext.request.contextPath}/cookie">Cookie Policy</a></li>
            </ul>
        </div>

    </div>

    <%-- FOOTER BOTTOM: COPYRIGHT E CIRCUITI DI PAGAMENTO --%>
    <div class="footer-bottom">
        <div class="footer-bottom-container">
            <p class="footer-copyright">&copy; 2026 RunItUp. Tutti i diritti riservati. Gruppo RunTime.</p>
            
            <div class="footer-payment-methods">
                <img src="${pageContext.request.contextPath}/images/common/payments/visa.png" alt="Visa" class="payment-icon">
                <img src="${pageContext.request.contextPath}/images/common/payments/mastercard.png" alt="Mastercard" class="payment-icon">
                <img src="${pageContext.request.contextPath}/images/common/payments/paypal.png" alt="PayPal" class="payment-icon">
                <img src="${pageContext.request.contextPath}/images/common/payments/applepay.png" alt="Apple Pay" class="payment-icon">
                <img src="${pageContext.request.contextPath}/images/common/payments/googlepay.png" alt="Google Pay" class="payment-icon">
            </div>
        </div>
    </div>
</footer>
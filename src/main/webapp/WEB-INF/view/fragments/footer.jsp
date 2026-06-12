<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<footer class="main-footer">
    <div class="footer-container">
        
        <div class="footer-column">
            <h3>Prodotti</h3>
            <ul>
                <li><a href="${pageContext.request.contextPath}/categoria/uomo">Uomo</a></li>
                <li><a href="${pageContext.request.contextPath}/categoria/donna">Donna</a></li>
                <li><a href="${pageContext.request.contextPath}/categoria/bambino">Bambino</a></li>
                <li><a href="${pageContext.request.contextPath}/promozioni">Promozioni</a></li>
            </ul>
        </div>

        <div class="footer-column">
            <h3>Assistenza</h3>
            <ul>
                <li><a href="${pageContext.request.contextPath}/assistenza">Contattaci</a></li>
                <li><a href="${pageContext.request.contextPath}/spedizioni">Spedizioni e Consegna</a></li>
                <li><a href="${pageContext.request.contextPath}/resi">Resi e Rimborsi</a></li>
                <li><a href="${pageContext.request.contextPath}/faq">FAQ</a></li>
            </ul>
        </div>

        <div class="footer-column">
            <h3>Info Legali</h3>
            <ul>
                <li><a href="${pageContext.request.contextPath}/termini">Termini e Condizioni</a></li>
                <li><a href="${pageContext.request.contextPath}/privacy">Privacy Policy</a></li>
                <li><a href="${pageContext.request.contextPath}/cookie">Cookie Policy</a></li>
            </ul>
        </div>

    </div>

    <div class="footer-bottom">
        <p>&copy; 2026 RunItUp. Tutti i diritti riservati.</p>
        <div class="payment-methods">
            <span>💳 Carte di Credito</span> | <span>🅿️ PayPal</span> | <span>🚚 Contrassegno</span>
        </div>
    </div>
</footer>
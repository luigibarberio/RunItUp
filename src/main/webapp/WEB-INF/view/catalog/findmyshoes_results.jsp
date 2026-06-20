<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="it.runitup.model.dto.ProdottoDTO" %>

<%
    // Recuperiamo i risultati calcolati dalla Servlet
    @SuppressWarnings("unchecked")
    List<ProdottoDTO> prodottiConsigliati = (List<ProdottoDTO>) request.getAttribute("prodottiConsigliati");
    
    boolean hasResults = prodottiConsigliati != null && !prodottiConsigliati.isEmpty();
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>I tuoi Risultati - FindMyShoes - RunItUp</title>

    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/images/common/favicon.png">
    
    <%-- CSS Condivisi --%>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/header.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/footer.css">
    
    <%-- CSS Specifico per le Card --%>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/prodotto_card.css">
    
    <%-- CSS Specifico per questa pagina di risultati --%>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/findmyshoes_results.css">
</head>
<body>

    <jsp:include page="/WEB-INF/view/fragments/header.jsp" />

    <main class="results-main">
        <div class="results-container">

            <%-- HEADER CELEBRATIVO --%>
            <div class="results-header">
                <% if (hasResults) { %>
                    <div class="results-icon">🎉</div>
                    <h1>Trovate!</h1>
                    <p>Abbiamo analizzato il tuo stile di corsa. Ecco i modelli perfetti per farti volare:</p>
                <% } else { %>
                    <div class="results-icon">🤔</div>
                    <h1>Mmh... Sei un runner esigente!</h1>
                    <p>Non abbiamo trovato una scarpa nel nostro catalogo che corrisponda *esattamente* a tutti i filtri che hai scelto. Ma non temere!</p>
                <% } %>
            </div>

            <%-- GRIGLIA DEI RISULTATI O STATO VUOTO --%>
            <% if (hasResults) { %>
                <div class="results-grid">
                    <% 
                        // Il principio DRY in azione: ricicliamo il frammento del catalogo!
                        for (ProdottoDTO prodotto : prodottiConsigliati) { 
                            request.setAttribute("prodottoDto", prodotto);
                    %>
                        <jsp:include page="/WEB-INF/view/fragments/prodotto_card.jsp" />
                    <% } %>
                </div>
                
                <div class="results-actions">
                    <a href="${pageContext.request.contextPath}/findmyshoes" class="btn-secondary">Rifai il Test</a>
                </div>
                
            <% } else { %>
                
                <div class="empty-state-actions">
                    <a href="${pageContext.request.contextPath}/findmyshoes" class="btn-primary">Ripeti il Quiz con filtri meno severi</a>
                    <a href="${pageContext.request.contextPath}/catalogo" class="btn-secondary">Sfoglia tutto il Catalogo</a>
                </div>
                
            <% } %>

        </div>
    </main>

    <jsp:include page="/WEB-INF/view/fragments/footer.jsp" />

</body>
</html>
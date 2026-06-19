<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="it.runitup.model.bean.Prodotto" %>
<%@ page import="it.runitup.model.dto.VarianteDTO" %>
<%@ page import="java.util.List" %>
<%@ page import="com.google.gson.Gson" %>
<%
    Prodotto prodotto = (Prodotto) request.getAttribute("prodotto");
    List<VarianteDTO> varianti = (List<VarianteDTO>) request.getAttribute("varianti");
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= prodotto != null ? prodotto.getNome() : "Dettaglio Prodotto" %> - RunItUp</title>
    
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/header.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/footer.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/toast.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/dettaglio.css">
</head>
<body>
    <jsp:include page="/WEB-INF/view/fragments/header.jsp" />

    <div id="toastContainer" class="toast-container"></div>

    <main class="dettaglio-main">
        <div class="product-layout">

            <div class="product-image">
                <%-- Immagine Primaria (Visibile di default) --%>
                <img src="${pageContext.request.contextPath}/images/prodotti?id=<%= prodotto.getIdProdotto() %>&index=0" 
                     alt="<%= prodotto.getNome() %>" 
                     class="product-img primary-img" 
                     loading="lazy"
                     onerror="this.src='${pageContext.request.contextPath}/images/placeholder.svg'">
                     
                <%-- Immagine Secondaria (Appare al passaggio del mouse) --%>
                <img src="${pageContext.request.contextPath}/images/prodotti?id=<%= prodotto.getIdProdotto() %>&index=1" 
                     alt="<%= prodotto.getNome() %> (Vista 2)" 
                     class="product-img secondary-img" 
                     loading="lazy"
                     onerror="this.style.display='none'">
            </div>

            <div class="product-details">
                <div class="product-info">
                    <h1 class="product-name"><%= prodotto.getNome() %></h1>
                    <p class="product-brand"><%= prodotto.getBrand().getNomeBrand() %></p>
                </div>

                <div id="product-variants" class="product-variants" data-varianti='<%= new Gson().toJson(varianti) %>'>
                    
                    <div id="price-container" class="price-section">
                        <span class="current-price">Seleziona una variante</span>
                    </div>

                    <div class="selector-box">
                        <label for="coloreSelect">Colore:</label>
                        <select id="coloreSelect">
                            <option value="">Scegli colore</option>
                        </select>
                    </div>

                    <div class="selector-box">
                        <label for="tagliaSelect">Taglia:</label>
                        <select id="tagliaSelect">
                            <option value="">Scegli taglia</option>
                        </select>
                    </div>

                    <button type="button" id="btnAggiungi" class="btn-cart" disabled>AGGIUNGI AL CARRELLO</button>
                </div>
            </div>

        </div>
    </main>

    <jsp:include page="/WEB-INF/view/fragments/footer.jsp" />

    <script src="${pageContext.request.contextPath}/js/toast.js"></script>
    <script src="${pageContext.request.contextPath}/js/dettaglio.js"></script>
</body>
</html>
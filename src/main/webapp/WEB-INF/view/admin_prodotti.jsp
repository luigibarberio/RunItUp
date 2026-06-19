<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="it.runitup.model.dto.ProdottoDTO" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>

<%
    @SuppressWarnings("unchecked")
    List<ProdottoDTO> prodottiAdmin = (List<ProdottoDTO>) request.getAttribute("prodottiAdmin");
    String feedbackMsg = (String) request.getAttribute("feedbackMsg");
    
    NumberFormat valutaFormat = NumberFormat.getCurrencyInstance(Locale.ITALY);
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gestione Prodotti - Admin RunItUp</title>
    
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/images/common/favicon.png">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/toast.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin_layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin_prodotti.css">
</head>
<body>

    <div class="admin-layout-wrapper">
        
        <%-- SIDEBAR LATERALE FISSA --%>
        <jsp:include page="/WEB-INF/view/fragments/admin_sidebar.jsp" />

        <%-- CONTENUTO PRINCIPALE --%>
        <main class="admin-main-content">
            
            <div class="admin-header-actions">
                <div>
                    <h1 class="admin-page-title">Catalogo Prodotti</h1>
                    <p class="admin-page-subtitle">Gestisci le scarpe, le varianti e controlla le giacenze.</p>
                </div>
                <a href="${pageContext.request.contextPath}/admin/prodotto/gestione" class="btn-primary btn-add-product">
                    + Nuovo Prodotto
                </a>
            </div>

            <% if (prodottiAdmin != null && !prodottiAdmin.isEmpty()) { %>
                <div class="admin-products-grid">
                    <% for (ProdottoDTO dto : prodottiAdmin) { %>
                        <div class="admin-product-card" id="card-prodotto-<%= dto.getProdotto().getIdProdotto() %>">
                            
                            <div class="admin-card-body">
                                <div class="admin-card-img-box">
                                    <img src="${pageContext.request.contextPath}/images/prodotti?id=<%= dto.getProdotto().getIdProdotto() %>&index=0" 
                                         alt="<%= dto.getProdotto().getNome() %>"
                                         onerror="this.src='${pageContext.request.contextPath}/images/placeholder.svg'">
                                </div>
                                <div class="admin-card-info">
                                    <span class="admin-product-id">ID: #<%= dto.getProdotto().getIdProdotto() %></span>
                                    <h3 class="admin-product-name"><%= dto.getProdotto().getNome() %></h3>
                                    <p class="admin-product-brand"><%= dto.getProdotto().getBrand().getNomeBrand() %> &bull; <%= dto.getProdotto().getGenere().name() %></p>
                                    <p class="admin-product-price">
                                        <% if (dto.getPrezzoMinimo() != null) { %>
                                            A partire da <%= valutaFormat.format(dto.getPrezzoMinimo()) %>
                                        <% } else { %>
                                            <span class="text-danger">Nessuna variante inserita</span>
                                        <% } %>
                                    </p>
                                </div>
                            </div>

                            <div class="admin-card-footer">
                                <a href="${pageContext.request.contextPath}/admin/prodotto/gestione?id=<%= dto.getProdotto().getIdProdotto() %>" class="btn-admin-action btn-edit">
                                    ✏️ Modifica
                                </a>
                                <a href="${pageContext.request.contextPath}/admin/prodotto/varianti?idProdotto=<%= dto.getProdotto().getIdProdotto() %>" class="btn-admin-action btn-variants">
                                    📦 Varianti
                                </a>
                                <button type="button" class="btn-admin-action btn-delete" data-id="<%= dto.getProdotto().getIdProdotto() %>">
                                    🗑️ Elimina
                                </button>
                            </div>
                            
                        </div>
                    <% } %>
                </div>
            <% } else { %>
                <div class="empty-state-box">
                    <p>Nessun prodotto presente a catalogo.</p>
                    <a href="${pageContext.request.contextPath}/admin/prodotto/gestione" class="btn-primary">Crea il primo prodotto</a>
                </div>
            <% } %>

        </main>
    </div>

    <%-- TOAST E OVERLAY PER MESSAGGI DI SUCCESSO --%>
    <jsp:include page="/WEB-INF/view/fragments/toast.jsp" />

    <%-- Passaggio del messaggio di feedback per il Javascript --%>
    <% if (feedbackMsg != null) { %>
        <script>
            window.serverFeedback = "<%= feedbackMsg %>";
        </script>
    <% } %>

    <script src="${pageContext.request.contextPath}/js/toast.js"></script>
    <script src="${pageContext.request.contextPath}/js/admin_prodotti.js"></script>
</body>
</html>
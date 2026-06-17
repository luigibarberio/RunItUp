<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="it.runitup.model.dto.ProdottoDTO" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>

<%
    ProdottoDTO dto = (ProdottoDTO) request.getAttribute("prodottoDto");

    if (dto != null && dto.getProdotto() != null) {
        NumberFormat valutaFormat = NumberFormat.getCurrencyInstance(Locale.ITALY);
        String linkDettaglio = request.getContextPath() + "/dettaglio?id=" + dto.getProdotto().getIdProdotto();
%>

<article class="product-card" data-id="<%= dto.getProdotto().getIdProdotto() %>">
    
    <div class="card-image-wrapper">
        <a href="<%= linkDettaglio %>">
            <img src="${pageContext.request.contextPath}/images/prodotti?id=<%= dto.getProdotto().getIdProdotto() %>" 
                 alt="<%= dto.getProdotto().getNome() %>" 
                 class="product-img" 
                 loading="lazy"
                 onerror="this.src='${pageContext.request.contextPath}/images/placeholder.svg'">
        </a>
        
        <div class="badges-container">
            <% if (dto.isNuovo()) { %><span class="badge badge-new">Nuovo</span><% } %>
            <% if (dto.getProdotto().isInEvidenza()) { %><span class="badge badge-hot">In Evidenza</span><% } %>
            <% if (dto.isScontato()) { %><span class="badge badge-sale">Offerta</span><% } %>
        </div>
    </div>

    <div class="card-body">
        
        <div class="card-meta">
            <span class="brand-name">
                <%= dto.getProdotto().getBrand() != null ? dto.getProdotto().getBrand().getNomeBrand() : "Brand N/D" %>
            </span>
        </div>
        
        <h3 class="product-name">
            <a href="<%= linkDettaglio %>"><%= dto.getProdotto().getNome() %></a>
        </h3>
        
        <p class="product-gender">
            <%= dto.getProdotto().getGenere() != null ? dto.getProdotto().getGenere().name() : "Unisex" %>
        </p>

        <div class="price-wrapper">
            <% 
                boolean isPrezzoMinore = false;
                if (dto.getPrezzoMinimo() != null && dto.getPrezzoOriginale() != null) {
                    if (dto.getPrezzoMinimo().compareTo(dto.getPrezzoOriginale()) < 0) {
                        isPrezzoMinore = true;
                    }
                }

                if (isPrezzoMinore) { 
            %>
                <span class="current-price sale-price"><%= valutaFormat.format(dto.getPrezzoMinimo()) %></span>
                <span class="old-price"><%= valutaFormat.format(dto.getPrezzoOriginale()) %></span>
            <% } else { %>
                <span class="current-price">
                    <%= (dto.getPrezzoMinimo() != null) ? valutaFormat.format(dto.getPrezzoMinimo()) : "N/D" %>
                </span>
            <% } %>
        </div>
        
    </div>
</article>

<% } %>
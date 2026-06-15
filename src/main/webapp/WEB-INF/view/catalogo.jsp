<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>
<%@ page import="it.runitup.model.bean.*" %>
<%@ page import="it.runitup.model.dto.ProdottoDTO" %>
<%@ page import="it.runitup.model.filter.FiltroCatalogo" %>

<%
    List<ProdottoDTO> prodotti = (List<ProdottoDTO>) request.getAttribute("prodottiCatalogo");
    List<TipoTerreno> tipiTerrenoDisponibili = Arrays.asList(TipoTerreno.values());
    List<TipoAppoggio> tipiAppoggioDisponibili = Arrays.asList(TipoAppoggio.values());
    List<TipoPasso> tipiPassoDisponibili = Arrays.asList(TipoPasso.values());
    FiltroCatalogo filtri = (FiltroCatalogo) request.getAttribute("filtriCorrenti");
    int paginaCorrente = (request.getAttribute("paginaCorrente") != null) ? (Integer) request.getAttribute("paginaCorrente") : 1;
    
    Boolean isPaginaSuccAttr = (Boolean) request.getAttribute("isPaginaSuccessiva");
    boolean isPaginaSuccessiva = (isPaginaSuccAttr != null && isPaginaSuccAttr);
    
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>Catalogo - RunItUp</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/header.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/footer.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/catalogo.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/prodotto_card.css">
</head>
<body>

<jsp:include page="/WEB-INF/view/fragments/header.jsp" />

<main class="catalogo-main">

    <%-- Sezione Ordinamento --%>
    <header class="catalogo-header">
        <%-- Questo bottone apparirà solo su schermi < 991px grazie alla classe mobile-only --%>
        <button type="button" class="btn-primary mobile-only" onclick="toggleFiltri()" style="margin-right: auto;">
            ☰ Filtri
        </button>

        <select id="ordinaSelect" name="ordina" form="filtriForm" onchange="submitFiltri()">
            <option value="vendite" <%= (filtri != null && "vendite".equals(filtri.getOrdinamento())) ? "selected" : "" %>>I più venduti</option>
            <option value="prezzo_asc" <%= (filtri != null && "prezzo_asc".equals(filtri.getOrdinamento())) ? "selected" : "" %>>Prezzo: dal più basso</option>
            <option value="prezzo_desc" <%= (filtri != null && "prezzo_desc".equals(filtri.getOrdinamento())) ? "selected" : "" %>>Prezzo: dal più alto</option>
            <option value="recenti" <%= (filtri != null && "recenti".equals(filtri.getOrdinamento())) ? "selected" : "" %>>Recenti</option>
        </select>
    </header>
    
    <aside class="catalogo-filters" id="sidebarFiltri">
        <form id="filtriForm" class="filters-form" method="get" action="${pageContext.request.contextPath}/catalogo" onsubmit="pulisciCampiVuoti()">
            <%-- Bottone Chiudi visibile solo su mobile --%>
            <button type="button" class="mobile-only" onclick="toggleFiltri()" 
                    style="width: 100%; padding: 12px; margin-bottom: 20px; background: var(--sfondo-alternativo); border: none; border-radius: 6px; font-weight: bold; cursor: pointer;">
                ✕ Chiudi
            </button>
            <div class="filter-group">
                <h3 class="filter-title">Categorie</h3>
                <div class="filter-pills-grid">
                    <label class="custom-pill">
                        <input type="checkbox" name="inEvidenza" value="true" <%= (filtri != null && filtri.getInEvidenza() != null && filtri.getInEvidenza()) ? "checked" : "" %>>
                        <span class="pill-box">In Evidenza</span>
                    </label>
                    <label class="custom-pill">
                        <input type="checkbox" name="inOfferta" value="true" <%= (filtri != null && filtri.getInOfferta() != null && filtri.getInOfferta()) ? "checked" : "" %>>
                        <span class="pill-box">In Offerta</span>
                    </label>
                    <label class="custom-pill" style="grid-column: span 2;">
                        <input type="checkbox" name="nuoviArrivi" value="true" <%= (filtri != null && filtri.getNuoviArrivi() != null && filtri.getNuoviArrivi()) ? "checked" : "" %>>
                        <span class="pill-box">Nuovi Arrivi</span>
                    </label>
                </div>
            </div>

            <%-- BRAND --%>
            <div class="filter-group">
                <h3 class="filter-title">Brand</h3>
                <div class="filter-pills-wrap">
                <%
                    if (prodotti != null) {
                        Map<Integer, String> brandsEstratti = new TreeMap<>();
                        for (ProdottoDTO pDto : prodotti) {
                            if (pDto.getProdotto().getBrand() != null) {
                                brandsEstratti.put(pDto.getProdotto().getBrand().getIdBrand(), pDto.getProdotto().getBrand().getNomeBrand());
                            }
                        }
                        for (Map.Entry<Integer, String> entry : brandsEstratti.entrySet()) {
                            Integer idBrand = entry.getKey();
                            String nomeBrand = entry.getValue();
                            boolean isChecked = filtri != null && filtri.getBrandIds() != null && filtri.getBrandIds().contains(idBrand);
                %>
                            <label class="custom-pill">
                                <input type="checkbox" name="brand" value="<%= idBrand %>" <%= isChecked ? "checked" : "" %>>
                                <span class="pill-box"><%= nomeBrand %></span>
                            </label>
                <%      }
                    } %>
                </div>
            </div>

            <%-- RANGE DI PREZZO --%>
            <div class="filter-group">
                <h3 class="filter-title">Prezzo</h3>
                <input type="number" id="prezzoMin" name="prezzoMin" placeholder="Minimo €" value="<%= (filtri != null && filtri.getPrezzoMinimo() != null) ? filtri.getPrezzoMinimo() : "" %>" min="0" step="0.01" style="margin-bottom: 8px;">
                <input type="number" id="prezzoMax" name="prezzoMax" placeholder="Massimo €" value="<%= (filtri != null && filtri.getPrezzoMassimo() != null) ? filtri.getPrezzoMassimo() : "" %>" min="0" step="0.01">
            </div>

            <%-- TAGLIE --%>
            <div class="filter-group">
                <h3 class="filter-title">Taglia</h3>
                <div class="filter-pills-wrap">
                <%
                    if (prodotti != null) {
                        Set<String> taglieSet = new TreeSet<>();
                        for (ProdottoDTO prodotto : prodotti) taglieSet.addAll(prodotto.getTaglieDisponibili());
                        for (String taglia : taglieSet) {
                            boolean isChecked = filtri != null && filtri.getTaglie() != null && filtri.getTaglie().contains(taglia);
                %>
                            <label class="custom-pill">
                                <input type="checkbox" name="taglie" value="<%= taglia %>" <%= isChecked ? "checked" : "" %>>
                                <span class="pill-box"><%= taglia %></span>
                            </label>
                <%      }
                    } %>
                </div>
            </div>

            <%-- COLORI --%>
            <div class="filter-group">
                <h3 class="filter-title">Colore</h3>
                <div class="filter-pills-wrap">
                <%
                    if (prodotti != null) {
                        Set<String> coloriSet = new TreeSet<>();
                        for (ProdottoDTO prodotto : prodotti) coloriSet.addAll(prodotto.getColoriDisponibili());
                        for (String colore : coloriSet) {
                            boolean isChecked = filtri != null && filtri.getColori() != null && filtri.getColori().contains(colore);
                %>
                            <label class="custom-pill">
                                <input type="checkbox" name="colori" value="<%= colore %>" <%= isChecked ? "checked" : "" %>>
                                <span class="pill-box"><%= colore %></span>
                            </label>
                <%      }
                    } %>
                </div>
            </div>

            <%-- BOTTONI SUBMIT/RESET --%>
            <div class="filter-actions">
                <button type="submit" class="btn-primary">Applica Filtri</button>
                <a href="${pageContext.request.contextPath}/catalogo" class="btn-secondary">Reset Filtri</a>
            </div>

        </form>
    </aside>

    <section class="catalogo-prodotti">
        <%
            if (prodotti != null && !prodotti.isEmpty()) {
                for (ProdottoDTO prodotto : prodotti) {
                    request.setAttribute("prodottoDto", prodotto);
        %>
                    <jsp:include page="/WEB-INF/view/fragments/prodotto_card.jsp" />
        <%
                }
            } else {
        %>
            <p>Nessun prodotto trovato con questi filtri.</p>
        <% } %>
    </section>

    <nav class="catalogo-pagination">
        <ul>
            <%-- Corretto: disabilita Precedente se siamo a pagina 1 --%>
            <li class="pagination-item <%= paginaCorrente <= 1 ? "disabled" : "" %>">
                <button type="submit" form="filtriForm" name="page" value="<%= paginaCorrente - 1 %>" <%= paginaCorrente <= 1 ? "disabled" : "" %>>Precedente</button>
            </li>
            <%-- Corretto: usa il flag calcolato dal Controller --%>
            <li class="pagination-item <%= !isPaginaSuccessiva ? "disabled" : "" %>">
                <button type="submit" form="filtriForm" name="page" value="<%= paginaCorrente + 1 %>" <%= !isPaginaSuccessiva ? "disabled" : "" %>>Successiva</button>
            </li>            
        </ul>
    </nav>

</main>

<jsp:include page="/WEB-INF/view/fragments/footer.jsp" />

<script src="${pageContext.request.contextPath}/js/catalogo.js"></script>

</body>
</html>
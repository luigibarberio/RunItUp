<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="it.runitup.model.bean.*" %>
<%@ page import="it.runitup.model.dto.ProdottoDTO" %>
<%@ page import="it.runitup.model.filter.FiltroCatalogo" %>

<%
    List<ProdottoDTO> prodotti = (List<ProdottoDTO>) request.getAttribute("prodottiCatalogo");
    List<ProdottoDTO> prodottiSidebar = (List<ProdottoDTO>) request.getAttribute("prodottiSidebar");
    List<TipoTerreno> tipiTerrenoDisponibili = Arrays.asList(TipoTerreno.values());
    List<TipoAppoggio> tipiAppoggioDisponibili = Arrays.asList(TipoAppoggio.values());
    List<TipoPasso> tipiPassoDisponibili = Arrays.asList(TipoPasso.values());
    FiltroCatalogo filtri = (FiltroCatalogo) request.getAttribute("filtriCorrenti");
    int paginaCorrente = (request.getAttribute("paginaCorrente") != null) ? (Integer) request.getAttribute("paginaCorrente") : 1;
    
    Boolean isPaginaSuccAttr = (Boolean) request.getAttribute("isPaginaSuccessiva");
    boolean isPaginaSuccessiva = (isPaginaSuccAttr != null && isPaginaSuccAttr);
    
    // Controlliamo se è attiva una ricerca testuale
    boolean hasRicerca = filtri != null && filtri.getRicercaTestuale() != null && !filtri.getRicercaTestuale().trim().isEmpty();
    String queryRicerca = hasRicerca ? filtri.getRicercaTestuale() : "";
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Catalogo - RunItUp</title>
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/images/common/favicon.png">
    
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

        <%-- BADGE DI RICERCA TESTUALE ATTIVA --%>
        <% if (hasRicerca) { %>
            <div class="search-active-badge">
                <h3>Risultati per: <span>"<%= queryRicerca %>"</span></h3>
                <a href="${pageContext.request.contextPath}/catalogo" class="clear-search-btn">Rimuovi ✕</a>
            </div>
        <% } %>

        <select id="ordinaSelect" name="ordina" form="filtriForm" onchange="submitFiltri()">
            <option value="vendite" <%= (filtri != null && "vendite".equals(filtri.getOrdinamento())) ? "selected" : "" %>>I più venduti</option>
            <option value="prezzo_asc" <%= (filtri != null && "prezzo_asc".equals(filtri.getOrdinamento())) ? "selected" : "" %>>Prezzo: dal più basso</option>
            <option value="prezzo_desc" <%= (filtri != null && "prezzo_desc".equals(filtri.getOrdinamento())) ? "selected" : "" %>>Prezzo: dal più alto</option>
            <option value="recenti" <%= (filtri != null && "recenti".equals(filtri.getOrdinamento())) ? "selected" : "" %>>Recenti</option>
        </select>
    </header>
    
    <aside class="catalogo-filters" id="sidebarFiltri">
        <form id="filtriForm" class="filters-form" method="get" action="${pageContext.request.contextPath}/catalogo" onsubmit="pulisciCampiVuoti()">
            
            <%-- CAMPO NASCOSTO PER LA RICERCA TESTUALE (Persistenza dello stato) --%>
            <% if (hasRicerca) { %>
                <input type="hidden" name="q" value="<%= queryRicerca.replace("\"", "&quot;") %>">
            <% } %>

            <%-- Bottone Chiudi visibile solo su mobile --%>
            <button type="button" class="close-btn mobile-only" onclick="toggleFiltri()">
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

            <%-- GENERE --%>
            <div class="filter-group">
                <h3 class="filter-title">Genere</h3>
                <div class="filter-pills-wrap">
                <%
                    // Recuperiamo la lista dei generi selezionati una sola volta
                    List<GenereScarpa> generiList = (filtri != null) ? filtri.getGeneri() : null;
                %>
                    <label class="custom-pill">
                        <input type="checkbox" name="genere" value="UOMO" <%= (generiList != null && generiList.contains(GenereScarpa.UOMO)) ? "checked" : "" %>>
                        <span class="pill-box">Uomo</span>
                    </label>
                    <label class="custom-pill">
                        <input type="checkbox" name="genere" value="DONNA" <%= (generiList != null && generiList.contains(GenereScarpa.DONNA)) ? "checked" : "" %>>
                        <span class="pill-box">Donna</span>
                    </label>
                    <label class="custom-pill">
                        <input type="checkbox" name="genere" value="UNISEX" <%= (generiList != null && generiList.contains(GenereScarpa.UNISEX)) ? "checked" : "" %>>
                        <span class="pill-box">Unisex</span>
                    </label>
                </div>
            </div>

            <%-- BRAND --%>
            <div class="filter-group">
                <h3 class="filter-title">Brand</h3>
                <div class="filter-pills-wrap">
                <%
                    if (prodottiSidebar != null) {
                        Map<Integer, String> brandsEstratti = new TreeMap<>();
                        for (ProdottoDTO pDto : prodottiSidebar) {
                            if (pDto.getProdotto().getBrand() != null) {
                                brandsEstratti.put(pDto.getProdotto().getBrand().getIdBrand(), pDto.getProdotto().getBrand().getNomeBrand());
                            }
                        }
                        // Controllo se la mappa dei brand è vuota e mostro un messaggio appropriato
                        if (brandsEstratti.isEmpty()) {
                %>
                            <p class="no-brands-msg">Nessun brand disponibile.</p>
                <%
                        } else {
                            // Ciclo solo se la mappa non è vuota
                            for (Map.Entry<Integer, String> entry : brandsEstratti.entrySet()) {
                                Integer idBrand = entry.getKey();
                                String nomeBrand = entry.getValue();
                                boolean isChecked = filtri != null && filtri.getBrandIds() != null && filtri.getBrandIds().contains(idBrand);
                %>
                                <label class="custom-pill">
                                    <input type="checkbox" name="brand" value="<%= idBrand %>" <%= isChecked ? "checked" : "" %>>
                                    <span class="pill-box"><%= nomeBrand %></span>
                                </label>
                <%
                            }
                        }
                    }
                %>
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
                    if (prodottiSidebar != null) {
                        Set<String> taglieSet = new TreeSet<>();
                        for (ProdottoDTO prodotto : prodottiSidebar) {
                            if (prodotto.getTaglieDisponibili() != null) {
                                taglieSet.addAll(prodotto.getTaglieDisponibili());
                            }
                        }

                        // CONTROLLO SPOSTATO FUORI: gestito prima del ciclo
                        if (taglieSet.isEmpty()) {
                %>
                            <p class="no-taglie-msg">Nessuna taglia disponibile.</p>
                <%
                        } else {
                            // Il ciclo avviene solo se ci sono taglie
                            for (String taglia : taglieSet) {
                                boolean isChecked = filtri != null && filtri.getTaglie() != null && filtri.getTaglie().contains(taglia);
                %>
                                <label class="custom-pill">
                                    <input type="checkbox" name="taglie" value="<%= taglia %>" <%= isChecked ? "checked" : "" %>>
                                    <span class="pill-box"><%= taglia %></span>
                                </label>
                <%
                            }
                        }
                    }
                %>
                </div>
            </div>

            <%-- COLORI --%>
            <div class="filter-group">
                <h3 class="filter-title">Colore</h3>
                <div class="filter-pills-wrap">
                <%
                    if (prodottiSidebar != null) {
                        Set<String> coloriSet = new TreeSet<>();
                        for (ProdottoDTO prodotto : prodottiSidebar) {
                            if (prodotto.getColoriDisponibili() != null) {
                                coloriSet.addAll(prodotto.getColoriDisponibili());
                            }
                        }

                        // CONTROLLO SPOSTATO FUORI dal ciclo for
                        if (coloriSet.isEmpty()) {
                %>
                            <p class="no-colori-msg">Nessun colore disponibile.</p>
                <%
                        } else {
                            // Ciclo solo se ci sono colori da mostrare
                            for (String colore : coloriSet) {
                                boolean isChecked = filtri != null && filtri.getColori() != null && filtri.getColori().contains(colore);
                %>
                                <label class="custom-pill">
                                    <input type="checkbox" name="colori" value="<%= colore %>" <%= isChecked ? "checked" : "" %>>
                                    <span class="pill-box"><%= colore %></span>
                                </label>
                <%
                            }
                        }
                    }
                %>
                </div>
            </div>

            <%-- BOTTONI SUBMIT/RESET --%>
            <div class="filter-actions">
                <button type="submit" class="btn-primary">Applica Filtri</button>
                <%-- Modifica del link Reset per mantenere la stringa di ricerca se presente --%>
                <a href="${pageContext.request.contextPath}/catalogo<%= hasRicerca ? "?q=" + URLEncoder.encode(queryRicerca, "UTF-8") : "" %>" class="btn-secondary">Reset Filtri</a>
            </div>

        </form>
    </aside>

    <%-- Wrapper per isolare la griglia prodotti dai banner soprastanti --%>
    <div class="catalogo-content-area">

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
                <p class="no-products-msg">Nessun prodotto trovato con questi filtri di ricerca.</p>
            <% } %>
        </section>
        
    </div>

    <nav class="catalogo-pagination">
        <ul>
            <li class="pagination-item <%= paginaCorrente <= 1 ? "disabled" : "" %>">
                <button type="submit" form="filtriForm" name="page" value="<%= paginaCorrente - 1 %>" <%= paginaCorrente <= 1 ? "disabled" : "" %>>Precedente</button>
            </li>
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
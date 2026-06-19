<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="it.runitup.model.bean.Utente" %>
<%
    Boolean nascondiPromo = (Boolean) request.getAttribute("nascondiPromo");
    if (nascondiPromo == null || !nascondiPromo) {
%>
    <jsp:include page="/WEB-INF/view/fragments/promo_banner.jsp" />
<%
    }
    // Controllo dello stato di login
    Utente utenteLoggato = (Utente) session.getAttribute("utenteLoggato");
    boolean isLoggato = (utenteLoggato != null);
%>

<header class="main-header">
    <%-- HEADER TOP: Barra Principale --%>
    <div class="header-top">
        <%-- SINISTRA: Logo --%>
        <a href="${pageContext.request.contextPath}/" class="logo">
            <img src="${pageContext.request.contextPath}/images/common/logo-runitup.svg" alt="RunItUp Logo" class="logo-img">
        </a>

        <%-- CENTRO: Ricerca Avanzata AJAX (Solo Desktop) --%>
        <div class="search-wrapper desktop-only">
            <div class="search-bar">
                <form action="${pageContext.request.contextPath}/catalogo" method="GET" id="searchForm">
                    <input type="text" name="q" id="desktopSearchInput" placeholder="Cerca scarpe, brand, categorie..." autocomplete="off">
                    <button type="submit" class="search-btn">
                        <img src="${pageContext.request.contextPath}/images/common/search.svg" alt="Cerca" class="icon-svg">
                    </button>
                </form>
            </div>
            <div id="desktopSearchSuggestions" class="search-suggestions-box hidden" data-context="${pageContext.request.contextPath}"></div>
        </div>

        <%-- DESTRA: Azioni Utente, Carrello e Hamburger Mobile --%>
        <div class="header-actions">
            <%-- Icona Ricerca Mobile (Attiva overlay) --%>
            <button class="mobile-search-trigger search-btn mobile-only" id="mobileSearchOpenBtn" aria-label="Apri ricerca">
                <img src="${pageContext.request.contextPath}/images/common/search.svg" alt="Cerca" class="icon-svg">
            </button>

            <%-- Wrapper Profilo con Dropdown (Desktop) --%>
            <div class="profile-dropdown-wrapper desktop-only">
                <% if (!isLoggato) { %>
                    <a href="${pageContext.request.contextPath}/login" class="action-link text-link">
                        <img src="${pageContext.request.contextPath}/images/common/user.svg" alt="Accedi" class="icon-svg">
                        <span class="action-text">Accedi</span>
                    </a>
                <% } else { %>
                    <div class="action-link text-link content-hover">
                        <img src="${pageContext.request.contextPath}/images/common/user.svg" alt="Profilo" class="icon-svg">
                        <span class="action-text">Profilo</span>
                        <%-- Dropdown Desktop --%>
                        <div class="desktop-dropdown-menu">
                            <a href="${pageContext.request.contextPath}/area_personale">Dashboard</a>
                            <a href="${pageContext.request.contextPath}/ordini">I miei Ordini</a>
                            <a href="${pageContext.request.contextPath}/logout" class="logout-text">Logout</a>
                        </div>
                    </div>
                <% } %>
            </div>

            <%-- Icona Carrello --%>
            <a href="${pageContext.request.contextPath}/carrello" class="action-link cart-icon-wrapper" aria-label="Carrello">
                <img src="${pageContext.request.contextPath}/images/common/cart.svg" alt="Carrello" class="icon-svg cart-icon">
                
                <%-- Leggiamo il numero dalla sessione. Se è > 0, lo mostriamo, altrimenti nascondiamo il badge --%>
                <% 
                    Integer cartCount = (Integer) session.getAttribute("cartBadgeCount");
                    int countValue = (cartCount != null) ? cartCount : 0;
                %>
                <span id="cartBadge" class="cart-badge <%= (countValue == 0) ? "hidden" : "" %>">
                    <%= countValue %>
                </span>
            </a>

            <%-- Trigger Hamburger (Solo Mobile - Estrema Destra) --%>
            <button class="mobile-menu-toggle mobile-only" id="mobileMenuOpenBtn" aria-label="Apri menu">
                <img src="${pageContext.request.contextPath}/images/common/menu.svg" alt="Menu" class="icon-svg">
            </button>
        </div>
    </div>

    <%-- HEADER BOTTOM: Menu di Navigazione Distribuito (Solo Desktop) --%>
    <nav class="header-bottom desktop-only">
        <%-- Gruppo Categorie (Sinistra) --%>
        <div class="nav-group nav-categories">
            <a href="${pageContext.request.contextPath}/catalogo?genere=UOMO">Uomo</a>
            <a href="${pageContext.request.contextPath}/catalogo?genere=DONNA">Donna</a>
            <a href="${pageContext.request.contextPath}/catalogo?genere=UNISEX">Unisex</a>
        </div>

        <%-- Gruppo Highlights (Centro) --%>
        <div class="nav-group nav-highlights">
            <a href="${pageContext.request.contextPath}/catalogo?inOfferta=true" class="highlight-promo">Promozioni</a>
            <a href="${pageContext.request.contextPath}/catalogo?inEvidenza=true">In Evidenza</a>
            <a href="${pageContext.request.contextPath}/catalogo?nuoviArrivi=true">Nuovi Arrivi</a>
        </div>

        <%-- Gruppo Utilities (Destra) --%>
        <div class="nav-group nav-utilities">
            <a href="${pageContext.request.contextPath}/findmyshoes" class="btn-shoefinder">FindMyShoes</a>
            <div class="utility-dropdown">
                <span class="utility-trigger">Aiuto & Contatti</span>
                <div class="desktop-dropdown-menu">
                    <a href="${pageContext.request.contextPath}/assistenza">Assistenza</a>
                    <a href="${pageContext.request.contextPath}/chi-siamo">Chi Siamo</a>
                    <a href="${pageContext.request.contextPath}/faq">FAQ</a>
                </div>
            </div>
        </div>
    </nav>
</header>

<%-- MOBILE SEARCH OVERLAY (Cala dall'alto al click sulla lente mobile) --%>
<div class="mobile-search-overlay mobile-only" id="mobileSearchOverlay">
    <div class="mobile-search-container">
        <form action="${pageContext.request.contextPath}/catalogo" method="GET">
            <input type="text" name="q" id="mobileSearchInput" placeholder="Cerca prodotti..." autocomplete="off">
            <button type="submit" class="search-btn">
                <img src="${pageContext.request.contextPath}/images/common/search.svg" alt="Cerca" class="icon-svg">
            </button>
        </form>
        <button class="mobile-search-close" id="mobileSearchCloseBtn">Annulla</button>
    </div>
    <div id="mobileSearchSuggestions" class="search-suggestions-box hidden" data-context="${pageContext.request.contextPath}"></div>
</div>

<%-- MOBILE DRAWER: Cassetto Off-Canvas Destro --%>
<div class="mobile-drawer-overlay mobile-only" id="mobileDrawerOverlay"></div>
<aside class="mobile-navigation-drawer mobile-only" id="mobileNavigationDrawer">
    <div class="drawer-header">
        <span class="drawer-title">Menu</span>
        <button class="drawer-close-btn" id="mobileMenuCloseBtn" aria-label="Chiudi menu">
            <img src="${pageContext.request.contextPath}/images/common/close.svg" alt="Chiudi" class="icon-svg">
        </button>
    </div>

    <div class="drawer-content">
        <%-- Sezione Profilo Mobile --%>
        <div class="drawer-section drawer-profile-box">
            <% if (!isLoggato) { %>
                <a href="${pageContext.request.contextPath}/login" class="drawer-btn-login">Accedi / Registrati</a>
            <% } else { %>
                <div class="drawer-user-info">
                    <%-- Controllo sul nome dell'utente per evitare di mostrare un messaggio vuoto --%>
                    <% if (utenteLoggato.getNome() != null && !utenteLoggato.getNome().isEmpty()) { %>
                        <p class="drawer-welcome">Bentornato nel club, ${utenteLoggato.getNome()}!</p>
                    <% } else { %>
                        <p class="drawer-welcome">Bentornato nel club!</p>
                    <% } %>
                    <a href="${pageContext.request.contextPath}/area_personale" class="drawer-link">Il mio Profilo</a>
                    <a href="${pageContext.request.contextPath}/ordini" class="drawer-link">I miei Ordini</a>
                    <a href="${pageContext.request.contextPath}/logout" class="drawer-link text-danger">Logout</a>
                </div>
            <% } %>
        </div>

        <%-- Categorie --%>
        <div class="drawer-section">
            <p class="drawer-section-title">Collezioni</p>
            <nav class="drawer-links-list">
                <a href="${pageContext.request.contextPath}/catalogo?genere=UOMO">Uomo</a>
                <a href="${pageContext.request.contextPath}/catalogo?genere=DONNA">Donna</a>
                <a href="${pageContext.request.contextPath}/catalogo?genere=UNISEX">Unisex</a>
            </nav>
        </div>

        <%-- Highlights & Strumenti --%>
        <div class="drawer-section">
            <p class="drawer-section-title">In Evidenza</p>
            <nav class="drawer-links-list">
                <a href="${pageContext.request.contextPath}/findmyshoes" class="drawer-highlight-finder">Trova la tua Scarpa (FindMyShoes)</a>
                <a href="${pageContext.request.contextPath}/catalogo?inOfferta=true" class="text-danger">Promozioni</a>
                <a href="${pageContext.request.contextPath}/catalogo?inEvidenza=true">In Evidenza</a>
                <a href="${pageContext.request.contextPath}/catalogo?nuoviArrivi=true">Nuovi Arrivi</a>
            </nav>
        </div>

        <%-- Supporto --%>
        <div class="drawer-section">
            <p class="drawer-section-title">Supporto</p>
            <nav class="drawer-links-list">
                <a href="${pageContext.request.contextPath}/assistenza">Assistenza Clienti</a>
                <a href="${pageContext.request.contextPath}/chi-siamo">Chi Siamo</a>
                <a href="${pageContext.request.contextPath}/faq">Domande Frequenti</a>
            </nav>
        </div>
    </div>
</aside>

<script src="${pageContext.request.contextPath}/js/header.js" defer></script>
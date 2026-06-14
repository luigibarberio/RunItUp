<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    Boolean nascondiPromo = (Boolean) request.getAttribute("nascondiPromo");
    if (nascondiPromo == null || !nascondiPromo) {
%>
    <jsp:include page="/WEB-INF/view/fragments/promo_banner.jsp" />
<%
    }
%>

<header class="main-header">
    <div class="header-top">
        <button class="mobile-menu-btn" aria-label="Menu">
            &#9776;
        </button>

        <a href="${pageContext.request.contextPath}/" class="logo">
            <span class="logo-text">@ RunItUp</span>
        </a>

        <div class="search-bar desktop-only">
            <form action="${pageContext.request.contextPath}/search" method="GET">
                <input type="text" name="q" placeholder="Cerca i prodotti...">
                <button type="submit" class="search-btn">&#128269;</button>
            </form>
        </div>

        <div class="header-actions">
            <button class="mobile-search-btn mobile-only" aria-label="Cerca">
                &#128269;
            </button>
            <a href="${pageContext.request.contextPath}/login" aria-label="Accedi">
                &#128100;
            </a>
            <a href="${pageContext.request.contextPath}/carrello" aria-label="Carrello">
                &#128722;
            </a>
        </div>
    </div>

    <nav class="header-bottom desktop-only">
        <ul class="nav-links">
            <li><a href="${pageContext.request.contextPath}/categoria/uomo">Uomo</a></li>
            <li><a href="${pageContext.request.contextPath}/categoria/donna">Donna</a></li>
            <li><a href="${pageContext.request.contextPath}/categoria/bambino">Bambino</a></li>
            <li><a href="${pageContext.request.contextPath}/promozioni" class="text-promo">Promozioni</a></li>
        </ul>
        <ul class="nav-links-right">
            <li><a href="${pageContext.request.contextPath}/assistenza">Assistenza</a></li>
        </ul>
    </nav>
</header>

<script src="${pageContext.request.contextPath}/js/header.js" defer></script>
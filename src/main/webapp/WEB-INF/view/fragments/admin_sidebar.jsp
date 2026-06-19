<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // Legge l'attributo impostato dalla Servlet, se nullo usa "dashboard"
    String activePage = (String) request.getAttribute("activePage");
    if (activePage == null) activePage = "prodotti"; // Default
%>

<aside class="admin-sidebar">
    <!-- Header Sidebar: Logo e Titolo -->
    <div class="sidebar-header">
        <a href="${pageContext.request.contextPath}/admin" class="sidebar-logo">
            <img src="${pageContext.request.contextPath}/images/common/icons/logo-runitup.svg" alt="RunItUp">
            <span>Admin Panel</span>
        </a>
    </div>

    <!-- Navigazione -->
    <nav class="sidebar-nav">
        <p class="sidebar-nav-title">Menu Principale</p>
        <ul class="nav-list">
            <li>
                <a href="${pageContext.request.contextPath}/admin/prodotti" class="nav-link <%= activePage.equals("prodotti") ? "active" : "" %>">
                    <img src="${pageContext.request.contextPath}/images/common/icons/products.svg" alt="" class="nav-icon" onerror="this.style.display='none'">
                    Gestione Prodotti
                </a>
            </li>
            <li>
                <a href="${pageContext.request.contextPath}/admin/ordini" class="nav-link <%= activePage.equals("ordini") ? "active" : "" %>">
                    <img src="${pageContext.request.contextPath}/images/common/icons/orders.svg" alt="" class="nav-icon" onerror="this.style.display='none'">
                    Gestione Ordini
                </a>
            </li>
        </ul>

        <p class="sidebar-nav-title">Sistema</p>
        <ul class="nav-list">
            <li>
                <a href="${pageContext.request.contextPath}/" class="nav-link">
                    <img src="${pageContext.request.contextPath}/images/common/icons/store.svg" alt="" class="nav-icon" onerror="this.style.display='none'">
                    Torna al Sito
                </a>
            </li>
            <li>
                <a href="${pageContext.request.contextPath}/logout" class="nav-link logout-link">
                    <img src="${pageContext.request.contextPath}/images/common/icons/logout.svg" alt="" class="nav-icon" onerror="this.style.display='none'">
                    Esci
                </a>
            </li>
        </ul>
    </nav>
</aside>
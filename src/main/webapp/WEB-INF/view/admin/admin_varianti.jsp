<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="it.runitup.model.bean.Prodotto" %>
<%@ page import="it.runitup.model.bean.VarianteProdotto" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>

<%
    Prodotto prodotto = (Prodotto) request.getAttribute("prodotto");
    @SuppressWarnings("unchecked")
    List<VarianteProdotto> varianti = (List<VarianteProdotto>) request.getAttribute("varianti");
    
    NumberFormat valutaFormat = NumberFormat.getCurrencyInstance(Locale.ITALY);
    String feedbackMsg = request.getParameter("msg"); // Per intercettare il redirect post-creazione
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Varianti: <%= prodotto.getNome() %> - Admin</title>
    
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/images/common/favicon.png">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/toast.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin_layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin_varianti.css">
</head>
<body>

    <div class="admin-layout-wrapper">
        <jsp:include page="/WEB-INF/view/fragments/admin_sidebar.jsp" />

        <main class="admin-main-content">
            
            <%-- ZONA 1: MASTER (Info Prodotto) --%>
            <div class="variants-header">
                <div class="product-context">
                    <img src="${pageContext.request.contextPath}/images/prodotti?id=<%= prodotto.getIdProdotto() %>&index=0" 
                         alt="Prodotto" onerror="this.src='${pageContext.request.contextPath}/images/placeholder.svg'" class="master-img">
                    <div>
                        <a href="${pageContext.request.contextPath}/admin/prodotti" class="back-link">&larr; Torna al Catalogo</a>
                        <h1 class="master-title"><%= prodotto.getNome() %></h1>
                        <p class="master-subtitle">ID: #<%= prodotto.getIdProdotto() %> &bull; Brand: <%= prodotto.getBrand().getNomeBrand() %></p>
                    </div>
                </div>
            </div>

            <%-- ZONA 2: AGGIUNTA RAPIDA (Form Orizzontale) --%>
            <div class="add-variant-bar">
                <form id="addVariantForm" class="add-variant-form">
                    <input type="hidden" id="add_idProdotto" value="<%= prodotto.getIdProdotto() %>">
                    
                    <div class="form-group-inline">
                        <label>Colore</label>
                        <input type="text" id="add_colore" required placeholder="es. Rosso/Nero">
                    </div>
                    <div class="form-group-inline">
                        <label>Taglia</label>
                        <input type="text" id="add_taglia" required placeholder="es. 42">
                    </div>
                    <div class="form-group-inline">
                        <label>Prezzo (€)</label>
                        <input type="number" id="add_prezzo" step="0.01" min="0" required placeholder="120.00">
                    </div>
                    <div class="form-group-inline">
                        <label>Giacenza</label>
                        <input type="number" id="add_quantita" min="0" required placeholder="10">
                    </div>
                    <button type="submit" class="btn-primary btn-add" id="btnAddSubmit">+ Aggiungi</button>
                </form>
            </div>

            <%-- ZONA 3: DETAIL GRIGLIA (Lista Varianti Esistenti) --%>
            <div class="variants-grid" id="variantsGrid">
                <% if (varianti != null && !varianti.isEmpty()) { %>
                    <% for (VarianteProdotto v : varianti) { %>
                        <%-- SINGOLA CARD VARIANTE --%>
                        <div class="variant-card" id="variante-<%= v.getIdVariante() %>" data-id="<%= v.getIdVariante() %>">
                            
                            <%-- MODALITÀ LETTURA (Visibile di default) --%>
                            <div class="variant-view-mode">
                                <div class="variant-data">
                                    <div class="v-badge"><span class="v-colore"><%= v.getColore() %></span> &bull; Tg. <strong class="v-taglia"><%= v.getTaglia() %></strong></div>
                                    <div class="v-price">€ <span class="v-prezzo"><%= v.getPrezzo() %></span></div>
                                    <div class="v-stock">Stock: <strong class="v-quantita"><%= v.getQuantitaMagazzino() %></strong> pz.</div>
                                </div>
                                <div class="variant-actions">
                                    <button class="btn-icon btn-edit" title="Modifica" onclick="toggleEditMode(<%= v.getIdVariante() %>, true)">✏️</button>
                                    <button class="btn-icon btn-delete" title="Elimina" onclick="deleteVariante(<%= v.getIdVariante() %>)">🗑️</button>
                                </div>
                            </div>

                            <%-- MODALITÀ MODIFICA (Nascosta di default) --%>
                            <div class="variant-edit-mode hidden">
                                <div class="edit-fields">
                                    <input type="text" class="edit-colore" value="<%= v.getColore() %>" placeholder="Colore">
                                    <input type="text" class="edit-taglia" value="<%= v.getTaglia() %>" placeholder="Taglia">
                                    <input type="number" step="0.01" class="edit-prezzo" value="<%= v.getPrezzo() %>" placeholder="Prezzo">
                                    <input type="number" class="edit-quantita" value="<%= v.getQuantitaMagazzino() %>" placeholder="Q.tà">
                                </div>
                                <div class="variant-actions">
                                    <button class="btn-icon btn-save" title="Salva" onclick="updateVariante(<%= v.getIdVariante() %>)">💾</button>
                                    <button class="btn-icon btn-cancel" title="Annulla" onclick="toggleEditMode(<%= v.getIdVariante() %>, false)">✖️</button>
                                </div>
                            </div>
                            
                        </div>
                    <% } %>
                <% } else { %>
                    <div class="empty-variants" id="emptyStateBox">
                        <p>Nessuna variante inserita. Aggiungi la prima taglia dal pannello superiore!</p>
                    </div>
                <% } %>
            </div>

        </main>
    </div>

    <%-- IL TEMPLATE HTML5 PER LA CREAZIONE DINAMICA IN JAVASCRIPT --%>
    <template id="variant-card-template">
        <div class="variant-card" id="variante-{ID}" data-id="{ID}">
            <div class="variant-view-mode">
                <div class="variant-data">
                    <div class="v-badge"><span class="v-colore">{COLORE}</span> &bull; Tg. <strong class="v-taglia">{TAGLIA}</strong></div>
                    <div class="v-price">€ <span class="v-prezzo">{PREZZO}</span></div>
                    <div class="v-stock">Stock: <strong class="v-quantita">{QUANTITA}</strong> pz.</div>
                </div>
                <div class="variant-actions">
                    <button class="btn-icon btn-edit" title="Modifica" onclick="toggleEditMode({ID}, true)">✏️</button>
                    <button class="btn-icon btn-delete" title="Elimina" onclick="deleteVariante({ID})">🗑️</button>
                </div>
            </div>
            <div class="variant-edit-mode hidden">
                <div class="edit-fields">
                    <input type="text" class="edit-colore" value="{COLORE}">
                    <input type="text" class="edit-taglia" value="{TAGLIA}">
                    <input type="number" step="0.01" class="edit-prezzo" value="{PREZZO}">
                    <input type="number" class="edit-quantita" value="{QUANTITA}">
                </div>
                <div class="variant-actions">
                    <button class="btn-icon btn-save" title="Salva" onclick="updateVariante({ID})">💾</button>
                    <button class="btn-icon btn-cancel" title="Annulla" onclick="toggleEditMode({ID}, false)">✖️</button>
                </div>
            </div>
        </div>
    </template>

    <%-- Feedback dal redirect di creazione prodotto base --%>
    <% if ("created".equals(feedbackMsg)) { %>
        <script>window.addEventListener('DOMContentLoaded', () => { if(window.showToast) window.showToast("Prodotto creato! Ora aggiungi le varianti.", "success"); });</script>
    <% } %>

    <jsp:include page="/WEB-INF/view/fragments/toast.jsp" />
    <script src="${pageContext.request.contextPath}/js/toast.js"></script>
    <script src="${pageContext.request.contextPath}/js/admin_varianti.js"></script>
</body>
</html>
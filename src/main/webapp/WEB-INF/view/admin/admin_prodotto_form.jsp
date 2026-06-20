<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="it.runitup.model.bean.Prodotto" %>
<%@ page import="it.runitup.model.bean.Brand" %>
<%@ page import="java.util.List" %>

<%
    // Recupero attributi dalla Servlet
    String modalita = (String) request.getAttribute("modalita");
    Prodotto p = (Prodotto) request.getAttribute("prodottoForm");
    
    // Flag logico per semplificare gli if nell'HTML
    boolean isModifica = "MODIFICA".equals(modalita) && p != null;

    // Estrazione sicura dei valori (per evitare NullPointerException se è Inserimento)
    String nomeVal = isModifica && p.getNome() != null ? p.getNome() : "";
    String descVal = isModifica && p.getDescrizione() != null ? p.getDescrizione() : "";
    String genereVal = isModifica && p.getGenere() != null ? p.getGenere().name() : "";
    String terrenoVal = isModifica && p.getTipoTerreno() != null ? p.getTipoTerreno().name() : "";
    String appoggioVal = isModifica && p.getTipoAppoggio() != null ? p.getTipoAppoggio().name() : "";
    String passoVal = isModifica && p.getTipoPasso() != null ? p.getTipoPasso().name() : "";
    boolean inEvidenzaVal = isModifica && p.isInEvidenza();

    // Recupero della lista dei brand dalla Servlet
    List<Brand> listaBrand = (List<it.runitup.model.bean.Brand>) request.getAttribute("listaBrand");
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= isModifica ? "Modifica Prodotto" : "Nuovo Prodotto" %> - Admin</title>
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/images/common/favicon.png">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin_layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin_prodotto_form.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/toast.css">
</head>
<body>
    <div class="admin-layout-wrapper">
        <jsp:include page="/WEB-INF/view/fragments/admin_sidebar.jsp" />

        <main class="admin-main-content">
            <div class="admin-form-container">
                
                <div class="admin-form-panel">
                    <div class="form-header">
                        <h1 class="form-title">
                            <%= isModifica ? "Modifica Modello #" + p.getIdProdotto() : "Aggiungi Nuovo Prodotto" %>
                        </h1>
                        <p>
                            <%= isModifica ? "Aggiorna le informazioni di base della scarpa." : "Inserisci i dati base. Potrai aggiungere le varianti nel prossimo step." %>
                        </p>
                    </div>

                    <form id="productForm" action="${pageContext.request.contextPath}/admin/prodotto/gestione" method="POST" enctype="multipart/form-data">
                        
                        <%-- CAMPO NASCOSTO: Contiene il messaggio di errore del backend da mostrare con il toast in javascript --%>
                        <% if (request.getAttribute("erroreMessage") != null) { %>
                            <input type="hidden" id="backendErrorMsg" value="<%= ((String)request.getAttribute("erroreMessage")).replace("\"", "&quot;") %>">
                        <% } %>

                        <%-- CAMPO NASCOSTO: La Servlet capisce che è un UPDATE solo se questo campo esiste --%>
                        <% if (isModifica) { %>
                            <input type="hidden" id="idProdotto" name="idProdotto" value="<%= p.getIdProdotto() %>">
                        <% } %>
                        
                        <div class="form-group">
                            <label for="nome">Nome Modello *</label>
                            <input type="text" id="nome" name="nome" class="form-control" value="<%= nomeVal %>" placeholder="es. Nike Pegasus 39">
                            <span id="nomeError" class="error-text"></span>
                        </div>

                        <div class="form-group">
                            <label for="descrizione">Descrizione</label>
                            <textarea id="descrizione" name="descrizione" class="form-control" placeholder="Descrivi la scarpa..."><%= descVal %></textarea>
                        </div>

                        <div class="form-row-2col">
                            <div class="form-group">
                                <label for="nomeBrand">Brand *</label>
                                <datalist id="brandList">
                                    <option value="">Seleziona...</option>
                                    <% for (Brand brand : listaBrand) { %>
                                        <option value="<%= brand.getNomeBrand() %>">
                                        </option>
                                    <% } %>
                                </datalist>
                                <% 
                                    // Recupero il nome del brand selezionato per precompilare il campo
                                    String brandVal = "";
                                    if (isModifica && p.getBrand() != null) {
                                        brandVal = p.getBrand().getNomeBrand();
                                    }
                                %>
                                <input type="text" id="nomeBrand" name="nomeBrand" class="form-control" list="brandList" value="<%= brandVal %>" placeholder="es. Nike">
                                <span id="brandError" class="error-text"></span>
                            </div>
                            <div class="form-group">
                                <label for="genere">Genere *</label>
                                <select id="genere" name="genere" class="form-control">
                                    <option value="UOMO" <%= "UOMO".equals(genereVal) ? "selected" : "" %>>Uomo</option>
                                    <option value="DONNA" <%= "DONNA".equals(genereVal) ? "selected" : "" %>>Donna</option>
                                    <option value="UNISEX" <%= "UNISEX".equals(genereVal) ? "selected" : "" %>>Unisex</option>
                                </select>
                            </div>
                        </div>

                        <div class="form-row-2col">
                            <div class="form-group">
                                <label for="tipoTerreno">Tipo Terreno</label>
                                <select id="tipoTerreno" name="tipoTerreno" class="form-control">
                                    <option value="STRADA" <%= "STRADA".equals(terrenoVal) ? "selected" : "" %>>Strada / Asfalto</option>
                                    <option value="STERRATO" <%= "STERRATO".equals(terrenoVal) ? "selected" : "" %>>Sterrato / Trail</option>
                                </select>
                            </div>
                            <div class="form-group">
                                <label for="tipoAppoggio">Appoggio Plantare</label>
                                <select id="tipoAppoggio" name="tipoAppoggio" class="form-control">
                                    <option value="NEUTRO" <%= "NEUTRO".equals(appoggioVal) ? "selected" : "" %>>Neutro</option>
                                    <option value="PRONATORE" <%= "PRONATORE".equals(appoggioVal) ? "selected" : "" %>>Pronatore</option>
                                    <option value="SUPINATORE" <%= "SUPINATORE".equals(appoggioVal) ? "selected" : "" %>>Supinatore</option>
                                </select>
                            </div>
                        </div>

                        <div class="form-row-2col">
                            <div class="form-group">
                                <label for="tipoPasso">Ritmo di Corsa</label>
                                <select id="tipoPasso" name="tipoPasso" class="form-control">
                                    <option value="LENTO" <%= "LENTO".equals(passoVal) ? "selected" : "" %>>Lento (> 5:00 min/km)</option>
                                    <option value="MEDIO" <%= "MEDIO".equals(passoVal) ? "selected" : "" %>>Medio (4:00 - 5:00 min/km)</option>
                                    <option value="VELOCE" <%= "VELOCE".equals(passoVal) ? "selected" : "" %>>Veloce (< 4:00 min/km)</option>
                                </select>
                            </div>
                            <div class="form-group" style="justify-content: center;">
                                <label style="display: flex; align-items: center; gap: 10px; cursor: pointer; margin-top: 15px;">
                                    <input type="checkbox" id="inEvidenza" name="inEvidenza" style="width: 20px; height: 20px;" <%= inEvidenzaVal ? "checked" : "" %>>
                                    Mostra in Evidenza nella Home Page
                                </label>
                            </div>
                        </div>

                        <div class="form-row-2col">
                            <div class="form-group file-upload-box">
                                <label for="immagine_0"><%= isModifica ? "Sostituisci Immagine Primaria" : "Immagine Primaria" %></label>
                                <input type="file" id="immagine_0" name="immagine_0" accept="image/png, image/jpeg">
                                <span id="img0Error" class="error-text"></span>
                                <% if (isModifica) { %> <p style="font-size:0.75rem; color:#64748b; margin-top:5px;">(Lascia vuoto per mantenere la foto attuale)</p> <% } %>
                            </div>
                            <div class="form-group file-upload-box">
                                <label for="immagine_1"><%= isModifica ? "Sostituisci Immagine Secondaria" : "Immagine Secondaria (Hover)" %></label>
                                <input type="file" id="immagine_1" name="immagine_1" accept="image/png, image/jpeg">
                                <span id="img1Error" class="error-text"></span>
                                <% if (isModifica) { %> <p style="font-size:0.75rem; color:#64748b; margin-top:5px;">(Lascia vuoto per mantenere la foto attuale)</p> <% } %>
                            </div>
                        </div>

                        <div class="form-actions">
                            <a href="${pageContext.request.contextPath}/admin/prodotti" class="btn-secondary">Annulla</a>
                            <button type="submit" id="btnSalva" class="btn-primary">
                                <%= isModifica ? "Aggiorna Prodotto" : "Salva e Procedi alle Varianti &rarr;" %>
                            </button>
                        </div>

                    </form>
                </div>
            </div>
        </main>
    </div>

    <jsp:include page="/WEB-INF/view/fragments/toast.jsp" />

    <script src="${pageContext.request.contextPath}/js/toast.js"></script>
    <script src="${pageContext.request.contextPath}/js/formUtils.js"></script>
    <script src="${pageContext.request.contextPath}/js/admin_prodotto_form.js"></script>
</body>
</html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="it.runitup.model.bean.Utente" %>

<%
    // Recupero dell'utente loggato dalla sessione 
    Utente utente = (Utente) session.getAttribute("utenteLoggato");
    
    String telefonoDb = (utente.getTelefono() != null) ? utente.getTelefono().trim() : "";
    String prefissoSelezionato = "+39"; // Default
    String numeroTelefono = "";

    // Se il telefono è presente e contiene lo spazio di separazione, lo scompone
    if (!telefonoDb.isEmpty() && telefonoDb.contains(" ")) {
        String[] parti = telefonoDb.split(" ");
        prefissoSelezionato = parti[0];
        numeroTelefono = parti[1];
    } else {
        // Fallback di sicurezza in caso di record senza spazio (es. vecchi test)
        numeroTelefono = telefonoDb;
    }
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Modifica Profilo - RunItUp</title>
    
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/header.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/footer.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/modifica_profilo.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/toast.css">
</head>
<body>

    <jsp:include page="/WEB-INF/view/fragments/header.jsp" />

    <main class="modifica-main">
        <div class="modifica-card">
            
            <div class="modifica-header">
                <h2>Impostazioni Profilo</h2>
                <p>Modifica le tue informazioni personali o la password di accesso.</p>
            </div>

            <form id="editProfileForm" novalidate>
                
                <h3 class="section-title">Dati Anagrafici</h3>
                
                <div class="input-group">
                    <label for="email">Indirizzo Email (Non modificabile)</label>
                    <input type="email" id="email" value="<%= utente.getEmail() %>" disabled class="input-disabled">
                </div>

                <div class="input-row">
                    <div class="input-group">
                        <label for="nome">Nome</label>
                        <%-- Usiamo data-initial per far capire al JS qual era il valore originale senza variabili globali --%>
                        <input type="text" id="nome" name="nome" value="<%= utente.getNome() %>" data-initial="<%= utente.getNome() %>" required maxlength="50">
                        <span class="error-msg" id="nomeError"></span>
                    </div>
                    
                    <div class="input-group">
                        <label for="cognome">Cognome</label>
                        <input type="text" id="cognome" name="cognome" value="<%= utente.getCognome() %>" data-initial="<%= utente.getCognome() %>" required maxlength="50">
                        <span class="error-msg" id="cognomeError"></span>
                    </div>
                </div>

                <div class="input-group">
                    <label for="telefono">Numero di Telefono (Opzionale)</label>
                    <div class="phone-input-wrapper">
                        <select name="prefisso" id="prefisso" class="phone-prefix" data-initial="<%= prefissoSelezionato %>">
                            <option value="+39" <%= prefissoSelezionato.equals("+39") ? "selected" : "" %>>+39</option>
                        </select>
                        <%-- Salviamo il numero puro nel data-initial per i futuri controlli del JS --%>
                        <input type="tel" id="telefono" name="telefono" value="<%= numeroTelefono %>" data-initial="<%= numeroTelefono %>" placeholder="3331234567" maxlength="10">
                    </div>
                    <span class="error-msg" id="telefonoError"></span>
                </div>

                <h3 class="section-title margin-top-section">Cambio Password</h3>
                <p class="section-subtitle">Lascia questi campi vuoti se non desideri modificare la password.</p>

                <div class="input-group">
                    <label for="passwordAttuale">Password Attuale</label>
                    <input type="password" id="passwordAttuale" name="passwordAttuale" placeholder="Inserisci la password corrente">
                    <span class="error-msg" id="passwordAttualeError"></span>
                </div>

                <div class="input-row">
                    <div class="input-group">
                        <label for="nuovaPassword">Nuova Password</label>
                        <input type="password" id="nuovaPassword" name="nuovaPassword" placeholder="Almeno 8 caratteri">
                        <span class="error-msg" id="nuovaPasswordError"></span>
                    </div>
                    
                    <div class="input-group">
                        <label for="confermaPassword">Conferma Nuova Password</label>
                        <input type="password" id="confermaPassword" name="confermaPassword" placeholder="Ripeti la nuova password">
                        <span class="error-msg" id="confermaPasswordError"></span>
                    </div>
                </div>

                <div class="form-actions">
                    <a href="${pageContext.request.contextPath}/area_personale" class="btn-secondary">Annulla</a>
                    <button type="submit" class="btn-primary modifica-btn" id="saveBtn" disabled>
                        <span class="btn-text">Applica Modifiche</span>
                    </button>
                </div>
            </form>

            <div id="successOverlay" class="success-overlay hidden">
                <div class="success-content">
                    <div class="check-icon">✔</div>
                    <h3>Profilo Aggiornato!</h3>
                    <p>Le tue modifiche sono state salvate con successo.</p>
                </div>
            </div>
            
        </div>
    </main>

    <jsp:include page="/WEB-INF/view/fragments/footer.jsp" />
    <jsp:include page="/WEB-INF/view/fragments/toast.jsp" />

    <script src="${pageContext.request.contextPath}/js/toast.js"></script>
    <script src="${pageContext.request.contextPath}/js/formUtils.js"></script>
    <script src="${pageContext.request.contextPath}/js/modifica_profilo.js"></script>

</body>
</html>
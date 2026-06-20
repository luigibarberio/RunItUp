<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("nascondiPromo", Boolean.TRUE); %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Registrati - RunItUp</title>
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/images/common/favicon.png">
    
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/header.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/footer.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/registrazione.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/toast.css">
</head>
<body>

    <jsp:include page="/WEB-INF/view/fragments/header.jsp" />

    <main class="login-main">
        <div class="login-card register-card">
            <div class="login-header">
                <h2>Crea un Account</h2>
                <p>Unisciti alla community di RunItUp.</p>
            </div>

            <form id="registerForm" novalidate>
                
                <div class="input-row">
                    <div class="input-group">
                        <label for="nome">Nome</label>
                        <input type="text" id="nome" name="nome" placeholder="Mario" required maxlength="50">
                        <span class="error-msg" id="nomeError"></span>
                    </div>
                    <div class="input-group">
                        <label for="cognome">Cognome</label>
                        <input type="text" id="cognome" name="cognome" placeholder="Rossi" required maxlength="50">
                        <span class="error-msg" id="cognomeError"></span>
                    </div>
                </div>

                <div class="input-group">
                    <label for="email">Email</label>
                    <input type="email" id="email" name="email" placeholder="es. mario.rossi@email.com" required maxlength="100">
                    <span class="error-msg" id="emailError"></span>
                </div>

                <div class="input-group">
                    <label for="telefono">Telefono (Opzionale)</label>
                    <div class="phone-input-wrapper">
                        <select name="prefisso" id="prefisso" class="phone-prefix">
                            <option value="+39">+39</option>
                        </select>
                        <input type="tel" id="telefono" name="telefono" placeholder="3331234567" maxlength="10">
                    </div>
                    <span class="error-msg" id="telefonoError"></span>
                </div>

                <div class="input-row">
                    <div class="input-group">
                        <label for="password">Password</label>
                        <input type="password" id="password" name="password" placeholder="Min. 8 caratteri" required maxlength="50">
                        <span class="error-msg" id="passwordError"></span>
                    </div>
                    <div class="input-group">
                        <label for="confermaPassword">Conferma Password</label>
                        <input type="password" id="confermaPassword" name="confermaPassword" placeholder="Ripeti password" required maxlength="50">
                        <span class="error-msg" id="confermaPasswordError"></span>
                    </div>
                </div>

                <button type="submit" class="btn-primary login-btn" id="registerBtn" disabled>
                    <span class="btn-text">Registrati</span>
                </button>
            </form>

            <div class="login-footer">
                <p>Hai già un account? <a href="${pageContext.request.contextPath}/login">Accedi</a></p>
            </div>

            <div id="successOverlay" class="success-overlay hidden">
                <div class="success-content">
                    <div class="check-icon">✔</div>
                    <h3>Registrazione Completata!</h3>
                    <p>Ti stiamo reindirizzando al Login...</p>
                </div>
            </div>
        </div>
    </main>

    <jsp:include page="/WEB-INF/view/fragments/footer.jsp" />
    <jsp:include page="/WEB-INF/view/fragments/toast.jsp" />

    <script src="${pageContext.request.contextPath}/js/toast.js"></script>
    <script src="${pageContext.request.contextPath}/js/formUtils.js"></script>
    <script src="${pageContext.request.contextPath}/js/registrazione.js"></script>
</body>
</html>
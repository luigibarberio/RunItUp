<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%-- Banner promozionale nascosto --%>
<% request.setAttribute("nascondiPromo", Boolean.TRUE); %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Accedi - RunItUp</title>
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/images/common/favicon.png">
    
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/header.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/footer.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/login.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/toast.css">
</head>
<body>

    <jsp:include page="/WEB-INF/view/fragments/header.jsp" />

    <main class="login-main">
        <div class="login-card">
            <div class="login-header">
                <h2>Bentornato</h2>
                <p>Accedi al tuo account RunItUp per continuare.</p>
            </div>

            <%-- Form muto (novalidate disabilita i popup brutti del browser per usare i nostri) --%>
            <form id="loginForm" novalidate>
                <div class="input-group">
                    <label for="email">Email</label>
                    <input type="email" id="email" name="email" placeholder="es. mario.rossi@email.com" required maxlength="100">
                    <span class="error-msg" id="emailError"></span>
                </div>

                <div class="input-group">
                    <label for="password">Password</label>
                    <div class="password-wrapper">
                        <input type="password" id="password" name="password" placeholder="Inserisci la tua password" required maxlength="50">
                        <button type="button" class="toggle-password" aria-label="Mostra/Nascondi password">
                            <img src="${pageContext.request.contextPath}/images/common/icons/password-hidden.svg" alt="Mostra password" class="eye-icon">
                        </button>
                    </div>
                    <span class="error-msg" id="passwordError"></span>
                </div>

                <div class="form-actions">
                    <a href="${pageContext.request.contextPath}/recupero-password" class="forgot-pwd">Password dimenticata?</a>
                </div>

                <button type="submit" class="btn-primary login-btn">
                    <span class="btn-text">Accedi</span>
                </button>
            </form>

            <div class="login-footer">
                <p>Non hai un account? <a href="${pageContext.request.contextPath}/registrazione">Registrati ora</a></p>
            </div>
        </div>
        
        <div id="successOverlay" class="success-overlay hidden">
            <div class="success-content">
                <div class="check-icon">✔</div>
                <h3 id="overlayTitle">Accesso Eseguito!</h3>
                <p id="overlayText">Benvenuto su RunItUp, ti stiamo reindirizzando...</p>
            </div>
        </div>
    </main>

    <jsp:include page="/WEB-INF/view/fragments/footer.jsp" />

    <%-- Placeholder per il Toast --%>
    <jsp:include page="/WEB-INF/view/fragments/toast.jsp" />
    
    <script src="${pageContext.request.contextPath}/js/toast.js"></script>
    <script src="${pageContext.request.contextPath}/js/formUtils.js"></script>
    <script src="${pageContext.request.contextPath}/js/login.js"></script>
</body>
</html>
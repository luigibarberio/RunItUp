<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>Accesso Negato - RunItUp</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/images/common/favicon-grayscale.png">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/header.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/footer.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/error.css">
</head>
<body>
    <jsp:include page="/WEB-INF/view/fragments/header.jsp" />
    <main class="error-main">
        <div class="error-container">
            <div class="error-emoji">🛑</div>
            <h1 class="error-code">Errore 403</h1>
            <h2 class="error-title">Area Riservata!</h2>
            <p class="error-message">Non hai i permessi per accedere a questa sezione. Se pensi sia un errore, prova a effettuare nuovamente il login.</p>
            <a href="${pageContext.request.contextPath}/login" class="btn-primary">Vai al Login</a>
        </div>
    </main>
    <jsp:include page="/WEB-INF/view/fragments/footer.jsp" />
</body>
</html>
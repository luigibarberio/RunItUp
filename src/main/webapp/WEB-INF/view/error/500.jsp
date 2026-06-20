<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>Errore del Server - RunItUp</title>

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
            <div class="error-emoji">🩹</div>
            <h1 class="error-code">Errore 500</h1>
            <h2 class="error-title">Ahia! Abbiamo preso una storta.</h2>
            <p class="error-message">I nostri server hanno incontrato un problema tecnico improvviso. Stiamo facendo stretching e torneremo subito operativi!</p>
            <a href="${pageContext.request.contextPath}/" class="btn-primary">Riprova dalla Home</a>
        </div>
    </main>
    <jsp:include page="/WEB-INF/view/fragments/footer.jsp" />
</body>
</html>
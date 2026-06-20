<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Trova la tua scarpa - RunItUp</title>

    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/images/common/favicon.png">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/header.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/footer.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/findmyshoes.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/toast.css">
</head>
<body>

    <%-- Inclusione dell'Header Pubblico --%>
    <jsp:include page="/WEB-INF/view/fragments/header.jsp" />

    <main class="finder-main">
        <div class="finder-container">

            <%-- Intestazione del Quiz e Barra di Progresso --%>
            <div class="finder-header">
                <h1>Shoe Finder</h1>
                <p>Rispondi a 4 semplici domande e scopri i modelli perfetti per il tuo stile di corsa.</p>
                
                <div class="progress-container">
                    <div class="progress-bar" id="quizProgress" style="width: 25%;"></div>
                </div>
            </div>

            <%-- IL FORM MULTI-STEP --%>
            <form id="shoeQuizForm" action="${pageContext.request.contextPath}/findmyshoes" method="GET" class="quiz-form">
                
                <%-- ================= STEP 1: GENERE ================= --%>
                <div class="quiz-step active-step" id="step-1" data-step="1">
                    <h2>1. Per chi stiamo cercando?</h2>
                    
                    <div class="options-grid grid-3">
                        <label class="quiz-option">
                            <input type="radio" name="genere" value="UOMO" required>
                            <div class="option-card">
                                <span class="option-emoji">👨🏻</span>
                                <span class="option-label">Uomo</span>
                            </div>
                        </label>
                        <label class="quiz-option">
                            <input type="radio" name="genere" value="DONNA">
                            <div class="option-card">
                                <span class="option-emoji">👩🏻</span>
                                <span class="option-label">Donna</span>
                            </div>
                        </label>
                        <label class="quiz-option">
                            <input type="radio" name="genere" value="ANY">
                            <div class="option-card">
                                <span class="option-emoji">👟</span>
                                <span class="option-label">Nessuno in particolare</span>
                            </div>
                        </label>
                    </div>

                    <div class="step-actions right-align">
                        <button type="button" class="btn-primary btn-next">Continua &rarr;</button>
                    </div>
                </div>

                <%-- ================= STEP 2: TERRENO ================= --%>
                <div class="quiz-step hidden-step-right" id="step-2" data-step="2">
                    <h2>2. Dove corri di solito?</h2>
                    
                    <div class="options-grid grid-3">
                        <label class="quiz-option">
                            <input type="radio" name="terreno" value="STRADA" required>
                            <div class="option-card">
                                <span class="option-emoji">🛣️</span>
                                <span class="option-label">Strada / Asfalto</span>
                            </div>
                        </label>
                        <label class="quiz-option">
                            <input type="radio" name="terreno" value="STERRATO">
                            <div class="option-card">
                                <span class="option-emoji">⛰️</span>
                                <span class="option-label">Trail / Sterrato</span>
                            </div>
                        </label>
                        <label class="quiz-option">
                            <input type="radio" name="terreno" value="ANY">
                            <div class="option-card">
                                <span class="option-emoji">🤷‍♂️</span>
                                <span class="option-label">Entrambi</span>
                            </div>
                        </label>
                    </div>

                    <div class="step-actions split-align">
                        <button type="button" class="btn-secondary btn-prev">&larr; Indietro</button>
                        <button type="button" class="btn-primary btn-next">Continua &rarr;</button>
                    </div>
                </div>

                <%-- ================= STEP 3: APPOGGIO ================= --%>
                <div class="quiz-step hidden-step-right" id="step-3" data-step="3">
                    <h2>3. Che tipo di appoggio hai?</h2>
                    
                    <div class="options-grid grid-4">
                        <label class="quiz-option">
                            <input type="radio" name="appoggio" value="NEUTRO" required>
                            <div class="option-card">
                                <span class="option-emoji">🦶</span>
                                <span class="option-label">Neutro</span>
                            </div>
                        </label>
                        <label class="quiz-option">
                            <input type="radio" name="appoggio" value="PRONATORE">
                            <div class="option-card">
                                <span class="option-emoji">↘️</span>
                                <span class="option-label">Pronatore<br><small>(Cede verso l'interno)</small></span>
                            </div>
                        </label>
                        <label class="quiz-option">
                            <input type="radio" name="appoggio" value="SUPINATORE">
                            <div class="option-card">
                                <span class="option-emoji">↙️</span>
                                <span class="option-label">Supinatore<br><small>(Cede verso l'esterno)</small></span>
                            </div>
                        </label>
                        <label class="quiz-option">
                            <input type="radio" name="appoggio" value="ANY">
                            <div class="option-card">
                                <span class="option-emoji">🤔</span>
                                <span class="option-label">Non lo so</span>
                            </div>
                        </label>
                    </div>

                    <div class="step-actions split-align">
                        <button type="button" class="btn-secondary btn-prev">&larr; Indietro</button>
                        <button type="button" class="btn-primary btn-next">Continua &rarr;</button>
                    </div>
                </div>

                <%-- ================= STEP 4: PASSO ================= --%>
                <div class="quiz-step hidden-step-right" id="step-4" data-step="4">
                    <h2>4. A che ritmo corri?</h2>
                    
                    <div class="options-grid grid-4">
                        <label class="quiz-option">
                            <input type="radio" name="passo" value="LENTO" required>
                            <div class="option-card">
                                <span class="option-emoji">🐢</span>
                                <span class="option-label">Lento<br><small>(> 5:00 min/km)</small></span>
                            </div>
                        </label>
                        <label class="quiz-option">
                            <input type="radio" name="passo" value="MEDIO">
                            <div class="option-card">
                                <span class="option-emoji">🏃</span>
                                <span class="option-label">Medio<br><small>(4:00 - 5:00 min/km)</small></span>
                            </div>
                        </label>
                        <label class="quiz-option">
                            <input type="radio" name="passo" value="VELOCE">
                            <div class="option-card">
                                <span class="option-emoji">🚀</span>
                                <span class="option-label">Veloce<br><small>(< 4:00 min/km)</small></span>
                            </div>
                        </label>
                        <label class="quiz-option">
                            <input type="radio" name="passo" value="ANY">
                            <div class="option-card">
                                <span class="option-emoji">⏱️</span>
                                <span class="option-label">Variegato</span>
                            </div>
                        </label>
                    </div>

                    <div class="step-actions split-align">
                        <button type="button" class="btn-secondary btn-prev">&larr; Indietro</button>
                        <%-- BOTTONE FINALE DI SUBMIT --%>
                        <button type="submit" class="btn-primary btn-submit">Trovami le scarpe! 🎯</button>
                    </div>
                </div>

            </form>
        </div>
    </main>

    <%-- Inclusione del Footer Pubblico --%>
    <jsp:include page="/WEB-INF/view/fragments/footer.jsp" />

    <%-- Placeholder per il Toast --%>
    <jsp:include page="/WEB-INF/view/fragments/toast.jsp" />

    <%-- Script dedicato per le animazioni del Quiz --%>
    <script src="${pageContext.request.contextPath}/js/findmyshoes.js"></script>
    <script src="${pageContext.request.contextPath}/js/toast.js"></script>

</body>
</html>
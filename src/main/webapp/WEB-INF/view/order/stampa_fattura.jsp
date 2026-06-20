<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="it.runitup.model.bean.Ordine" %>
<%@ page import="it.runitup.model.bean.DettaglioOrdine" %>

<%
    // Recupero attributi passati dalla Servlet
    Ordine ordine = (Ordine) request.getAttribute("ordine");
    @SuppressWarnings("unchecked")
    List<DettaglioOrdine> dettagliOrdini = (List<DettaglioOrdine>) request.getAttribute("dettagliOrdini");

    // Formattatore data per visualizzare la data in modo leggibile
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <%-- Il titolo della pagina diventa il nome predefinito quando si fa "Salva come PDF" --%>
    <title>Ricevuta_Ordine_<%= ordine.getIdOrdine() %>.pdf</title>
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/images/common/favicon.png">
    
    <%-- Includiamo ESCLUSIVAMENTE il CSS per il layout A4 --%>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/stampa_fattura.css">
</head>
<body>

    <!-- Il contenitore principale che simula il foglio A4 -->
    <div class="a4-page">
        
        <!-- HEADER: Dati Aziendali e Dati Documento -->
        <header class="fattura-header">
            <div class="company-info">
                <img src="${pageContext.request.contextPath}/images/common/icons/logo-runitup.svg" alt="RunItUp Logo" class="company-logo">
                <h1 class="company-name">RunTime SRL</h1>
                <p>Via Giovanni Paolo II, 32</p>
                <p>84084 Fisciano (SA), Italia</p>
                <p>P.IVA: 01234567890</p>
            </div>
            <div class="document-info">
                <h2>RICEVUTA DI ACQUISTO</h2>
                <table class="doc-meta-table">
                    <tr>
                        <th>N° Ordine:</th>
                        <td><%= ordine.getIdOrdine() %></td>
                    </tr>
                    <tr>
                        <th>Data:</th>
                        <td><%= ordine.getDataOrdine().format(formatter) %></td>
                    </tr>
                    <tr>
                        <th>Pagamento:</th>
                        <td><%= ordine.getMetodoPagamento() %></td>
                    </tr>
                </table>
            </div>
        </header>

        <!-- SEZIONE CLIENTE: Indirizzo di Spedizione ed Email -->
        <section class="customer-section">
            <div class="customer-box">
                <h3>Intestato a:</h3>
                <p><strong><%= ordine.getNome_e_cognome_destinatario() %></strong></p>
                <p><%= ordine.getIndirizzoSpedizione() %></p>
                <p><%= ordine.getCap() %> - <%= ordine.getCitta() %></p>
                <p>Email: <%= ordine.getUtente().getEmail() %></p>
            </div>
        </section>

        <!-- SEZIONE TABELLARE: Articoli Acquistati -->
        <section class="items-section">
            <table class="items-table">
                <thead>
                    <tr>
                        <th class="col-desc">Descrizione Articolo</th>
                        <th class="col-qty">Q.tà</th>
                        <th class="col-price">Prezzo Unit.</th>
                        <th class="col-total">Totale</th>
                    </tr>
                </thead>
                <tbody>
                    <% for(DettaglioOrdine dett : dettagliOrdini) { 
                        // Calcoliamo il subtotale di riga
                        java.math.BigDecimal subtotaleRiga = dett.getPrezzoUnitarioConIva().multiply(new java.math.BigDecimal(dett.getQuantitaAcquistata()));
                    %>
                    <tr>
                        <td class="col-desc">
                            <strong><%= dett.getNomeProdottoStorico() %></strong><br>
                            <span class="item-variants">Colore: <%= dett.getColoreStorico() %> | Taglia: <%= dett.getTagliaStorica() %></span>
                        </td>
                        <td class="col-qty"><%= dett.getQuantitaAcquistata() %></td>
                        <td class="col-price">€ <%= String.format(java.util.Locale.ITALY, "%.2f", dett.getPrezzoUnitarioConIva()) %></td>
                        <td class="col-total">€ <%= String.format(java.util.Locale.ITALY, "%.2f", subtotaleRiga) %></td>
                    </tr>
                    <% } %>
                </tbody>
            </table>
        </section>

        <!-- SEZIONE TOTALI: Resoconto economico -->
        <section class="totals-section">
            <div class="totals-box">
                <table class="totals-table">
                    <tr>
                        <th>Imponibile:</th>
                        <td>€ <%= String.format(java.util.Locale.ITALY, "%.2f", ordine.getTotaleImponibile()) %></td>
                    </tr>
                    <tr>
                        <th>IVA (22%):</th>
                        <td>€ <%= String.format(java.util.Locale.ITALY, "%.2f", ordine.getTotaleIva()) %></td>
                    </tr>
                    <tr>
                        <th>Spese di Spedizione:</th>
                        <td>
                            <% if (ordine.getCostoSpedizione().compareTo(java.math.BigDecimal.ZERO) == 0) { %>
                                Gratis
                            <% } else { %>
                                € <%= String.format(java.util.Locale.ITALY, "%.2f", ordine.getCostoSpedizione()) %>
                            <% } %>
                        </td>
                    </tr>
                    <% if (ordine.getCoupon() != null) { %>
                    <tr class="row-discount">
                        <th>Sconto (<%= ordine.getCoupon().getCodice() %>):</th>
                        <td>-€ <%= String.format(java.util.Locale.ITALY, "%.2f", ordine.getCoupon().getValoreSconto()) %></td>
                    </tr>
                    <% } %>
                    <tr class="row-final-total">
                        <th>TOTALE PAGATO:</th>
                        <td>€ <%= String.format(java.util.Locale.ITALY, "%.2f", ordine.getTotalePagato()) %></td>
                    </tr>
                </table>
            </div>
        </section>

        <!-- FOOTER DEL DOCUMENTO -->
        <footer class="doc-footer">
            <p><strong>Grazie per aver scelto RunItUp!</strong></p>
            <p>Il presente documento ha validità come ricevuta di acquisto ai fini della garanzia e resi.</p>
        </footer>
        
    </div>

    <!-- Script inline per la stampa automatica -->
    <script>
        window.onload = function() {
            // Un piccolo ritardo di 300ms garantisce che il browser abbia
            // il tempo di caricare completamente il CSS e i font prima di stampare
            setTimeout(function() {
                window.print();
            }, 300);
        };
    </script>
</body>
</html>
package it.runitup.control.checkout;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

import it.runitup.model.bean.DettaglioOrdine;
import it.runitup.model.bean.Ordine;
import it.runitup.model.dao.OrdineDAO;

@WebServlet("/fattura_pdf")
public class FatturaPdfServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private final OrdineDAO ordineDAO = new OrdineDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 1. Recupero dell'ordine già validato dal nostro OrderOwnerFilter
        Ordine ordine = (Ordine) request.getAttribute("ordineValidato");

        if (ordine == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Richiesta PDF non valida.");
            return;
        }

        try {
            // 2. Caricamento dei prodotti
            List<DettaglioOrdine> dettagli = ordineDAO.getDettagliByOrdine(ordine.getIdOrdine());

            // 3. Preparazione degli Header HTTP per forzare il download del PDF
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=\"Fattura_Ordine_" + ordine.getIdOrdine() + ".pdf\"");

            // 4. Inizializzazione del Documento iText
            Document document = new Document();
            
            // Il PdfWriter collega il documento iText all'OutputStream della Servlet
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            // --- DEFINIZIONE DEI FONT ---
            Font fontTitolo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
            Font fontSottotitolo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
            Font fontNormale = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
            Font fontGrigio = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.DARK_GRAY);
            Font fontIntestazioneTabella = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);

            // --- SEZIONE 1: HEADER (Azienda e Dati Documento) ---
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{60, 40}); // Proporzioni delle due colonne

            // Colonna Sinistra (Dati RunItUp)
            PdfPCell leftCell = new PdfPCell();
            leftCell.setBorder(PdfPCell.NO_BORDER);
            leftCell.addElement(new Paragraph("RunTime SRL.", fontTitolo));
            leftCell.addElement(new Paragraph("Via Giovanni Paolo II, 32", fontGrigio));
            leftCell.addElement(new Paragraph("84084 Fisciano (SA), Italia", fontGrigio));
            leftCell.addElement(new Paragraph("P.IVA: 01234567890", fontGrigio));
            headerTable.addCell(leftCell);

            // Colonna Destra (Dati Ricevuta)
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            PdfPCell rightCell = new PdfPCell();
            rightCell.setBorder(PdfPCell.NO_BORDER);
            rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            
            Paragraph pRicevuta = new Paragraph("RICEVUTA DI ACQUISTO\n", fontSottotitolo);
            pRicevuta.setAlignment(Element.ALIGN_RIGHT);
            rightCell.addElement(pRicevuta);
            
            Paragraph pDatiOrdine = new Paragraph(
                "N° Ordine: " + ordine.getIdOrdine() + "\n" +
                "Data: " + ordine.getDataOrdine().format(formatter) + "\n" +
                "Pagamento: " + ordine.getMetodoPagamento(), fontNormale);
            pDatiOrdine.setAlignment(Element.ALIGN_RIGHT);
            rightCell.addElement(pDatiOrdine);
            
            headerTable.addCell(rightCell);
            document.add(headerTable);

            // Linea di separazione
            document.add(new Chunk(new LineSeparator(1f, 100f, BaseColor.BLACK, Element.ALIGN_CENTER, -10f)));
            document.add(new Paragraph("\n"));

            // --- SEZIONE 2: DATI CLIENTE ---
            Paragraph intestazioneCliente = new Paragraph("Intestato a:", fontSottotitolo);
            document.add(intestazioneCliente);
            
            Paragraph datiCliente = new Paragraph(
                ordine.getNome_e_cognome_destinatario() + "\n" +
                ordine.getIndirizzoSpedizione() + "\n" +
                ordine.getCap() + " - " + ordine.getCitta() + "\n" +
                "Email: " + ordine.getUtente().getEmail() + "\n\n", fontNormale);
            document.add(datiCliente);

            // --- SEZIONE 3: TABELLA PRODOTTI ---
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{45, 15, 20, 20}); // Larghezze relative colonne
            table.setSpacingBefore(10f);
            table.setSpacingAfter(20f);

            // Intestazioni di colonna
            String[] headers = {"Descrizione Articolo", "Q.tà", "Prezzo Unit.", "Totale"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, fontIntestazioneTabella));
                // Usiamo un grigio simile allo sfondo alternativo del tuo CSS
                cell.setBackgroundColor(new BaseColor(100, 116, 139)); 
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(8f);
                table.addCell(cell);
            }

            // Inserimento Righe Prodotti
            for (DettaglioOrdine dett : dettagli) {
                java.math.BigDecimal subtotaleRiga = dett.getPrezzoUnitarioConIva().multiply(new java.math.BigDecimal(dett.getQuantitaAcquistata()));

                PdfPCell cellDesc = new PdfPCell();
                cellDesc.addElement(new Paragraph(dett.getNomeProdottoStorico(), fontSottotitolo));
                cellDesc.addElement(new Paragraph("Colore: " + dett.getColoreStorico() + " | Taglia: " + dett.getTagliaStorica(), fontGrigio));
                cellDesc.setPaddingBottom(8f);
                table.addCell(cellDesc);

                PdfPCell cellQty = new PdfPCell(new Phrase(String.valueOf(dett.getQuantitaAcquistata()), fontNormale));
                cellQty.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cellQty);

                PdfPCell cellPrezzo = new PdfPCell(new Phrase("€ " + String.format(java.util.Locale.ITALY, "%.2f", dett.getPrezzoUnitarioConIva()), fontNormale));
                cellPrezzo.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(cellPrezzo);

                PdfPCell cellTot = new PdfPCell(new Phrase("€ " + String.format(java.util.Locale.ITALY, "%.2f", subtotaleRiga), fontNormale));
                cellTot.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(cellTot);
            }
            document.add(table);

            // --- SEZIONE 4: TOTALI ---
            PdfPTable totaliTable = new PdfPTable(2);
            totaliTable.setWidthPercentage(40);
            totaliTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totaliTable.setWidths(new float[]{60, 40});

            aggiungiRigaTotale(totaliTable, "Imponibile:", "€ " + String.format(java.util.Locale.ITALY, "%.2f", ordine.getTotaleImponibile()), fontNormale);
            aggiungiRigaTotale(totaliTable, "IVA (22%):", "€ " + String.format(java.util.Locale.ITALY, "%.2f", ordine.getTotaleIva()), fontNormale);
            
            String stringaSpedizione = ordine.getCostoSpedizione().compareTo(java.math.BigDecimal.ZERO) == 0 ? 
                                     "Gratis" : "€ " + String.format(java.util.Locale.ITALY, "%.2f", ordine.getCostoSpedizione());
            aggiungiRigaTotale(totaliTable, "Spedizione:", stringaSpedizione, fontNormale);

            if (ordine.getCoupon() != null) {
                Font fontRosso = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new BaseColor(204, 58, 18));
                aggiungiRigaTotale(totaliTable, "Sconto (" + ordine.getCoupon().getCodice() + "):", 
                                 "-€ " + String.format(java.util.Locale.ITALY, "%.2f", ordine.getCoupon().getValoreSconto()), fontRosso);
            }

            // Totale Finale in Grassetto e più grande
            Font fontTotaleFinale = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
            aggiungiRigaTotale(totaliTable, "TOTALE PAGATO:", "€ " + String.format(java.util.Locale.ITALY, "%.2f", ordine.getTotalePagato()), fontTotaleFinale);

            document.add(totaliTable);

            // Chiusura Documento
            document.close();

        } catch (SQLException | DocumentException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante la generazione del PDF: " + e.getMessage());
        }
    }

    // Metodo helper per inserire rapidamente le righe della tabella dei totali
    private void aggiungiRigaTotale(PdfPTable table, String etichetta, String valore, Font font) {
        PdfPCell cellEtichetta = new PdfPCell(new Phrase(etichetta, font));
        cellEtichetta.setBorder(PdfPCell.NO_BORDER);
        cellEtichetta.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellEtichetta.setPaddingBottom(5f);
        
        PdfPCell cellValore = new PdfPCell(new Phrase(valore, font));
        cellValore.setBorder(PdfPCell.NO_BORDER);
        cellValore.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellValore.setPaddingBottom(5f);
        
        table.addCell(cellEtichetta);
        table.addCell(cellValore);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
}
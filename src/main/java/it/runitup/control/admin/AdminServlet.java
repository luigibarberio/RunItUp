package it.runitup.control.admin;

import javax.servlet.annotation.WebServlet;

// Servlet che funge da "Punto di Ingresso" per l'area amministrativa, ma che di fatto reindirizza a AdminProdottiServlet, che è la pagina più rilevante e rappresentativa del pannello di controllo.
@WebServlet("/admin")
public class AdminServlet extends javax.servlet.http.HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, java.io.IOException {
        // Reindirizza direttamente alla pagina di gestione prodotti, che è il cuore del pannello admin.
        response.sendRedirect(request.getContextPath() + "/admin/prodotti");
    }

    @Override
    protected void doPost(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, java.io.IOException {
        // Esegue la doGet
        doGet(request, response);
    }
}

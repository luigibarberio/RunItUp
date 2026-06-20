-- ==============================================================================
-- RUNITUP - SCRIPT DI PULIZIA (00-drop.sql)
-- ==============================================================================

DROP TABLE IF EXISTS voce_carrello CASCADE;
DROP TABLE IF EXISTS carrello CASCADE;
DROP TABLE IF EXISTS dettaglio_ordine CASCADE;
DROP TABLE IF EXISTS ordine CASCADE;
DROP TABLE IF EXISTS variante_promozione CASCADE;
DROP TABLE IF EXISTS variante_prodotto CASCADE;
DROP TABLE IF EXISTS prodotto CASCADE;
DROP TABLE IF EXISTS promozione CASCADE;
DROP TABLE IF EXISTS brand CASCADE;
DROP TABLE IF EXISTS coupon CASCADE;
DROP TABLE IF EXISTS utente CASCADE;

DROP TYPE IF EXISTS genere_scarpa CASCADE;
DROP TYPE IF EXISTS tipo_terreno CASCADE;
DROP TYPE IF EXISTS tipo_passo CASCADE;
DROP TYPE IF EXISTS tipo_appoggio CASCADE;
DROP TYPE IF EXISTS stato_ordine CASCADE;
DROP TYPE IF EXISTS tipo_sconto_coupon CASCADE;
DROP TYPE IF EXISTS metodo_pagamento CASCADE;
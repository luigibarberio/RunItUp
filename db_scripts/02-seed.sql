-- ==============================================================================
-- RUNITUP - DATI DI INIZIALIZZAZIONE (02-seed.sql)
-- ==============================================================================

-- Utente Admin di partenza (La password in chiaro andrà sostituita con l'hash SHA-256 nel codice Java)
INSERT INTO utente (nome, cognome, email, password_hash, is_admin) 
VALUES ('Luigi', 'Barberio', 'luigi@runitup.it', '$2a$12$3XTnicH5lqc81C3F9RkL5OqDT53ymwQ7x0OWnfmXHkeZSGeS0Fnf6', TRUE);

INSERT INTO utente (nome, cognome, email, password_hash, is_admin) 
VALUES ('Antonio', 'Tolino', 'antonio@runitup.it', '$2a$12$3XTnicH5lqc81C3F9RkL5OqDT53ymwQ7x0OWnfmXHkeZSGeS0Fnf6', TRUE);

-- Inseriamo alcuni Brand di base
INSERT INTO brand (nome_brand) VALUES ('Nike'), ('Asics'), ('Hoka'), ('Brooks'), ('Saucony');

-- Inseriamo un Coupon attivo da oggi fino alla fine dell'anno
INSERT INTO coupon (codice, valore_sconto, tipo_sconto, data_inizio, data_scadenza) 
VALUES ('BENVENUTO10', 10.00, 'PERCENTUALE', CURRENT_DATE, '2026-12-31');
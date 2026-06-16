-- ==============================================================================
-- RUNITUP - SUPER SEED DATA (02-seed.sql)
-- ==============================================================================
-- Contiene un set di dati ampio e realistico per testare e-commerce, filtri, 
-- paginazione, carrelli e storico ordini.
-- Tutte le password sono 'password123'

-- ==============================================================================
-- 1. UTENTI
-- ==============================================================================
INSERT INTO utente (nome, cognome, email, password_hash, telefono, is_admin) VALUES 
('Luigi', 'Barberio', 'luigi@runitup.it', '$2a$12$3XTnicH5lqc81C3F9RkL5OqDT53ymwQ7x0OWnfmXHkeZSGeS0Fnf6', '3331234567', TRUE),
('Antonio', 'Tolino', 'antonio@runitup.it', '$2a$12$3XTnicH5lqc81C3F9RkL5OqDT53ymwQ7x0OWnfmXHkeZSGeS0Fnf6', '3339876543', TRUE),
('Mario', 'Rossi', 'cliente1@example.com', '$2a$12$3XTnicH5lqc81C3F9RkL5OqDT53ymwQ7x0OWnfmXHkeZSGeS0Fnf6', '3201112233', FALSE),
('Giulia', 'Bianchi', 'cliente2@example.com', '$2a$12$3XTnicH5lqc81C3F9RkL5OqDT53ymwQ7x0OWnfmXHkeZSGeS0Fnf6', '3405556677', FALSE),
('Luca', 'Verdi', 'cliente3@example.com', '$2a$12$3XTnicH5lqc81C3F9RkL5OqDT53ymwQ7x0OWnfmXHkeZSGeS0Fnf6', '3318889900', FALSE),
('Martina', 'Neri', 'cliente4@example.com', '$2a$12$3XTnicH5lqc81C3F9RkL5OqDT53ymwQ7x0OWnfmXHkeZSGeS0Fnf6', '3391231234', FALSE),
('Alessandro', 'Gialli', 'cliente5@example.com', '$2a$12$3XTnicH5lqc81C3F9RkL5OqDT53ymwQ7x0OWnfmXHkeZSGeS0Fnf6', NULL, FALSE),
('Francesca', 'Marrone', 'cliente6@example.com', '$2a$12$3XTnicH5lqc81C3F9RkL5OqDT53ymwQ7x0OWnfmXHkeZSGeS0Fnf6', '3334445555', FALSE);

-- ==============================================================================
-- 2. BRAND
-- ==============================================================================
INSERT INTO brand (nome_brand) VALUES 
('Nike'), ('Adidas'), ('Asics'), ('Brooks'), ('Hoka'), ('Saucony'), 
('Mizuno'), ('New Balance'), ('Salomon'), ('On Running'), ('Puma'), ('La Sportiva');

-- ==============================================================================
-- 3. PROMOZIONI E COUPON
-- ==============================================================================
INSERT INTO promozione (nome, percentuale, data_inizio, data_fine) VALUES 
('Sconto Primavera', 20.00, CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP + INTERVAL '15 days'),
('Fuori Tutto Inverno', 50.00, CURRENT_TIMESTAMP - INTERVAL '60 days', CURRENT_TIMESTAMP - INTERVAL '10 days'), -- SCADUTA
('Promo Trail', 15.00, CURRENT_TIMESTAMP - INTERVAL '1 days', CURRENT_TIMESTAMP + INTERVAL '30 days'),
('Flash Sale 48H', 30.00, CURRENT_TIMESTAMP - INTERVAL '12 hours', CURRENT_TIMESTAMP + INTERVAL '36 hours'),
('Back to School', 25.00, CURRENT_TIMESTAMP + INTERVAL '90 days', CURRENT_TIMESTAMP + INTERVAL '120 days'); -- FUTURA

INSERT INTO coupon (codice, valore_sconto, tipo_sconto, data_inizio, data_scadenza) VALUES 
('BENVENUTO10', 10.00, 'PERCENTUALE', CURRENT_DATE - INTERVAL '100 days', NULL),
('MINUS20', 20.00, 'FISSO', CURRENT_DATE, '2026-12-31'),
('TRAILVIP', 50.00, 'FISSO', CURRENT_DATE - INTERVAL '5 days', CURRENT_DATE + INTERVAL '5 days'),
('SCADUTO15', 15.00, 'PERCENTUALE', CURRENT_DATE - INTERVAL '30 days', CURRENT_DATE - INTERVAL '1 day'),
('SUMMER25', 25.00, 'PERCENTUALE', CURRENT_DATE, '2026-08-31');

-- ==============================================================================
-- 4. PRODOTTI (Mix di vecchi, nuovi, strada, sterrato, pronatori ecc.)
-- ==============================================================================

-- NIKE
INSERT INTO prodotto (nome, descrizione, id_brand, genere, tipo_terreno, tipo_appoggio, tipo_passo, data_inserimento, in_evidenza, totale_vendite) VALUES 
('Pegasus 40', 'La scarpa da running per eccellenza. Affidabile e reattiva per l''allenamento quotidiano.', (SELECT id_brand FROM brand WHERE nome_brand = 'Nike'), 'UOMO', 'STRADA', 'NEUTRO', 'MEDIO', CURRENT_TIMESTAMP - INTERVAL '50 days', true, 540),
('Vaporfly 3', 'Il top per le gare. Piastra in carbonio per battere ogni tuo record in maratona.', (SELECT id_brand FROM brand WHERE nome_brand = 'Nike'), 'UNISEX', 'STRADA', 'NEUTRO', 'VELOCE', CURRENT_TIMESTAMP - INTERVAL '10 days', true, 120),
('Wildhorse 8', 'Resistente e pensata per affrontare i sentieri più impervi. Ottimo grip e protezione.', (SELECT id_brand FROM brand WHERE nome_brand = 'Nike'), 'DONNA', 'STERRATO', 'NEUTRO', 'LENTO', CURRENT_TIMESTAMP - INTERVAL '100 days', false, 80),
('Structure 25', 'Supporto mirato per pronatori. Ammortizzazione stabile per le lunghe corse.', (SELECT id_brand FROM brand WHERE nome_brand = 'Nike'), 'UOMO', 'STRADA', 'PRONATORE', 'MEDIO', CURRENT_TIMESTAMP - INTERVAL '5 days', false, 30);

-- ADIDAS
INSERT INTO prodotto (nome, descrizione, id_brand, genere, tipo_terreno, tipo_appoggio, tipo_passo, data_inserimento, in_evidenza, totale_vendite) VALUES 
('Ultraboost Light', 'Incredibile ritorno di energia. La mescola Boost più leggera di sempre.', (SELECT id_brand FROM brand WHERE nome_brand = 'Adidas'), 'DONNA', 'STRADA', 'NEUTRO', 'LENTO', CURRENT_TIMESTAMP - INTERVAL '200 days', true, 800),
('Adizero Adios Pro 3', 'Progettata per vincere. Schiuma Lightstrike Pro e barre in carbonio ENERGYRODS.', (SELECT id_brand FROM brand WHERE nome_brand = 'Adidas'), 'UNISEX', 'STRADA', 'NEUTRO', 'VELOCE', CURRENT_TIMESTAMP - INTERVAL '2 days', true, 45),
('Terrex Speed Ultra', 'Veloce sui sentieri, nata per le gare di trail running leggere.', (SELECT id_brand FROM brand WHERE nome_brand = 'Adidas'), 'UOMO', 'STERRATO', 'NEUTRO', 'VELOCE', CURRENT_TIMESTAMP - INTERVAL '40 days', false, 65);

-- ASICS
INSERT INTO prodotto (nome, descrizione, id_brand, genere, tipo_terreno, tipo_appoggio, tipo_passo, data_inserimento, in_evidenza, totale_vendite) VALUES 
('Gel-Nimbus 25', 'Comfort assoluto. L''ammortizzazione PureGEL garantisce atterraggi morbidissimi.', (SELECT id_brand FROM brand WHERE nome_brand = 'Asics'), 'UOMO', 'STRADA', 'NEUTRO', 'LENTO', CURRENT_TIMESTAMP - INTERVAL '60 days', false, 410),
('Gel-Kayano 30', 'Il punto di riferimento per la stabilità. Supporto avanzato per iperpronatori.', (SELECT id_brand FROM brand WHERE nome_brand = 'Asics'), 'DONNA', 'STRADA', 'PRONATORE', 'MEDIO', CURRENT_TIMESTAMP - INTERVAL '15 days', true, 320),
('Novablast 3', 'Rimbalzo esplosivo e divertimento ad ogni passo grazie alla schiuma FF BLAST PLUS.', (SELECT id_brand FROM brand WHERE nome_brand = 'Asics'), 'UOMO', 'STRADA', 'NEUTRO', 'VELOCE', CURRENT_TIMESTAMP - INTERVAL '120 days', false, 280),
('Trabuco Max 2', 'Massima ammortizzazione per il trail running sulle lunghissime distanze.', (SELECT id_brand FROM brand WHERE nome_brand = 'Asics'), 'UNISEX', 'STERRATO', 'NEUTRO', 'LENTO', CURRENT_TIMESTAMP - INTERVAL '8 days', false, 25);

-- BROOKS
INSERT INTO prodotto (nome, descrizione, id_brand, genere, tipo_terreno, tipo_appoggio, tipo_passo, data_inserimento, in_evidenza, totale_vendite) VALUES 
('Ghost 15', 'Morbida, fluida e amata da tutti i runner. Una garanzia su strada.', (SELECT id_brand FROM brand WHERE nome_brand = 'Brooks'), 'DONNA', 'STRADA', 'NEUTRO', 'MEDIO', CURRENT_TIMESTAMP - INTERVAL '80 days', true, 600),
('Adrenaline GTS 22', 'Stabilità intelligente con sistema GuideRails. Supporto ideale per pronatori.', (SELECT id_brand FROM brand WHERE nome_brand = 'Brooks'), 'UOMO', 'STRADA', 'PRONATORE', 'LENTO', CURRENT_TIMESTAMP - INTERVAL '150 days', false, 450),
('Cascadia 17', 'Pronta per ogni montagna. Scudo antipietra integrato e trazione TrailTack.', (SELECT id_brand FROM brand WHERE nome_brand = 'Brooks'), 'UOMO', 'STERRATO', 'NEUTRO', 'MEDIO', CURRENT_TIMESTAMP - INTERVAL '3 days', true, 15);

-- HOKA
INSERT INTO prodotto (nome, descrizione, id_brand, genere, tipo_terreno, tipo_appoggio, tipo_passo, data_inserimento, in_evidenza, totale_vendite) VALUES 
('Clifton 9', 'Leggerissima e incredibilmente morbida. Perfetta per le corse quotidiane.', (SELECT id_brand FROM brand WHERE nome_brand = 'Hoka'), 'UOMO', 'STRADA', 'NEUTRO', 'LENTO', CURRENT_TIMESTAMP - INTERVAL '25 days', true, 350),
('Speedgoat 5', 'Una belva sui sentieri. Trazione Vibram Megagrip per i terreni più tecnici.', (SELECT id_brand FROM brand WHERE nome_brand = 'Hoka'), 'UNISEX', 'STERRATO', 'NEUTRO', 'MEDIO', CURRENT_TIMESTAMP - INTERVAL '180 days', true, 550),
('Arahi 6', 'Stabilità senza peso grazie al J-Frame. Ideale per iperpronatori.', (SELECT id_brand FROM brand WHERE nome_brand = 'Hoka'), 'DONNA', 'STRADA', 'PRONATORE', 'LENTO', CURRENT_TIMESTAMP - INTERVAL '90 days', false, 120);

-- ALTRI BRAND
INSERT INTO prodotto (nome, descrizione, id_brand, genere, tipo_terreno, tipo_appoggio, tipo_passo, data_inserimento, in_evidenza, totale_vendite) VALUES 
('Endorphin Pro 3', 'Piastra in carbonio e schiuma PWRRUN PB per le tue maratone record.', (SELECT id_brand FROM brand WHERE nome_brand = 'Saucony'), 'UOMO', 'STRADA', 'NEUTRO', 'VELOCE', CURRENT_TIMESTAMP - INTERVAL '40 days', true, 210),
('Kinvara 14', 'Flessibile e minimalista. Per chi ama sentire la strada sotto i piedi.', (SELECT id_brand FROM brand WHERE nome_brand = 'Saucony'), 'DONNA', 'STRADA', 'NEUTRO', 'VELOCE', CURRENT_TIMESTAMP - INTERVAL '7 days', false, 45),
('Wave Rider 27', 'La classica di Mizuno. Piastra Wave per transizioni fluide e reattive.', (SELECT id_brand FROM brand WHERE nome_brand = 'Mizuno'), 'UOMO', 'STRADA', 'NEUTRO', 'MEDIO', CURRENT_TIMESTAMP - INTERVAL '110 days', false, 300),
('Fresh Foam 1080v12', 'Ammortizzazione premium. Ti sembrerà di correre sulle nuvole.', (SELECT id_brand FROM brand WHERE nome_brand = 'New Balance'), 'DONNA', 'STRADA', 'NEUTRO', 'LENTO', CURRENT_TIMESTAMP - INTERVAL '55 days', true, 420),
('Speedcross 6', 'Il fango non ti spaventa. Tasselli aggressivi per terreni morbidi e scivolosi.', (SELECT id_brand FROM brand WHERE nome_brand = 'Salomon'), 'UOMO', 'STERRATO', 'NEUTRO', 'MEDIO', CURRENT_TIMESTAMP - INTERVAL '200 days', true, 650),
('Cloudmonster', 'Ammortizzazione esagerata firmata On. Elementi CloudMax per atterraggi morbidi.', (SELECT id_brand FROM brand WHERE nome_brand = 'On Running'), 'UNISEX', 'STRADA', 'NEUTRO', 'MEDIO', CURRENT_TIMESTAMP - INTERVAL '12 days', true, 180),
('Bushido II', 'Scarpa da skyrunning. Avvolgente, super tecnica e reattiva.', (SELECT id_brand FROM brand WHERE nome_brand = 'La Sportiva'), 'UOMO', 'STERRATO', 'SUPINATORE', 'MEDIO', CURRENT_TIMESTAMP - INTERVAL '300 days', false, 240);


-- ==============================================================================
-- 5. VARIANTI PRODOTTO (Almeno 2-3 per ogni scarpa, prezzi e stock diversi)
-- ==============================================================================

-- NIKE Pegasus 40
INSERT INTO variante_prodotto (id_prodotto, colore, taglia, prezzo, quantita_magazzino) VALUES 
((SELECT id_prodotto FROM prodotto WHERE nome = 'Pegasus 40'), 'Nero/Bianco', '42', 129.99, 15),
((SELECT id_prodotto FROM prodotto WHERE nome = 'Pegasus 40'), 'Nero/Bianco', '43', 129.99, 10),
((SELECT id_prodotto FROM prodotto WHERE nome = 'Pegasus 40'), 'Rosso Fuoco', '44', 135.00, 5),
((SELECT id_prodotto FROM prodotto WHERE nome = 'Pegasus 40'), 'Rosso Fuoco', '45', 135.00, 0); -- ESAURITA

-- NIKE Vaporfly 3
INSERT INTO variante_prodotto (id_prodotto, colore, taglia, prezzo, quantita_magazzino) VALUES 
((SELECT id_prodotto FROM prodotto WHERE nome = 'Vaporfly 3'), 'Rosa Elettrico', '41', 259.99, 4),
((SELECT id_prodotto FROM prodotto WHERE nome = 'Vaporfly 3'), 'Rosa Elettrico', '42.5', 259.99, 2),
((SELECT id_prodotto FROM prodotto WHERE nome = 'Vaporfly 3'), 'Bianco/Oro', '43', 269.99, 8);

-- NIKE Wildhorse e Structure
INSERT INTO variante_prodotto (id_prodotto, colore, taglia, prezzo, quantita_magazzino) VALUES 
((SELECT id_prodotto FROM prodotto WHERE nome = 'Wildhorse 8'), 'Verde Oliva', '38', 119.99, 12),
((SELECT id_prodotto FROM prodotto WHERE nome = 'Wildhorse 8'), 'Verde Oliva', '39', 119.99, 20),
((SELECT id_prodotto FROM prodotto WHERE nome = 'Structure 25'), 'Blu Navy', '42', 140.00, 15);

-- ADIDAS Ultraboost 
INSERT INTO variante_prodotto (id_prodotto, colore, taglia, prezzo, quantita_magazzino) VALUES 
((SELECT id_prodotto FROM prodotto WHERE nome = 'Ultraboost Light'), 'Bianco Ghiaccio', '37', 190.00, 25),
((SELECT id_prodotto FROM prodotto WHERE nome = 'Ultraboost Light'), 'Bianco Ghiaccio', '38', 190.00, 30),
((SELECT id_prodotto FROM prodotto WHERE nome = 'Ultraboost Light'), 'Nero', '39', 185.00, 10);

-- ADIDAS Adizero & Terrex
INSERT INTO variante_prodotto (id_prodotto, colore, taglia, prezzo, quantita_magazzino) VALUES 
((SELECT id_prodotto FROM prodotto WHERE nome = 'Adizero Adios Pro 3'), 'Giallo Fluo', '44', 250.00, 5),
((SELECT id_prodotto FROM prodotto WHERE nome = 'Terrex Speed Ultra'), 'Grigio/Giallo', '43', 160.00, 18);

-- ASICS Gel-Nimbus 25
INSERT INTO variante_prodotto (id_prodotto, colore, taglia, prezzo, quantita_magazzino) VALUES 
((SELECT id_prodotto FROM prodotto WHERE nome = 'Gel-Nimbus 25'), 'Blu Navy', '42', 190.00, 40),
((SELECT id_prodotto FROM prodotto WHERE nome = 'Gel-Nimbus 25'), 'Blu Navy', '43', 190.00, 35),
((SELECT id_prodotto FROM prodotto WHERE nome = 'Gel-Nimbus 25'), 'Giallo/Nero', '42', 195.00, 5);

-- ASICS Kayano, Novablast, Trabuco
INSERT INTO variante_prodotto (id_prodotto, colore, taglia, prezzo, quantita_magazzino) VALUES 
((SELECT id_prodotto FROM prodotto WHERE nome = 'Gel-Kayano 30'), 'Viola', '39', 200.00, 15),
((SELECT id_prodotto FROM prodotto WHERE nome = 'Novablast 3'), 'Verde Acqua', '44', 150.00, 20),
((SELECT id_prodotto FROM prodotto WHERE nome = 'Trabuco Max 2'), 'Nero/Rosso', '43', 170.00, 12);

-- BROOKS Ghost 15
INSERT INTO variante_prodotto (id_prodotto, colore, taglia, prezzo, quantita_magazzino) VALUES 
((SELECT id_prodotto FROM prodotto WHERE nome = 'Ghost 15'), 'Rosa Chiaro', '38', 145.00, 50),
((SELECT id_prodotto FROM prodotto WHERE nome = 'Ghost 15'), 'Nero', '39', 140.00, 22),
((SELECT id_prodotto FROM prodotto WHERE nome = 'Ghost 15'), 'Nero', '40', 140.00, 18);

-- BROOKS Adrenaline & Cascadia
INSERT INTO variante_prodotto (id_prodotto, colore, taglia, prezzo, quantita_magazzino) VALUES 
((SELECT id_prodotto FROM prodotto WHERE nome = 'Adrenaline GTS 22'), 'Grigio/Blu', '42.5', 140.00, 10),
((SELECT id_prodotto FROM prodotto WHERE nome = 'Cascadia 17'), 'Arancione', '43', 150.00, 15);

-- HOKA Clifton & Speedgoat & Arahi
INSERT INTO variante_prodotto (id_prodotto, colore, taglia, prezzo, quantita_magazzino) VALUES 
((SELECT id_prodotto FROM prodotto WHERE nome = 'Clifton 9'), 'Azzurro', '42', 150.00, 30),
((SELECT id_prodotto FROM prodotto WHERE nome = 'Clifton 9'), 'Azzurro', '43', 150.00, 25),
((SELECT id_prodotto FROM prodotto WHERE nome = 'Speedgoat 5'), 'Arancio/Blu', '42.5', 160.00, 18),
((SELECT id_prodotto FROM prodotto WHERE nome = 'Arahi 6'), 'Azzurro', '38', 140.00, 5);

-- ALTRI (Saucony, Mizuno, NB, Salomon, On, La Sportiva)
INSERT INTO variante_prodotto (id_prodotto, colore, taglia, prezzo, quantita_magazzino) VALUES 
((SELECT id_prodotto FROM prodotto WHERE nome = 'Endorphin Pro 3'), 'Rosa', '43', 250.00, 8),
((SELECT id_prodotto FROM prodotto WHERE nome = 'Kinvara 14'), 'Bianco', '38', 130.00, 12),
((SELECT id_prodotto FROM prodotto WHERE nome = 'Wave Rider 27'), 'Blu Oltremare', '42', 155.00, 20),
((SELECT id_prodotto FROM prodotto WHERE nome = 'Fresh Foam 1080v12'), 'Grigio Scuro', '39', 180.00, 15),
((SELECT id_prodotto FROM prodotto WHERE nome = 'Speedcross 6'), 'Nero/Giallo', '44', 145.00, 45),
((SELECT id_prodotto FROM prodotto WHERE nome = 'Cloudmonster'), 'Nero/Grigio', '42', 180.00, 10),
((SELECT id_prodotto FROM prodotto WHERE nome = 'Bushido II'), 'Giallo/Nero', '42.5', 165.00, 8);


-- ==============================================================================
-- 6. COLLEGAMENTO PROMOZIONI-VARIANTI (Tabella N:M)
-- ==============================================================================

-- 'Sconto Primavera' (ATTIVA, 20%) su tutte le varianti di Clifton 9 e Gel-Nimbus 25
INSERT INTO variante_promozione (id_variante, id_promozione)
SELECT id_variante, (SELECT id_promozione FROM promozione WHERE nome = 'Sconto Primavera')
FROM variante_prodotto WHERE id_prodotto IN (
    SELECT id_prodotto FROM prodotto WHERE nome IN ('Clifton 9', 'Gel-Nimbus 25')
);

-- 'Promo Trail' (ATTIVA, 15%) su Speedgoat 5 e Speedcross 6
INSERT INTO variante_promozione (id_variante, id_promozione)
SELECT id_variante, (SELECT id_promozione FROM promozione WHERE nome = 'Promo Trail')
FROM variante_prodotto WHERE id_prodotto IN (
    SELECT id_prodotto FROM prodotto WHERE nome IN ('Speedgoat 5', 'Speedcross 6')
);

-- 'Flash Sale 48H' (ATTIVA, 30%) su una specifica variante rossa della Pegasus
INSERT INTO variante_promozione (id_variante, id_promozione)
SELECT id_variante, (SELECT id_promozione FROM promozione WHERE nome = 'Flash Sale 48H')
FROM variante_prodotto WHERE id_prodotto = (SELECT id_prodotto FROM prodotto WHERE nome = 'Pegasus 40') AND colore = 'Rosso Fuoco';

-- 'Fuori Tutto Inverno' (SCADUTA, 50%) su Ghost 15
INSERT INTO variante_promozione (id_variante, id_promozione)
SELECT id_variante, (SELECT id_promozione FROM promozione WHERE nome = 'Fuori Tutto Inverno')
FROM variante_prodotto WHERE id_prodotto = (SELECT id_prodotto FROM prodotto WHERE nome = 'Ghost 15');


-- ==============================================================================
-- 7. CARRELLI ATTIVI
-- ==============================================================================
-- Carrello per Mario (cliente1)
INSERT INTO carrello (id_utente) VALUES ((SELECT id_utente FROM utente WHERE email = 'cliente1@example.com'));
INSERT INTO voce_carrello (id_carrello, id_variante, quantita)
SELECT 
    (SELECT id_carrello FROM carrello WHERE id_utente = (SELECT id_utente FROM utente WHERE email = 'cliente1@example.com')), id_variante, 1 
FROM variante_prodotto WHERE id_prodotto = (SELECT id_prodotto FROM prodotto WHERE nome = 'Vaporfly 3') LIMIT 1;

-- Carrello per Giulia (cliente2)
INSERT INTO carrello (id_utente) VALUES ((SELECT id_utente FROM utente WHERE email = 'cliente2@example.com'));
INSERT INTO voce_carrello (id_carrello, id_variante, quantita)
SELECT 
    (SELECT id_carrello FROM carrello WHERE id_utente = (SELECT id_utente FROM utente WHERE email = 'cliente2@example.com')), id_variante, 2 
FROM variante_prodotto WHERE id_prodotto = (SELECT id_prodotto FROM prodotto WHERE nome = 'Ultraboost Light') LIMIT 1;


-- ==============================================================================
-- 8. STORICO ORDINI E DETTAGLIO ORDINI
-- ==============================================================================

-- ORDINE 1: Mario Rossi, Spedito (Con Coupon BENVENUTO10)
INSERT INTO ordine (id_utente, id_coupon, data_ordine, totale_imponibile, totale_iva, totale_pagato, costo_spedizione, nome_e_cognome_destinatario, indirizzo_spedizione, citta, cap, metodo_pagamento, stato)
VALUES (
    (SELECT id_utente FROM utente WHERE email = 'cliente1@example.com'), 
    (SELECT id_coupon FROM coupon WHERE codice = 'BENVENUTO10'), 
    CURRENT_TIMESTAMP - INTERVAL '3 days', 
    115.00, 25.30, 140.30, 10.00, -- Valori simulati post sconto
    'Mario Rossi', 'Via Roma 10', 'Salerno', '84100', 'PAYPAL', 'SPEDITO'
);
INSERT INTO dettaglio_ordine (id_ordine, id_variante, nome_prodotto_storico, colore_storico, taglia_storica, quantita_acquistata, prezzo_unitario_imponibile, aliquota_iva, prezzo_unitario_con_iva)
SELECT 
    (SELECT MAX(id_ordine) FROM ordine), v.id_variante, p.nome, v.colore, v.taglia, 1, 115.00, 22.00, 140.30
FROM variante_prodotto v JOIN prodotto p ON v.id_prodotto = p.id_prodotto
WHERE p.nome = 'Clifton 9' LIMIT 1;


-- ORDINE 2: Luca Verdi, Consegnato (No Coupon)
INSERT INTO ordine (id_utente, id_coupon, data_ordine, totale_imponibile, totale_iva, totale_pagato, costo_spedizione, nome_e_cognome_destinatario, indirizzo_spedizione, citta, cap, metodo_pagamento, stato)
VALUES (
    (SELECT id_utente FROM utente WHERE email = 'cliente3@example.com'), 
    NULL, CURRENT_TIMESTAMP - INTERVAL '45 days', 
    204.91, 45.08, 250.00, 10.00,
    'Luca Verdi', 'Piazza Duomo 1', 'Milano', '20100', 'CARTA', 'CONSEGNATO'
);
INSERT INTO dettaglio_ordine (id_ordine, id_variante, nome_prodotto_storico, colore_storico, taglia_storica, quantita_acquistata, prezzo_unitario_imponibile, aliquota_iva, prezzo_unitario_con_iva)
SELECT 
    (SELECT MAX(id_ordine) FROM ordine), v.id_variante, p.nome, v.colore, v.taglia, 1, 204.91, 22.00, 250.00
FROM variante_prodotto v JOIN prodotto p ON v.id_prodotto = p.id_prodotto
WHERE p.nome = 'Endorphin Pro 3' LIMIT 1;


-- ORDINE 3: Martina Neri, In Lavorazione (Con Coupon MINUS20)
INSERT INTO ordine (id_utente, id_coupon, data_ordine, totale_imponibile, totale_iva, totale_pagato, costo_spedizione, nome_e_cognome_destinatario, indirizzo_spedizione, citta, cap, metodo_pagamento, stato)
VALUES (
    (SELECT id_utente FROM utente WHERE email = 'cliente4@example.com'), 
    (SELECT id_coupon FROM coupon WHERE codice = 'MINUS20'), 
    CURRENT_TIMESTAMP - INTERVAL '2 hours', 
    102.45, 22.54, 125.00, 10.00, -- (145 listino - 20 sconto = 125 iva inclusa)
    'Martina Neri', 'Corso Garibaldi 45', 'Napoli', '80100', 'CONTRASSEGNO', 'IN_LAVORAZIONE'
);
INSERT INTO dettaglio_ordine (id_ordine, id_variante, nome_prodotto_storico, colore_storico, taglia_storica, quantita_acquistata, prezzo_unitario_imponibile, aliquota_iva, prezzo_unitario_con_iva)
SELECT 
    (SELECT MAX(id_ordine) FROM ordine), v.id_variante, p.nome, v.colore, v.taglia, 1, 118.85, 22.00, 145.00
FROM variante_prodotto v JOIN prodotto p ON v.id_prodotto = p.id_prodotto
WHERE p.nome = 'Speedcross 6' LIMIT 1;


-- ORDINE 4: Alessandro Gialli, Annullato
INSERT INTO ordine (id_utente, id_coupon, data_ordine, totale_imponibile, totale_iva, totale_pagato, costo_spedizione, nome_e_cognome_destinatario, indirizzo_spedizione, citta, cap, metodo_pagamento, stato)
VALUES (
    (SELECT id_utente FROM utente WHERE email = 'cliente5@example.com'), 
    NULL, CURRENT_TIMESTAMP - INTERVAL '10 days', 
    106.55, 23.44, 129.99, 10.00,
    'Alessandro Gialli', 'Via Torino 5', 'Torino', '10100', 'PAYPAL', 'ANNULLATO'
);
INSERT INTO dettaglio_ordine (id_ordine, id_variante, nome_prodotto_storico, colore_storico, taglia_storica, quantita_acquistata, prezzo_unitario_imponibile, aliquota_iva, prezzo_unitario_con_iva)
SELECT 
    (SELECT MAX(id_ordine) FROM ordine), v.id_variante, p.nome, v.colore, v.taglia, 1, 106.55, 22.00, 129.99
FROM variante_prodotto v JOIN prodotto p ON v.id_prodotto = p.id_prodotto
WHERE p.nome = 'Pegasus 40' AND taglia = '42' LIMIT 1;